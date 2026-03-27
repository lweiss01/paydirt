package com.lweiss01.paydirt.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val GOAL_SETTINGS_SINGLETON_ID = 1L
const val DEFAULT_MONTHLY_GOAL = 50.0

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val currentBalance: Double,
    val originalBalance: Double,
    val apr: Double,
    val minPayment: Double,
    val colorTag: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val plaidAccountId: String? = null,
    val plaidItemId: String? = null,
    val plaidInstitutionName: String? = null,
    val plaidLastRefreshed: Long? = null,
    val plaidNeedsReauth: Boolean = false,
    val aprSource: String? = null,
    val statementBalance: Double? = null,
    val nextPaymentDueDate: Long? = null,
    val dueDate: Int? = null,
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("cardId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val amount: Double,
    val isExtraPayment: Boolean = false,
    val note: String? = null,
    val paidAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "linked_accounts")
data class LinkedAccountEntity(
    @PrimaryKey
    val itemId: String,
    val institutionId: String,
    val institutionName: String,
    val accessToken: String,
    val linkedAt: Long = System.currentTimeMillis(),
    val lastRefreshed: Long? = null,
    val needsReauth: Boolean = false,
    val consentExpiresAt: Long? = null,
)

@Entity(tableName = "goal_settings")
data class GoalSettingsEntity(
    @PrimaryKey
    val id: Long = GOAL_SETTINGS_SINGLETON_ID,
    val monthlyGoal: Double = DEFAULT_MONTHLY_GOAL,
)
