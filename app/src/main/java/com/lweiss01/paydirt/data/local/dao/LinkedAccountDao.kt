package com.lweiss01.paydirt.data.local.dao

import androidx.room.*
import com.lweiss01.paydirt.data.local.entity.LinkedAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkedAccountDao {

    @Query("SELECT * FROM linked_accounts ORDER BY linkedAt ASC")
    fun getAllLinkedAccounts(): Flow<List<LinkedAccountEntity>>

    @Query("SELECT * FROM linked_accounts WHERE itemId = :itemId")
    suspend fun getLinkedAccount(itemId: String): LinkedAccountEntity?

    @Query("SELECT COUNT(*) FROM linked_accounts")
    fun getLinkedAccountCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedAccount(account: LinkedAccountEntity)

    @Query("UPDATE linked_accounts SET needsReauth = 1 WHERE itemId = :itemId")
    suspend fun markNeedsReauth(itemId: String)

    @Query("UPDATE linked_accounts SET needsReauth = 0, lastRefreshed = :timestamp WHERE itemId = :itemId")
    suspend fun markRefreshed(itemId: String, timestamp: Long)

    @Query("DELETE FROM linked_accounts WHERE itemId = :itemId")
    suspend fun deleteLinkedAccount(itemId: String)

    @Query("SELECT * FROM linked_accounts WHERE needsReauth = 1")
    suspend fun getAccountsNeedingReauth(): List<LinkedAccountEntity>
}
