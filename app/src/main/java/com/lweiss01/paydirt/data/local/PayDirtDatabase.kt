package com.lweiss01.paydirt.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lweiss01.paydirt.data.local.dao.CardDao
import com.lweiss01.paydirt.data.local.dao.LinkedAccountDao
import com.lweiss01.paydirt.data.local.dao.PaymentDao
import com.lweiss01.paydirt.data.local.entity.CardEntity
import com.lweiss01.paydirt.data.local.entity.LinkedAccountEntity
import com.lweiss01.paydirt.data.local.entity.PaymentEntity

@Database(
    entities = [CardEntity::class, PaymentEntity::class, LinkedAccountEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class PayDirtDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun paymentDao(): PaymentDao
    abstract fun linkedAccountDao(): LinkedAccountDao

    companion object {
        const val DATABASE_NAME = "paydirt.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN plaidAccountId TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN plaidItemId TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN plaidInstitutionName TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN plaidLastRefreshed INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN plaidNeedsReauth INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cards ADD COLUMN aprSource TEXT")
                db.execSQL("ALTER TABLE cards ADD COLUMN statementBalance REAL")
                db.execSQL("ALTER TABLE cards ADD COLUMN nextPaymentDueDate INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN dueDate INTEGER")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS linked_accounts (
                        itemId TEXT NOT NULL PRIMARY KEY,
                        institutionId TEXT NOT NULL,
                        institutionName TEXT NOT NULL,
                        accessToken TEXT NOT NULL,
                        linkedAt INTEGER NOT NULL,
                        lastRefreshed INTEGER,
                        needsReauth INTEGER NOT NULL DEFAULT 0,
                        consentExpiresAt INTEGER
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
