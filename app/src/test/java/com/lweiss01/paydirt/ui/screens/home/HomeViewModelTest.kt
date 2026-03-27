package com.lweiss01.paydirt.ui.screens.home

import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.GoalSettingsDao
import com.lweiss01.paydirt.data.local.dao.PaymentDao
import com.lweiss01.paydirt.data.local.entity.CardEntity
import com.lweiss01.paydirt.data.local.entity.GoalSettingsEntity
import com.lweiss01.paydirt.data.local.entity.PaymentEntity
import com.lweiss01.paydirt.data.repository.CardRepository
import com.lweiss01.paydirt.data.repository.GoalSettingsRepository
import com.lweiss01.paydirt.data.repository.PaymentRepository
import com.lweiss01.paydirt.domain.engine.PayoffEngine
import com.lweiss01.paydirt.domain.model.MomentumScore
import com.lweiss01.paydirt.domain.usecase.CalculatePayoffPlanUseCase
import com.lweiss01.paydirt.domain.usecase.GetCardsUseCase
import com.lweiss01.paydirt.domain.usecase.GetHomePaymentSummaryUseCase
import com.lweiss01.paydirt.domain.usecase.GetMonthlyGoalUseCase
import com.lweiss01.paydirt.domain.usecase.UpdateMonthlyGoalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `home state stays empty when there are no cards`() = runTest {
        val viewModel = buildViewModel(cards = emptyList(), payments = emptyList())

        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.cards.isEmpty())
        assertEquals(0.0, state.totalBalance, 0.0)
        assertNull(state.quickPlan)
        assertNull(state.recommendation)
        assertNull(state.behaviorState)
        assertNull(state.progress)
    }

    @Test
    fun `single manual card with unknown apr still yields reveal-ready recommendation`() = runTest {
        val viewModel = buildViewModel(cards = listOf(singleManualUnknownAprCard()), payments = emptyList())

        advanceUntilIdle()
        viewModel.showFirstRecommendationReveal()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(1, state.cards.size)
        assertEquals("Starter Visa", state.recommendation?.cardName)
        assertTrue(state.recommendation?.title?.contains("Starter Visa") == true)
        assertTrue(state.recommendation?.body?.contains("clearest next move") == true)
        assertEquals("APR not confirmed", com.lweiss01.paydirt.ui.components.aprTrustCopyForSource(state.cards.single().aprSource).badgeLabel)
        assertTrue(state.isShowingFirstRecommendationReveal)
    }

    @Test
    fun `first recommendation reveal can be dismissed without losing recommendation`() = runTest {
        val viewModel = buildViewModel(cards = listOf(singleManualUnknownAprCard()), payments = emptyList())

        advanceUntilIdle()
        viewModel.showFirstRecommendationReveal()
        advanceUntilIdle()
        viewModel.dismissFirstRecommendationReveal()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isShowingFirstRecommendationReveal)
        assertNotNull(state.recommendation)
        assertEquals("Starter Visa", state.recommendation?.cardName)
    }

    @Test
    fun `home state derives recommendation and default monthly goal from persisted cards`() = runTest {
        val viewModel = buildViewModel(cards = sampleCards(), payments = emptyList())

        advanceUntilIdle()
        val state = viewModel.uiState.value
        val behaviorState = requireNotNull(state.behaviorState)

        assertFalse(state.isLoading)
        assertNotNull(state.quickPlan)
        assertEquals("Capital", state.quickPlan?.recommendedTargetName)
        assertEquals("Capital", state.recommendation?.cardName)
        assertTrue(state.recommendation?.title?.contains("Capital") == true)
        assertEquals(MomentumScore.NONE, behaviorState.momentumScore)
        assertEquals(0.0, behaviorState.extraThisMonth, 0.001)
        assertEquals(50.0, behaviorState.monthlyGoal, 0.001)
        assertEquals(25.0, behaviorState.nextOpportunityAmount, 0.001)
        assertEquals("No extra payments yet this month.", state.progress?.headline)
        assertEquals("Momentum", state.progress?.momentumCallout?.title)
        assertTrue(state.progress?.momentumCallout?.body?.contains("first extra payment") == true)
        assertNull(state.progress?.recentPaymentCallout)
        assertEquals("Next opportunity", state.progress?.nextOpportunityCallout?.title)
        assertTrue(state.progress?.nextOpportunityCallout?.body?.contains("$25") == true)
    }

    @Test
    fun `home state shows this month payment progress from persisted payment summary`() = runTest {
        val payment = PaymentEntity(
            id = 1L,
            cardId = 2L,
            amount = 20.0,
            isExtraPayment = true,
            paidAt = System.currentTimeMillis(),
        )
        val viewModel = buildViewModel(cards = sampleCards(), payments = listOf(payment))

        advanceUntilIdle()
        val state = viewModel.uiState.value
        val behaviorState = requireNotNull(state.behaviorState)

        assertEquals(MomentumScore.BUILDING, behaviorState.momentumScore)
        assertEquals(20.0, behaviorState.totalExtraPayments, 0.001)
        assertEquals(20.0, behaviorState.extraThisMonth, 0.001)
        assertEquals(0.4f, behaviorState.goalProgress, 0.001f)
        assertEquals("\$20 of \$50 extra this month", state.progress?.paceLabel)
        assertEquals("Momentum", state.progress?.momentumCallout?.title)
        assertTrue(state.progress?.momentumCallout?.body?.contains("\$30") == true)
        assertEquals("Recent payment impact", state.progress?.recentPaymentCallout?.title)
        assertTrue(state.progress?.recentPaymentCallout?.body?.contains("\$20") == true)
        assertTrue(state.progress?.recentPaymentCallout?.body?.contains("trimmed about") == true)
        assertEquals("Next opportunity", state.progress?.nextOpportunityCallout?.title)
        assertTrue(state.progress?.nextOpportunityCallout?.body?.contains("\$5.00") == true)
        assertTrue(state.progress?.totalsLabel?.contains("saved so far") == true)
    }

    @Test
    fun `home state uses updated persisted monthly goal for progress framing`() = runTest {
        val payment = PaymentEntity(
            id = 1L,
            cardId = 2L,
            amount = 20.0,
            isExtraPayment = true,
            paidAt = System.currentTimeMillis(),
        )
        val goalDao = FakeGoalSettingsDao(GoalSettingsEntity(monthlyGoal = 125.0))
        val viewModel = buildViewModel(cards = sampleCards(), payments = listOf(payment), goalSettingsDao = goalDao)

        advanceUntilIdle()
        val state = viewModel.uiState.value
        val behaviorState = requireNotNull(state.behaviorState)

        assertEquals(125.0, behaviorState.monthlyGoal, 0.001)
        assertEquals(0.16f, behaviorState.goalProgress, 0.001f)
        assertEquals("\$20 of \$125 extra this month", state.progress?.paceLabel)
        assertTrue(state.progress?.momentumCallout?.body?.contains("\$105") == true)
    }

    @Test
    fun `home state refreshes when payment stream updates`() = runTest {
        val cardDao = FakeCardDao(sampleCards())
        val paymentDao = FakePaymentDao(emptyList())
        val viewModel = buildViewModel(cardDao = cardDao, paymentDao = paymentDao)

        advanceUntilIdle()
        val before = viewModel.uiState.value
        val beforeBehaviorState = requireNotNull(before.behaviorState)
        assertEquals(0.0, beforeBehaviorState.extraThisMonth, 0.001)
        assertEquals(MomentumScore.NONE, beforeBehaviorState.momentumScore)
        assertNull(before.progress?.recentPaymentCallout)
        assertEquals("Next opportunity", before.progress?.nextOpportunityCallout?.title)
        assertTrue(before.progress?.nextOpportunityCallout?.body?.contains("$25") == true)

        paymentDao.emitPayments(
            listOf(
                PaymentEntity(
                    id = 1L,
                    cardId = 2L,
                    amount = 20.0,
                    isExtraPayment = true,
                    paidAt = System.currentTimeMillis(),
                )
            )
        )

        advanceUntilIdle()
        val after = viewModel.uiState.value
        val afterBehaviorState = requireNotNull(after.behaviorState)

        assertEquals(MomentumScore.BUILDING, afterBehaviorState.momentumScore)
        assertEquals(20.0, afterBehaviorState.extraThisMonth, 0.001)
        assertEquals("You’re building this month’s payoff momentum.", after.progress?.headline)
        assertNotNull(after.progress?.recentPaymentCallout)
        assertNotNull(after.progress?.nextOpportunityCallout)
    }

    @Test
    fun `home state refreshes when persisted monthly goal updates`() = runTest {
        val goalDao = FakeGoalSettingsDao(GoalSettingsEntity(monthlyGoal = 50.0))
        val viewModel = buildViewModel(cards = sampleCards(), payments = emptyList(), goalSettingsDao = goalDao)

        advanceUntilIdle()
        assertTrue(requireNotNull(viewModel.uiState.value.progress?.momentumCallout?.body).contains("\$50"))

        goalDao.emit(GoalSettingsEntity(monthlyGoal = 125.0))
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        val updatedBehaviorState = requireNotNull(updatedState.behaviorState)
        assertEquals(125.0, updatedBehaviorState.monthlyGoal, 0.001)
        assertTrue(updatedState.progress?.momentumCallout?.body?.contains("\$125") == true)
    }

    @Test
    fun `saving monthly goal updates persisted goal and exits edit mode`() = runTest {
        val goalDao = FakeGoalSettingsDao(GoalSettingsEntity(monthlyGoal = 50.0))
        val viewModel = buildViewModel(cards = sampleCards(), payments = emptyList(), goalSettingsDao = goalDao)

        advanceUntilIdle()
        viewModel.beginGoalEdit()
        viewModel.updateGoalInput("125")
        viewModel.saveGoal()
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        val updatedBehaviorState = requireNotNull(updatedState.behaviorState)
        assertFalse(updatedState.isEditingGoal)
        assertEquals(125.0, updatedState.monthlyGoal, 0.001)
        assertEquals(125.0, updatedBehaviorState.monthlyGoal, 0.001)
        assertEquals(125.0, requireNotNull(goalDao.getGoalSettings(1L)).monthlyGoal, 0.001)
    }

    private fun buildViewModel(
        cards: List<CardEntity> = sampleCards(),
        payments: List<PaymentEntity> = emptyList(),
        cardDao: FakeCardDao = FakeCardDao(cards),
        paymentDao: FakePaymentDao = FakePaymentDao(payments),
        goalSettingsDao: FakeGoalSettingsDao = FakeGoalSettingsDao(),
    ): HomeViewModel {
        val cardRepository = CardRepository(cardDao)
        val paymentRepository = PaymentRepository(paymentDao)
        val goalSettingsRepository = GoalSettingsRepository(goalSettingsDao)
        return HomeViewModel(
            getCardsUseCase = GetCardsUseCase(cardRepository),
            getHomePaymentSummaryUseCase = GetHomePaymentSummaryUseCase(paymentRepository),
            calculatePayoffPlanUseCase = CalculatePayoffPlanUseCase(cardRepository),
            getMonthlyGoalUseCase = GetMonthlyGoalUseCase(goalSettingsRepository),
            updateMonthlyGoalUseCase = UpdateMonthlyGoalUseCase(goalSettingsRepository),
        )
    }

    private fun singleManualUnknownAprCard(): CardEntity = CardEntity(
        id = 10L,
        name = "Starter Visa",
        currentBalance = 1200.0,
        originalBalance = 1200.0,
        apr = 0.0,
        minPayment = 35.0,
        aprSource = "unknown",
        createdAt = 10L,
    )

    private fun sampleCards(): List<CardEntity> = listOf(
        CardEntity(
            id = 1L,
            name = "Chase",
            currentBalance = 4200.0,
            originalBalance = 5000.0,
            apr = 22.99,
            minPayment = 84.0,
            createdAt = 1L,
        ),
        CardEntity(
            id = 2L,
            name = "Capital",
            currentBalance = 1850.0,
            originalBalance = 2200.0,
            apr = 28.49,
            minPayment = 37.0,
            createdAt = 2L,
        ),
        CardEntity(
            id = 3L,
            name = "Citi",
            currentBalance = 6100.0,
            originalBalance = 7000.0,
            apr = 19.74,
            minPayment = 122.0,
            createdAt = 3L,
        ),
    )

    private class FakeCardDao(initialCards: List<CardEntity>) : CardDao {
        private val cards = MutableStateFlow(initialCards)

        override fun getActiveCards(): Flow<List<CardEntity>> =
            cards.map { entities -> entities.filterNot { it.isArchived }.sortedBy { it.createdAt } }

        override suspend fun getCardById(id: Long): CardEntity? = cards.value.firstOrNull { it.id == id }

        override suspend fun getCardByPlaidAccountId(accountId: String): CardEntity? =
            cards.value.firstOrNull { it.plaidAccountId == accountId }

        override suspend fun insertCard(card: CardEntity): Long {
            cards.value = cards.value + card
            return card.id
        }

        override suspend fun updateCard(card: CardEntity) {
            cards.value = cards.value.map { if (it.id == card.id) card else it }
        }

        override suspend fun updateBalance(id: Long, newBalance: Double) {
            cards.value = cards.value.map { entity ->
                if (entity.id == id) entity.copy(currentBalance = newBalance) else entity
            }
        }

        override suspend fun markItemNeedsReauth(itemId: String) {
            cards.value = cards.value.map { entity ->
                if (entity.plaidItemId == itemId) entity.copy(plaidNeedsReauth = true) else entity
            }
        }

        override suspend fun getCardsByItemId(itemId: String): List<CardEntity> =
            cards.value.filter { it.plaidItemId == itemId }

        override suspend fun updateFromPlaid(
            plaidAccountId: String,
            balance: Double,
            minPayment: Double,
            apr: Double,
            aprSource: String,
            refreshedAt: Long,
            statementBalance: Double?,
            nextPaymentDueDate: Long?,
            dueDate: Int?,
        ) {
            cards.value = cards.value.map { entity ->
                if (entity.plaidAccountId == plaidAccountId) {
                    entity.copy(
                        currentBalance = balance,
                        minPayment = minPayment,
                        apr = apr,
                        aprSource = aprSource,
                        plaidLastRefreshed = refreshedAt,
                        statementBalance = statementBalance,
                        nextPaymentDueDate = nextPaymentDueDate,
                        dueDate = dueDate,
                    )
                } else {
                    entity
                }
            }
        }

        override suspend fun archiveCard(id: Long) {
            cards.value = cards.value.map { if (it.id == id) it.copy(isArchived = true) else it }
        }

        override suspend fun deleteCard(card: CardEntity) {
            cards.value = cards.value.filterNot { it.id == card.id }
        }

        override fun getTotalBalance(): Flow<Double?> =
            cards.map { entities -> entities.filterNot { it.isArchived }.sumOf { it.currentBalance } }

        override fun getActiveCardCount(): Flow<Int> =
            cards.map { entities -> entities.count { !it.isArchived } }
    }

    private class FakePaymentDao(initialPayments: List<PaymentEntity>) : PaymentDao {
        private val payments = MutableStateFlow(initialPayments)

        fun emitPayments(newPayments: List<PaymentEntity>) {
            payments.value = newPayments
        }

        override fun getPaymentsForCard(cardId: Long): Flow<List<PaymentEntity>> =
            payments.map { entities -> entities.filter { it.cardId == cardId }.sortedByDescending { it.paidAt } }

        override fun getRecentPayments(limit: Int): Flow<List<PaymentEntity>> =
            payments.map { entities -> entities.sortedByDescending { it.paidAt }.take(limit) }

        override fun getTotalPaidForCard(cardId: Long): Flow<Double?> =
            payments.map { entities -> entities.filter { it.cardId == cardId }.sumOf { it.amount } }

        override suspend fun insertPayment(payment: PaymentEntity): Long {
            payments.value = (payments.value + payment).sortedByDescending { it.paidAt }
            return payment.id
        }

        override suspend fun deletePayment(payment: PaymentEntity) {
            payments.value = payments.value.filterNot { it.id == payment.id }
        }

        override suspend fun deletePaymentById(id: Long) {
            payments.value = payments.value.filterNot { it.id == id }
        }
    }

    private class FakeGoalSettingsDao(initialSettings: GoalSettingsEntity? = null) : GoalSettingsDao {
        private val settings = MutableStateFlow(initialSettings)

        fun emit(newSettings: GoalSettingsEntity?) {
            settings.value = newSettings
        }

        override fun observeGoalSettings(id: Long): Flow<GoalSettingsEntity?> = settings

        override suspend fun getGoalSettings(id: Long): GoalSettingsEntity? = settings.value

        override suspend fun upsertGoalSettings(settings: GoalSettingsEntity) {
            this.settings.value = settings
        }
    }

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = StandardTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
