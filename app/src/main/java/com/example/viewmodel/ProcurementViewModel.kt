package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.DatabaseConfig
import com.example.data.local.*
import com.example.data.remote.GeminiProcurementAssistant
import com.example.data.repository.ProcurementRepository
import com.example.service.BudgetAlertNotification
import com.example.service.BudgetCheckBackgroundService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProcurementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProcurementRepository
    private val geminiAssistant = GeminiProcurementAssistant()

    // Real-time Budget Limit Alert Notifications from Background Service
    val realTimeBudgetAlerts: StateFlow<List<BudgetAlertNotification>> = BudgetCheckBackgroundService.realtimeAlerts

    init {
        // Initialize MongoDB Atlas connection configuration from MONGODB_URI in .env / BuildConfig
        DatabaseConfig.initializeStartupConnection()

        val db = AppDatabase.getDatabase(application)
        repository = ProcurementRepository(db.procurementDao())

        // Check if database needs initial seeding
        viewModelScope.launch {
            repository.allPurchaseRequests.firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    repository.seedInitialData()
                }
            } ?: run {
                repository.seedInitialData()
            }
        }

        // Start background budget limit evaluation monitoring loop
        viewModelScope.launch {
            combine(repository.allPurchaseRequests, repository.allBudgets) { prs, bds ->
                prs to bds
            }.collect { (prs, bds) ->
                BudgetCheckBackgroundService.evaluateAllRequests(getApplication(), prs, bds)
            }
        }
    }

    // Role & User state
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Unique Sync Loader state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncTitle = MutableStateFlow("MongoDB Cluster Sync")
    val syncTitle: StateFlow<String> = _syncTitle.asStateFlow()

    private val _syncSubtitle = MutableStateFlow("Synchronizing SmartFlow 3-Way Audit Trail...")
    val syncSubtitle: StateFlow<String> = _syncSubtitle.asStateFlow()

    // Unified Global Sync Loader Helper
    private suspend fun <T> runWithSyncLoader(
        title: String,
        subtitle: String,
        delayMs: Long = 900,
        block: suspend () -> T
    ): T {
        _syncTitle.value = title
        _syncSubtitle.value = subtitle
        _isSyncing.value = true
        return try {
            val result = block()
            if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
            result
        } finally {
            _isSyncing.value = false
        }
    }

    fun triggerSyncLoader(
        title: String = "MongoDB Cluster Sync",
        subtitle: String = "Synchronizing SmartFlow Enterprise Ledger..."
    ) {
        viewModelScope.launch {
            runWithSyncLoader(title, subtitle, delayMs = 1200) {}
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun loginUser(user: UserEntity) {
        _currentUser.value = user
        val matchedRole = UserRole.entries.firstOrNull { it.displayName.equals(user.role, ignoreCase = true) }
            ?: UserRole.EMPLOYEE
        _currentRole.value = matchedRole
        triggerSyncLoader("User Persona Authenticated", "Loaded MongoDB User Document ID: ${user.mongoId}")
    }

    fun logoutUser() {
        _currentUser.value = null
        triggerSyncLoader("Session Disconnected", "Clearing active credentials & returning to guest mode")
    }

    fun zeroOutAllOrders() {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Zeroing Out Procurement Ledger",
                subtitle = "Clearing PRs, POs, Invoices & resetting budgets in MongoDB..."
            ) {
                repository.zeroOutAllOrders()
            }
        }
    }

    fun registerUser(name: String, email: String, role: UserRole, department: String, onRegistered: ((UserEntity) -> Unit)? = null) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Creating MongoDB Document",
                subtitle = "Inserting user record into smartflow.users collection..."
            ) {
                val createdUser = repository.registerUser(name, email, role.displayName, department)
                loginUser(createdUser)
                onRegistered?.invoke(createdUser)
            }
        }
    }

    // Active Navigation Tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Database reactive streams
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseRequests: StateFlow<List<PurchaseRequestEntity>> = repository.allPurchaseRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvalRecords: StateFlow<List<ApprovalRecordEntity>> = repository.allApprovalRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rfqs: StateFlow<List<RfqEntity>> = repository.allRfqs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotations: StateFlow<List<QuotationEntity>> = repository.allQuotations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseOrders: StateFlow<List<PurchaseOrderEntity>> = repository.allPurchaseOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goodsReceipts: StateFlow<List<GoodsReceiptEntity>> = repository.allGoodsReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItemEntity>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockMovements: StateFlow<List<StockMovementEntity>> = repository.allStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant state
    private val _aiChatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello! I am your SmartFlow AI Procurement Assistant. Ask me about spend analytics, supplier ratings, RFQ specification drafting, or 3-Way match verification." to false
        )
    )
    val aiChatMessages: StateFlow<List<Pair<String, Boolean>>> = _aiChatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun createPurchaseRequest(
        title: String,
        requesterName: String,
        department: String,
        priority: String,
        requiredDate: String,
        justification: String,
        items: List<Pair<String, Pair<Int, Double>>>
    ) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Creating Purchase Request Document",
                subtitle = "Persisting PR to MongoDB Collection & SQLite Room Ledger..."
            ) {
                repository.createPurchaseRequest(
                    title = title,
                    requesterName = requesterName,
                    department = department,
                    priority = priority,
                    requiredDate = requiredDate,
                    justification = justification,
                    items = items
                )
            }
        }
    }

    fun approveOrRejectRequest(
        prId: Long,
        isApprove: Boolean,
        approverName: String,
        comments: String
    ) {
        viewModelScope.launch {
            val role = _currentRole.value
            val actionText = if (isApprove) "Approving" else "Rejecting"
            runWithSyncLoader(
                title = "$actionText Purchase Request #$prId",
                subtitle = "Updating Workflow Audit Trail & Synchronizing MongoDB Atlas Document..."
            ) {
                val newStatus = if (isApprove) RequestStatus.APPROVED.label else RequestStatus.REJECTED.label

                repository.updatePurchaseRequestStatus(
                    prId = prId,
                    newStatus = newStatus,
                    approverName = approverName,
                    approverRole = role.displayName,
                    comments = comments
                )
            }
        }
    }

    fun createRfq(prId: Long, title: String, deadlineDate: String, terms: String) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Publishing RFQ Document",
                subtitle = "Dispatching Request for Quotation to Registered Suppliers..."
            ) {
                repository.createRfqForPr(prId, title, deadlineDate, terms)
            }
        }
    }

    fun submitQuotation(
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
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Submitting Supplier Quotation",
                subtitle = "Recording Bidding Offer & Pricing in MongoDB Ledger..."
            ) {
                repository.submitQuotation(
                    rfqId, supplierId, supplierName, unitPrice, qty, deliveryDays, paymentTerms, rating, notes
                )
            }
        }
    }

    fun awardPo(quotation: QuotationEntity, deliveryAddress: String) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Awarding Purchase Order",
                subtitle = "Issuing Formal PO & Committing Departmental Budget..."
            ) {
                repository.awardPoFromQuotation(quotation, deliveryAddress)
            }
        }
    }

    fun createGoodsReceipt(
        poId: Long,
        receivedBy: String,
        warehouseName: String,
        receivedQty: Int,
        acceptedQty: Int,
        rejectedQty: Int,
        rejectionReason: String
    ) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Generating Goods Receipt Note (GRN)",
                subtitle = "Recording Physical Inspection & Inventory Intake..."
            ) {
                repository.createGoodsReceipt(
                    poId, receivedBy, warehouseName, receivedQty, acceptedQty, rejectedQty, rejectionReason
                )
            }
        }
    }

    fun perform3WayMatch(invoiceId: Long) {
        viewModelScope.launch {
            runWithSyncLoader(
                title = "Executing 3-Way Match Engine",
                subtitle = "Matching PO, GRN, & Supplier Invoice Tolerances in MongoDB..."
            ) {
                repository.perform3WayMatchAndPay(invoiceId, _currentRole.value.displayName)
            }
        }
    }

    fun sendAiQuery(query: String) {
        if (query.isBlank()) return
        val current = _aiChatMessages.value.toMutableList()
        current.add(query to true)
        _aiChatMessages.value = current
        _isAiLoading.value = true

        viewModelScope.launch {
            runWithSyncLoader(
                title = "Gemini AI Copilot Processing",
                subtitle = "Analyzing System Context & Generating Response...",
                delayMs = 400
            ) {
                val prCount = purchaseRequests.value.size
                val poCount = purchaseOrders.value.size
                val totalBudget = budgets.value.sumOf { it.totalBudget }
                val totalSpent = budgets.value.sumOf { it.spentAmount }

                val dataSummary = "Total PRs: $prCount, Total POs: $poCount, Total Org Budget: $$totalBudget, Total Org Spent: $$totalSpent."

                val reply = geminiAssistant.queryProcurementAi(query, dataSummary)
                val updated = _aiChatMessages.value.toMutableList()
                updated.add(reply to false)
                _aiChatMessages.value = updated
                _isAiLoading.value = false
            }
        }
    }

    fun dismissBudgetAlert(alertId: String) {
        BudgetCheckBackgroundService.dismissAlert(alertId)
    }

    fun clearAllBudgetAlerts() {
        BudgetCheckBackgroundService.clearAllAlerts()
    }
}
