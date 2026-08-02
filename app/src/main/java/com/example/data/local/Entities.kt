package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    ADMIN("Admin", 0xFF6366F1),
    EMPLOYEE("Employee", 0xFF0284C7),
    MANAGER("Manager", 0xFF0D9488),
    FINANCE("Finance User", 0xFFD97706),
    WAREHOUSE("Warehouse User", 0xFF8B5CF6),
    SUPPLIER("Supplier", 0xFF10B981)
}

enum class RequestStatus(val label: String) {
    DRAFT("Draft"),
    PENDING_MANAGER("Pending Manager"),
    PENDING_FINANCE("Pending Finance"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    RFQ_SENT("RFQ Sent"),
    PO_CREATED("PO Created"),
    COMPLETED("Purchase Completed")
}

enum class PriorityLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class RfqStatus(val label: String) {
    OPEN("Open"),
    QUOTATIONS_RECEIVED("Quotations Received"),
    EVALUATED("Evaluated"),
    AWARDED("Awarded"),
    CLOSED("Closed")
}

enum class PoStatus(val label: String) {
    DRAFT("Draft"),
    SENT_TO_SUPPLIER("Sent to Supplier"),
    APPROVED("Approved"),
    PARTIALLY_DELIVERED("Partially Delivered"),
    DELIVERED("Fully Delivered"),
    CANCELLED("Cancelled")
}

enum class GrnStatus(val label: String) {
    PENDING_INSPECTION("Pending Inspection"),
    PASSED("Passed"),
    REJECTED("Rejected"),
    PARTIALLY_ACCEPTED("Partially Accepted")
}

enum class InvoiceStatus(val label: String) {
    SUBMITTED("Submitted"),
    MATCHED_OK("3-Way Matched"),
    DISCREPANCY("Discrepancy Flagged"),
    APPROVED_FOR_PAYMENT("Approved for Payment"),
    PAID("Paid")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mongoId: String = "66f" + System.currentTimeMillis().toString(16).takeLast(8) + "a79c20",
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val dbProvider: String = "MongoDB Document Store (Cluster0)"
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val email: String,
    val phone: String,
    val category: String,
    val rating: Double,
    val status: String = "Active",
    val paymentTerms: String = "Net 30"
)

@Entity(tableName = "purchase_requests")
data class PurchaseRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestNumber: String,
    val title: String,
    val requesterName: String,
    val department: String,
    val priority: String,
    val requiredDate: String,
    val totalEstimatedCost: Double,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val justification: String
)

@Entity(tableName = "request_items")
data class RequestItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val itemName: String,
    val category: String,
    val quantity: Int,
    val estimatedUnitPrice: Double
)

@Entity(tableName = "approval_records")
data class ApprovalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: Long,
    val approverName: String,
    val approverRole: String,
    val status: String,
    val comments: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rfqs")
data class RfqEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rfqNumber: String,
    val title: String,
    val requestId: Long,
    val deadlineDate: String,
    val status: String,
    val terms: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quotations")
data class QuotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rfqId: Long,
    val supplierId: Long,
    val supplierName: String,
    val unitPrice: Double,
    val totalAmount: Double,
    val deliveryDays: Int,
    val paymentTerms: String,
    val rating: Double,
    val isSelected: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "purchase_orders")
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poNumber: String,
    val rfqId: Long,
    val supplierId: Long,
    val supplierName: String,
    val totalAmount: Double,
    val deliveryAddress: String,
    val status: String,
    val createdDate: String,
    val expectedDeliveryDate: String,
    val paymentTerms: String
)

@Entity(tableName = "goods_receipts")
data class GoodsReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grnNumber: String,
    val poId: Long,
    val receivedBy: String,
    val warehouseName: String,
    val receivedDate: String,
    val status: String,
    val receivedQty: Int,
    val acceptedQty: Int,
    val rejectedQty: Int,
    val rejectionReason: String = ""
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val category: String,
    val warehouseName: String,
    val stockQty: Int,
    val minAlertQty: Int,
    val unitPrice: Double
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val type: String, // IN, OUT, TRANSFER
    val qty: Int,
    val referenceNumber: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val poId: Long,
    val grnId: Long,
    val supplierName: String,
    val invoiceDate: String,
    val poAmount: Double,
    val grnValue: Double,
    val invoiceAmount: Double,
    val status: String,
    val is3WayMatched: Boolean,
    val discrepancyNotes: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val department: String,
    val totalBudget: Double,
    val spentAmount: Double,
    val fiscalYear: String = "FY 2026"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userRole: String,
    val userName: String,
    val action: String,
    val module: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
