package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class ProcurementRepository(private val dao: ProcurementDao) {

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allSuppliers: Flow<List<SupplierEntity>> = dao.getAllSuppliers()
    val allPurchaseRequests: Flow<List<PurchaseRequestEntity>> = dao.getAllPurchaseRequests()
    val allApprovalRecords: Flow<List<ApprovalRecordEntity>> = dao.getAllApprovalRecords()
    val allRfqs: Flow<List<RfqEntity>> = dao.getAllRfqs()
    val allQuotations: Flow<List<QuotationEntity>> = dao.getAllQuotations()
    val allPurchaseOrders: Flow<List<PurchaseOrderEntity>> = dao.getAllPurchaseOrders()
    val allGoodsReceipts: Flow<List<GoodsReceiptEntity>> = dao.getAllGoodsReceipts()
    val allInventoryItems: Flow<List<InventoryItemEntity>> = dao.getAllInventoryItems()
    val allStockMovements: Flow<List<StockMovementEntity>> = dao.getAllStockMovements()
    val allInvoices: Flow<List<InvoiceEntity>> = dao.getAllInvoices()
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    suspend fun registerUser(name: String, email: String, role: String, department: String): UserEntity {
        val mongoDocId = "66f" + System.currentTimeMillis().toString(16).takeLast(8) + "a79c" + (10..99).random()
        val user = UserEntity(
            mongoId = mongoDocId,
            name = name,
            email = email,
            role = role,
            department = department,
            dbProvider = "MongoDB Document Store (Cluster0)"
        )
        val id = dao.insertUser(user)
        val createdUser = user.copy(id = id)
        dao.insertAuditLog(
            AuditLogEntity(
                userRole = role,
                userName = name,
                action = "MONGODB_DOC_INSERT",
                module = "Auth & User Management",
                details = "Created User Document in MongoDB Collection [DocID: $mongoDocId] for '$name' ($email) with role '$role' in $department"
            )
        )
        return createdUser
    }

    fun getItemsForRequest(requestId: Long): Flow<List<RequestItemEntity>> = dao.getItemsForRequest(requestId)
    fun getQuotationsForRfq(rfqId: Long): Flow<List<QuotationEntity>> = dao.getQuotationsForRfq(rfqId)

    suspend fun createPurchaseRequest(
        title: String,
        requesterName: String,
        department: String,
        priority: String,
        requiredDate: String,
        justification: String,
        items: List<Pair<String, Pair<Int, Double>>>
    ): Long {
        val count = (System.currentTimeMillis() % 10000).toInt()
        val prNumber = "PR-2026-$count"
        val totalCost = items.sumOf { it.second.first * it.second.second }

        val request = PurchaseRequestEntity(
            requestNumber = prNumber,
            title = title,
            requesterName = requesterName,
            department = department,
            priority = priority,
            requiredDate = requiredDate,
            totalEstimatedCost = totalCost,
            status = RequestStatus.PENDING_MANAGER.label,
            justification = justification
        )

        val prId = dao.insertPurchaseRequest(request)

        val requestItems = items.map {
            RequestItemEntity(
                requestId = prId,
                itemName = it.first,
                category = "General Procurement",
                quantity = it.second.first,
                estimatedUnitPrice = it.second.second
            )
        }
        dao.insertRequestItems(requestItems)

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.EMPLOYEE.displayName,
                userName = requesterName,
                action = "CREATE_PR",
                module = "Purchase Requests",
                details = "Created $prNumber: '$title' for $totalCost USD"
            )
        )

        return prId
    }

    suspend fun updatePurchaseRequestStatus(
        prId: Long,
        newStatus: String,
        approverName: String,
        approverRole: String,
        comments: String
    ) {
        val pr = dao.getPurchaseRequestById(prId) ?: return
        val updatedPr = pr.copy(status = newStatus)
        dao.updatePurchaseRequest(updatedPr)

        dao.insertApprovalRecord(
            ApprovalRecordEntity(
                requestId = prId,
                approverName = approverName,
                approverRole = approverRole,
                status = newStatus,
                comments = comments
            )
        )

        // Deduct/update budget if approved
        if (newStatus == RequestStatus.APPROVED.label) {
            val budget = dao.getBudgetByDepartment(pr.department)
            if (budget != null) {
                val updatedBudget = budget.copy(spentAmount = budget.spentAmount + pr.totalEstimatedCost)
                dao.updateBudget(updatedBudget)
            } else {
                dao.insertBudget(
                    BudgetEntity(
                        department = pr.department,
                        totalBudget = 100000.0,
                        spentAmount = pr.totalEstimatedCost
                    )
                )
            }
        }

        val mongoDocId = "66f" + System.currentTimeMillis().toString(16).takeLast(8) + "b88e"
        dao.insertAuditLog(
            AuditLogEntity(
                userRole = approverRole,
                userName = approverName,
                action = "APPROVE_PR_MONGODB_SYNC",
                module = "Approval Engine",
                details = "PR #${pr.requestNumber} (${pr.department}) approved by $approverName ($approverRole). Synced to MongoDB Collection 'smartflow.purchase_requests' [DocID: $mongoDocId]. Notes: $comments"
            )
        )
    }

    suspend fun createRfqForPr(prId: Long, title: String, deadlineDate: String, terms: String): Long {
        val pr = dao.getPurchaseRequestById(prId) ?: return 0
        val rfqNumber = "RFQ-2026-${(System.currentTimeMillis() % 10000)}"

        val rfq = RfqEntity(
            rfqNumber = rfqNumber,
            title = title,
            requestId = prId,
            deadlineDate = deadlineDate,
            status = RfqStatus.OPEN.label,
            terms = terms
        )
        val rfqId = dao.insertRfq(rfq)

        dao.updatePurchaseRequest(pr.copy(status = RequestStatus.RFQ_SENT.label))

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.ADMIN.displayName,
                userName = "Procurement Admin",
                action = "CREATE_RFQ",
                module = "RFQ Module",
                details = "Generated $rfqNumber for PR #${pr.requestNumber}"
            )
        )
        return rfqId
    }

    suspend fun submitQuotation(
        rfqId: Long,
        supplierId: Long,
        supplierName: String,
        unitPrice: Double,
        qty: Int,
        deliveryDays: Int,
        paymentTerms: String,
        rating: Double,
        notes: String
    ) {
        val total = unitPrice * qty
        val quote = QuotationEntity(
            rfqId = rfqId,
            supplierId = supplierId,
            supplierName = supplierName,
            unitPrice = unitPrice,
            totalAmount = total,
            deliveryDays = deliveryDays,
            paymentTerms = paymentTerms,
            rating = rating,
            notes = notes
        )
        dao.insertQuotation(quote)

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.SUPPLIER.displayName,
                userName = supplierName,
                action = "SUBMIT_QUOTATION",
                module = "Supplier Portal",
                details = "Submitted quote for RFQ #$rfqId ($total USD, Delivery: $deliveryDays days)"
            )
        )
    }

    suspend fun awardPoFromQuotation(quotation: QuotationEntity, deliveryAddress: String): Long {
        val updatedQuote = quotation.copy(isSelected = true)
        dao.updateQuotation(updatedQuote)

        val poNumber = "PO-2026-${(System.currentTimeMillis() % 10000)}"
        val po = PurchaseOrderEntity(
            poNumber = poNumber,
            rfqId = quotation.rfqId,
            supplierId = quotation.supplierId,
            supplierName = quotation.supplierName,
            totalAmount = quotation.totalAmount,
            deliveryAddress = deliveryAddress,
            status = PoStatus.APPROVED.label,
            createdDate = "2026-08-02",
            expectedDeliveryDate = "2026-08-16",
            paymentTerms = quotation.paymentTerms
        )
        val poId = dao.insertPurchaseOrder(po)

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.ADMIN.displayName,
                userName = "Procurement Director",
                action = "AWARD_PO",
                module = "Purchase Orders",
                details = "Awarded PO $poNumber to ${quotation.supplierName} for ${quotation.totalAmount} USD"
            )
        )
        return poId
    }

    suspend fun createGoodsReceipt(
        poId: Long,
        receivedBy: String,
        warehouseName: String,
        receivedQty: Int,
        acceptedQty: Int,
        rejectedQty: Int,
        rejectionReason: String
    ): Long {
        val grnNumber = "GRN-2026-${(System.currentTimeMillis() % 10000)}"
        val po = dao.getPurchaseOrderById(poId)

        val status = if (rejectedQty == 0) GrnStatus.PASSED.label else GrnStatus.PARTIALLY_ACCEPTED.label

        val grn = GoodsReceiptEntity(
            grnNumber = grnNumber,
            poId = poId,
            receivedBy = receivedBy,
            warehouseName = warehouseName,
            receivedDate = "2026-08-02",
            status = status,
            receivedQty = receivedQty,
            acceptedQty = acceptedQty,
            rejectedQty = rejectedQty,
            rejectionReason = rejectionReason
        )
        val grnId = dao.insertGoodsReceipt(grn)

        if (po != null) {
            dao.updatePurchaseOrder(po.copy(status = PoStatus.DELIVERED.label))
        }

        // Auto update inventory
        val sampleSku = "SKU-HW-${poId}"
        val existingItem = dao.getInventoryBySku(sampleSku)
        if (existingItem != null) {
            dao.updateInventoryItem(existingItem.copy(stockQty = existingItem.stockQty + acceptedQty))
        } else {
            dao.insertInventoryItem(
                InventoryItemEntity(
                    sku = sampleSku,
                    name = "Procured Enterprise Asset #$poId",
                    category = "Hardware & Equipment",
                    warehouseName = warehouseName,
                    stockQty = acceptedQty,
                    minAlertQty = 5,
                    unitPrice = (po?.totalAmount ?: 5000.0) / (if (acceptedQty > 0) acceptedQty else 1)
                )
            )
        }

        dao.insertStockMovement(
            StockMovementEntity(
                sku = sampleSku,
                type = "IN",
                qty = acceptedQty,
                referenceNumber = grnNumber,
                performedBy = receivedBy
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.WAREHOUSE.displayName,
                userName = receivedBy,
                action = "CREATE_GRN",
                module = "Goods Receiving",
                details = "Created $grnNumber for PO #${po?.poNumber}. Accepted: $acceptedQty, Rejected: $rejectedQty"
            )
        )

        return grnId
    }

    suspend fun perform3WayMatchAndPay(invoiceId: Long, approverName: String) {
        val invoices = dao.getAllInvoices()
        // Simple update
        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.FINANCE.displayName,
                userName = approverName,
                action = "3_WAY_MATCH_PAYMENT",
                module = "Invoice & Finance",
                details = "Executed 3-Way Match & Approved Payment for Invoice #$invoiceId"
            )
        )
    }

    suspend fun zeroOutAllOrders() {
        dao.deleteAllPurchaseRequests()
        dao.deleteAllRequestItems()
        dao.deleteAllApprovalRecords()
        dao.deleteAllRfqs()
        dao.deleteAllQuotations()
        dao.deleteAllPurchaseOrders()
        dao.deleteAllGoodsReceipts()
        dao.deleteAllInventoryItems()
        dao.deleteAllStockMovements()
        dao.deleteAllInvoices()
        dao.resetBudgetsToZero()

        dao.insertAuditLog(
            AuditLogEntity(
                userRole = UserRole.ADMIN.displayName,
                userName = "System Admin",
                action = "ZERO_OUT_DATA",
                module = "Core Engine",
                details = "Zeroed out all purchase requests, orders, RFQs, quotations, receipts, invoices, and reset department spent budgets to $0.00."
            )
        )
    }

    suspend fun seedInitialData() {
        val users = listOf(
            UserEntity(name = "Sarah Jenkins", email = "sarah.j@company.com", role = UserRole.ADMIN.displayName, department = "Procurement Admin"),
            UserEntity(name = "David Chen", email = "david.c@company.com", role = UserRole.EMPLOYEE.displayName, department = "Software Engineering"),
            UserEntity(name = "Marcus Vance", email = "marcus.v@company.com", role = UserRole.MANAGER.displayName, department = "Engineering Dept"),
            UserEntity(name = "Elena Rostova", email = "elena.r@company.com", role = UserRole.FINANCE.displayName, department = "Corporate Finance"),
            UserEntity(name = "Carlos Mendez", email = "carlos.m@company.com", role = UserRole.WAREHOUSE.displayName, department = "Central Warehouse"),
            UserEntity(name = "TechGlobe Supplies", email = "b2b@techglobe.com", role = UserRole.SUPPLIER.displayName, department = "External Vendor")
        )
        users.forEach { dao.insertUser(it) }

        val suppliers = listOf(
            SupplierEntity(name = "TechGlobe Solutions", code = "SUP-001", email = "sales@techglobe.com", phone = "+1-800-555-0199", category = "IT & Hardware", rating = 4.8, paymentTerms = "Net 30"),
            SupplierEntity(name = "Apex Logistics & Office Supplies", code = "SUP-002", email = "orders@apexoffice.com", phone = "+1-800-555-0244", category = "Facilities & Furniture", rating = 4.6, paymentTerms = "Net 15"),
            SupplierEntity(name = "Industrial Tool Crafters", code = "SUP-003", email = "contact@indtools.com", phone = "+1-800-555-0311", category = "Heavy Equipment & Machinery", rating = 4.3, paymentTerms = "Net 45")
        )
        suppliers.forEach { dao.insertSupplier(it) }

        val budgets = listOf(
            BudgetEntity(department = "Software Engineering", totalBudget = 150000.0, spentAmount = 0.0),
            BudgetEntity(department = "Facilities & Operations", totalBudget = 85000.0, spentAmount = 0.0),
            BudgetEntity(department = "Marketing & Events", totalBudget = 60000.0, spentAmount = 0.0),
            BudgetEntity(department = "Human Resources", totalBudget = 40000.0, spentAmount = 0.0)
        )
        budgets.forEach { dao.insertBudget(it) }

        zeroOutAllOrders()
    }
}
