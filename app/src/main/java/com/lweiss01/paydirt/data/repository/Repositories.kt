package com.lweiss01.paydirt.data.repository

import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.PaymentDao
import com.lweiss01.paydirt.data.local.mapper.toDomain
import com.lweiss01.paydirt.data.local.mapper.toEntity
import com.lweiss01.paydirt.domain.model.Card
import com.lweiss01.paydirt.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao
) {
    fun getActiveCards(): Flow<List<Card>> =
        cardDao.getActiveCards().map { list -> list.map { it.toDomain() } }

    suspend fun getCardById(id: Long): Card? =
        cardDao.getCardById(id)?.toDomain()

    suspend fun insertCard(card: Card): Long =
        cardDao.insertCard(card.toEntity())

    suspend fun updateCard(card: Card) =
        cardDao.updateCard(card.toEntity())

    suspend fun updateBalance(id: Long, newBalance: Double) =
        cardDao.updateBalance(id, newBalance)

    suspend fun archiveCard(id: Long) =
        cardDao.archiveCard(id)

    fun getTotalBalance(): Flow<Double> =
        cardDao.getTotalBalance().map { it ?: 0.0 }

    fun getActiveCardCount(): Flow<Int> =
        cardDao.getActiveCardCount()
}

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {
    fun getPaymentsForCard(cardId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForCard(cardId).map { list -> list.map { it.toDomain() } }

    fun getRecentPayments(limit: Int = 20): Flow<List<Payment>> =
        paymentDao.getRecentPayments(limit).map { list -> list.map { it.toDomain() } }

    fun getTotalPaidForCard(cardId: Long): Flow<Double> =
        paymentDao.getTotalPaidForCard(cardId).map { it ?: 0.0 }

    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment.toEntity())

    suspend fun deletePaymentById(id: Long) =
        paymentDao.deletePaymentById(id)
}
