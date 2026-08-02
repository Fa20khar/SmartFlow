package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BudgetAlertBannerList
import com.example.ui.components.RoleSwitcherHeader
import com.example.ui.components.UniqueSmartFlowLoaderDialog
import com.example.ui.screens.*
import com.example.ui.theme.SmartFlowTheme
import com.example.viewmodel.ProcurementViewModel

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFlowTheme {
                SmartFlowApp()
            }
        }
    }
}

@Composable
fun SmartFlowApp(
    viewModel: ProcurementViewModel = viewModel()
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val purchaseRequests by viewModel.purchaseRequests.collectAsState()
    val approvalRecords by viewModel.approvalRecords.collectAsState()
    val rfqs by viewModel.rfqs.collectAsState()
    val quotations by viewModel.quotations.collectAsState()
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val goodsReceipts by viewModel.goodsReceipts.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val stockMovements by viewModel.stockMovements.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val aiMessages by viewModel.aiChatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncTitle by viewModel.syncTitle.collectAsState()
    val syncSubtitle by viewModel.syncSubtitle.collectAsState()

    val realTimeBudgetAlerts by viewModel.realTimeBudgetAlerts.collectAsState()

    val navTabs = listOf(
        NavTabItem("Dashboard", Icons.Default.Dashboard, 0),
        NavTabItem("Requests", Icons.Default.Description, 1),
        NavTabItem("Approvals", Icons.Default.Verified, 2),
        NavTabItem("RFQs", Icons.Default.Assignment, 3),
        NavTabItem("POs", Icons.Default.ShoppingCart, 4),
        NavTabItem("Receiving", Icons.Default.LocalShipping, 5),
        NavTabItem("Invoices", Icons.Default.ReceiptLong, 6),
        NavTabItem("Budgets", Icons.Default.AccountBalanceWallet, 7),
        NavTabItem("Suppliers", Icons.Default.Store, 8),
        NavTabItem("AI Copilot", Icons.Default.AutoAwesome, 9),
        NavTabItem("Audit Logs", Icons.Default.History, 10),
        NavTabItem("Login/Register", Icons.Default.Person, 11)
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    @Composable
    fun MainContentBox(modifier: Modifier = Modifier) {
        Column(modifier = modifier.fillMaxSize()) {
            if (realTimeBudgetAlerts.isNotEmpty()) {
                BudgetAlertBannerList(
                    alerts = realTimeBudgetAlerts,
                    onDismissAlert = { alertId -> viewModel.dismissBudgetAlert(alertId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                0 -> DashboardScreen(
                    purchaseRequests = purchaseRequests,
                    purchaseOrders = purchaseOrders,
                    budgets = budgets,
                    auditLogs = auditLogs,
                    currentRole = currentRole,
                    onNavigateToTab = { viewModel.selectTab(it) }
                )
                1 -> PurchaseRequestsScreen(
                    purchaseRequests = purchaseRequests,
                    currentRole = currentRole,
                    onCreateRequest = { title, requester, dept, priority, date, just, items ->
                        viewModel.createPurchaseRequest(title, requester, dept, priority, date, just, items)
                    },
                    onCreateRfq = { prId, title, deadline, terms ->
                        viewModel.createRfq(prId, title, deadline, terms)
                        viewModel.selectTab(3)
                    },
                    onApproveOrReject = { prId, isApprove, approverName, comments ->
                        viewModel.approveOrRejectRequest(prId, isApprove, approverName, comments)
                    }
                )
                2 -> ApprovalsScreen(
                    purchaseRequests = purchaseRequests,
                    approvalRecords = approvalRecords,
                    currentRole = currentRole,
                    onApproveOrReject = { prId, isApprove, approverName, comments ->
                        viewModel.approveOrRejectRequest(prId, isApprove, approverName, comments)
                    }
                )
                3 -> RfqScreen(
                    rfqs = rfqs,
                    quotations = quotations,
                    suppliers = suppliers,
                    currentRole = currentRole,
                    onSubmitQuote = { rfqId, supId, supName, price, qty, deliveryDays, terms, rating, notes ->
                        viewModel.submitQuotation(rfqId, supId, supName, price, qty, deliveryDays, terms, rating, notes)
                    },
                    onAwardPo = { quote, address ->
                        viewModel.awardPo(quote, address)
                        viewModel.selectTab(4)
                    }
                )
                4 -> PurchaseOrdersScreen(
                    purchaseOrders = purchaseOrders,
                    currentRole = currentRole
                )
                5 -> GrnWarehouseScreen(
                    purchaseOrders = purchaseOrders,
                    goodsReceipts = goodsReceipts,
                    currentRole = currentRole,
                    onCreateGrn = { poId, recBy, wh, tot, acc, rej, notes ->
                        viewModel.createGoodsReceipt(poId, recBy, wh, tot, acc, rej, notes)
                    }
                )
                6 -> InvoiceMatchingScreen(
                    invoices = invoices,
                    currentRole = currentRole,
                    onApprovePayment = { invoiceId ->
                        viewModel.perform3WayMatch(invoiceId)
                    }
                )
                7 -> BudgetSpendScreen(
                    budgets = budgets
                )
                8 -> SupplierPortalScreen(
                    suppliers = suppliers,
                    quotations = quotations,
                    purchaseOrders = purchaseOrders,
                    goodsReceipts = goodsReceipts
                )
                9 -> AiAssistantScreen(
                    messages = aiMessages,
                    isLoading = isAiLoading,
                    onSendMessage = { viewModel.sendAiQuery(it) }
                )
                10 -> AuditLogScreen(
                    auditLogs = auditLogs
                )
                11 -> LoginRegisterScreen(
                    users = users,
                    currentUser = currentUser,
                    currentRole = currentRole,
                    onLoginUser = { viewModel.loginUser(it) },
                    onLogoutUser = { viewModel.logoutUser() },
                    onRegisterUser = { name, email, role, dept ->
                        viewModel.registerUser(name, email, role, dept)
                    },
                    onSwitchPersonaRole = { viewModel.switchRole(it) }
                )
            }
        }
    }
}

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .testTag("landscape_nav_rail")
            ) {
                navTabs.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedTab == tab.index,
                        onClick = { viewModel.selectTab(tab.index) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 10.sp) },
                        modifier = Modifier.testTag("rail_tab_${tab.title.lowercase()}")
                    )
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    RoleSwitcherHeader(
                        currentRole = currentRole,
                        currentUser = currentUser,
                        onRoleSelected = { viewModel.switchRole(it) },
                        onOpenAuthPage = { viewModel.selectTab(11) },
                        onZeroOutOrders = { viewModel.zeroOutAllOrders() },
                        onTriggerSyncLoader = { viewModel.triggerSyncLoader() }
                    )
                }
            ) { innerPadding ->
                MainContentBox(modifier = Modifier.padding(innerPadding))
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                RoleSwitcherHeader(
                    currentRole = currentRole,
                    currentUser = currentUser,
                    onRoleSelected = { viewModel.switchRole(it) },
                    onOpenAuthPage = { viewModel.selectTab(11) },
                    onZeroOutOrders = { viewModel.zeroOutAllOrders() },
                    onTriggerSyncLoader = { viewModel.triggerSyncLoader() }
                )
            },
            bottomBar = {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_row")
                ) {
                    navTabs.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab.index,
                            onClick = { viewModel.selectTab(tab.index) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == tab.index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.title.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            MainContentBox(modifier = Modifier.padding(innerPadding))
        }
    }

    if (isSyncing) {
        UniqueSmartFlowLoaderDialog(
            title = syncTitle,
            subtitle = syncSubtitle
        )
    }
}
