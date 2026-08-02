package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcurementDao {

    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    // Purchase Requests
    @Query("SELECT * FROM purchase_requests ORDER BY id DESC")
    fun getAllPurchaseRequests(): Flow<List<PurchaseRequestEntity>>

    @Query("SELECT * FROM purchase_requests WHERE id = :id")
    suspend fun getPurchaseRequestById(id: Long): PurchaseRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseRequest(request: PurchaseRequestEntity): Long

    @Update
    suspend fun updatePurchaseRequest(request: PurchaseRequestEntity)

    // Request Items
    @Query("SELECT * FROM request_items WHERE requestId = :requestId")
    fun getItemsForRequest(requestId: Long): Flow<List<RequestItemEntity>>

    @Query("SELECT * FROM request_items WHERE requestId = :requestId")
    suspend fun getItemsForRequestList(requestId: Long): List<RequestItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestItem(item: RequestItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestItems(items: List<RequestItemEntity>)

    // Approvals
    @Query("SELECT * FROM approval_records ORDER BY timestamp DESC")
    fun getAllApprovalRecords(): Flow<List<ApprovalRecordEntity>>

    @Query("SELECT * FROM approval_records WHERE requestId = :requestId ORDER BY timestamp ASC")
    fun getApprovalsForRequest(requestId: Long): Flow<List<ApprovalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalRecord(approval: ApprovalRecordEntity)

    // RFQs
    @Query("SELECT * FROM rfqs ORDER BY id DESC")
    fun getAllRfqs(): Flow<List<RfqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRfq(rfq: RfqEntity): Long

    @Update
    suspend fun updateRfq(rfq: RfqEntity)

    // Quotations
    @Query("SELECT * FROM quotations WHERE rfqId = :rfqId ORDER BY totalAmount ASC")
    fun getQuotationsForRfq(rfqId: Long): Flow<List<QuotationEntity>>

    @Query("SELECT * FROM quotations ORDER BY id DESC")
    fun getAllQuotations(): Flow<List<QuotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity): Long

    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    // Purchase Orders
    @Query("SELECT * FROM purchase_orders ORDER BY id DESC")
    fun getAllPurchaseOrders(): Flow<List<PurchaseOrderEntity>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getPurchaseOrderById(id: Long): PurchaseOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrder(po: PurchaseOrderEntity): Long

    @Update
    suspend fun updatePurchaseOrder(po: PurchaseOrderEntity)

    // Goods Receipts
    @Query("SELECT * FROM goods_receipts ORDER BY id DESC")
    fun getAllGoodsReceipts(): Flow<List<GoodsReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoodsReceipt(grn: GoodsReceiptEntity): Long

    // Inventory
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE sku = :sku LIMIT 1")
    suspend fun getInventoryBySku(sku: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    // Stock Movements
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    // Budgets
    @Query("SELECT * FROM budgets ORDER BY department ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE LOWER(department) = LOWER(:dept) LIMIT 1")
    suspend fun getBudgetByDepartment(dept: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Clear / Zero Out
    @Query("DELETE FROM purchase_requests")
    suspend fun deleteAllPurchaseRequests()

    @Query("DELETE FROM request_items")
    suspend fun deleteAllRequestItems()

    @Query("DELETE FROM approval_records")
    suspend fun deleteAllApprovalRecords()

    @Query("DELETE FROM rfqs")
    suspend fun deleteAllRfqs()

    @Query("DELETE FROM quotations")
    suspend fun deleteAllQuotations()

    @Query("DELETE FROM purchase_orders")
    suspend fun deleteAllPurchaseOrders()

    @Query("DELETE FROM goods_receipts")
    suspend fun deleteAllGoodsReceipts()

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllInventoryItems()

    @Query("DELETE FROM stock_movements")
    suspend fun deleteAllStockMovements()

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()

    @Query("UPDATE budgets SET spentAmount = 0.0")
    suspend fun resetBudgetsToZero()
}
