package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@Composable
fun DashboardScreen(
    purchaseRequests: List<PurchaseRequestEntity>,
    purchaseOrders: List<PurchaseOrderEntity>,
    budgets: List<BudgetEntity>,
    auditLogs: List<AuditLogEntity>,
    currentRole: UserRole,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingApprovals = purchaseRequests.count {
        it.status == RequestStatus.PENDING_MANAGER.label || it.status == RequestStatus.PENDING_FINANCE.label
    }
    val totalPoValue = purchaseOrders.sumOf { it.totalAmount }
    val totalBudget = budgets.sumOf { it.totalBudget }
    val totalSpent = budgets.sumOf { it.spentAmount }
    val budgetPercent = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Welcome Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Dashboard",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Welcome, ${currentRole.displayName}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Real-time enterprise purchasing control & spend overview",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Executive KPI Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Active Requests",
                        value = "${purchaseRequests.size}",
                        subtext = "$pendingApprovals pending review",
                        icon = Icons.Default.Description,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Total PO Value",
                        value = "$${(totalPoValue / 1000).toInt()}k",
                        subtext = "${purchaseOrders.size} issued orders",
                        icon = Icons.Default.ShoppingCart,
                        iconTint = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricKpiCard(
                        title = "Pending Approvals",
                        value = "$pendingApprovals",
                        subtext = "Action required",
                        icon = Icons.Default.Verified,
                        iconTint = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Budget Usage",
                        value = "$budgetPercent%",
                        subtext = "$${(totalSpent/1000).toInt()}k spent of $${(totalBudget/1000).toInt()}k",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // MongoDB Atlas Cluster Live Sync Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_mongodb_atlas_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF023430))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00ED64).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "MongoDB Atlas",
                                    tint = Color(0xFF00ED64),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "MongoDB Atlas Enterprise Store",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Cluster0 • DB: smartflow_db • Status: 🟢 Connected",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF00ED64)),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Synchronized Atlas Collections & Document Stream",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00ED64).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00ED64).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF00ED64),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "💡 Atlas Explorer Note: In MongoDB Atlas UI, select database 'smartflow_db' (or click Cluster0 -> smartflow_db) to view collections. System collection 'local > oplog.rs' does not store application data.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Procurement End-to-End Workflow Map
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_workflow_map_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "End-to-End Procurement Lifecycle",
                        subtitle = "Automated 8-Stage Purchasing Pipeline"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val stages = listOf(
                        "1. PR Created" to 1,
                        "2. Approval" to 2,
                        "3. RFQ Tender" to 3,
                        "4. Quotations" to 3,
                        "5. PO Issued" to 4,
                        "6. Goods Receiving" to 5,
                        "7. 3-Way Match" to 6,
                        "8. Payment Done" to 7
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stages.take(4).forEach { (label, tabIndex) ->
                            WorkflowStepChip(
                                label = label,
                                onClick = { onNavigateToTab(tabIndex) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stages.drop(4).forEach { (label, tabIndex) ->
                            WorkflowStepChip(
                                label = label,
                                onClick = { onNavigateToTab(tabIndex) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            SectionHeader(title = "Quick Actions & Workflows")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab(1) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New PR", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onNavigateToTab(2) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approvals", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onNavigateToTab(9) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Advisor", fontSize = 12.sp)
                }
            }
        }

        // Recent Audit Activity Log
        item {
            SectionHeader(
                title = "Recent System Audit Activity",
                actionText = "View All Logs",
                onActionClick = { onNavigateToTab(10) }
            )
        }

        items(auditLogs.take(4)) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.userName} (${log.userRole})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            ProcurementStatusBadge(status = log.module)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowStepChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                maxLines = 1
            )
        }
    }
}
