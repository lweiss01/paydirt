package com.lweiss01.paydirt.data.local.dao

import androidx.room.*
import com.lweiss01.paydirt.data.local.entity.CardEntity
import com.lweiss01.paydirt.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun getActiveCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE plaidAccountId = :accountId LIMIT 1")
    suspend fun getCardByPlaidAccountId(accountId: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("UPDATE cards SET currentBalance = :newBalance WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Double)

    @Query("UPDATE cards SET plaidNeedsReauth = 1 WHERE plaidItemId = :itemId")
    suspend fun markItemNeedsReauth(itemId: String)

    @Query("SELECT * FROM cards WHERE plaidItemId = :itemId")
    suspend fun getCardsByItemId(itemId: String): List<CardEntity>

    @Query(
        """
        UPDATE cards
        SET currentBalance = :balance,
            minPayment = :minPayment,
            apr = :apr,
            aprSource = :aprSource,
            plaidLastRefreshed = :refreshedAt,
            statementBalance = :statementBalance,
            nextPaymentDueDate = :nextPaymentDueDate,
            dueDate = :dueDate
        WHERE plaidAccountId = :plaidAccountId
        """
    )
    suspend fun updateFromPlaid(
        plaidAccountId: String,
        balance: Double,
        minPayment: Double,
        apr: Double,
        aprSource: String,
        refreshedAt: Long,
        statementBalance: Double?,
        nextPaymentDueDate: Long?,
        dueDate: Int?,
    )

    @Query("UPDATE cards SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCard(id: Long)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT SUM(currentBalance) FROM cards WHERE isArchived = 0")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM cards WHERE isArchived = 0")
    fun getActiveCardCount(): Flow<Int>
}

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE cardId = :cardId ORDER BY paidAt DESC")
    fun getPaymentsForCard(cardId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY paidAt DESC LIMIT :limit")
    fun getRecentPayments(limit: Int = 20): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM payments WHERE cardId = :cardId")
    fun getTotalPaidForCard(cardId: Long): Flow<Double?>

    @Insert
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)
}
