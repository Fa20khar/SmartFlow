package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        SupplierEntity::class,
        PurchaseRequestEntity::class,
        RequestItemEntity::class,
        ApprovalRecordEntity::class,
        RfqEntity::class,
        QuotationEntity::class,
        PurchaseOrderEntity::class,
        GoodsReceiptEntity::class,
        InventoryItemEntity::class,
        StockMovementEntity::class,
        InvoiceEntity::class,
        BudgetEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun procurementDao(): ProcurementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartflow_procurement_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
