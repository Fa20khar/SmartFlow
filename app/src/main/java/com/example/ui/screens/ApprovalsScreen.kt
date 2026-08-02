package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@Composable
fun ApprovalsScreen(
    purchaseRequests: List<PurchaseRequestEntity>,
    approvalRecords: List<ApprovalRecordEntity>,
    currentRole: UserRole,
    onApproveOrReject: (Long, Boolean, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Pending Review, 1 = Approval Audit History
    var activeActionPr by remember { mutableStateOf<Pair<PurchaseRequestEntity, Boolean>?>(null) } // PR + isApprove

    val pendingRequests = purchaseRequests.filter { pr ->
        pr.status == RequestStatus.PENDING_MANAGER.label || pr.status == RequestStatus.PENDING_FINANCE.label
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Approval Workflow Engine",
            subtitle = "Role-Based Multi-Level Gatekeeping"
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pending Review (${pendingRequests.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Approval History (${approvalRecords.size})") }
            )
        }

        if (selectedTab == 0) {
            if (pendingRequests.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All purchase requests are reviewed!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "No pending approval bottlenecks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingRequests) { pr ->
                        ApprovalCard(
                            pr = pr,
                            currentRole = currentRole,
                            onApproveClick = { activeActionPr = pr to true },
                            onRejectClick = { activeActionPr = pr to false }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(approvalRecords) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Request #${record.requestId}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                ProcurementStatusBadge(status = record.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By ${record.approverName} (${record.approverRole})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (record.comments.isNotBlank()) {
                                Text(
                                    text = "Comments: ${record.comments}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Sign-off Comments
    activeActionPr?.let { (pr, isApprove) ->
        var comments by remember { mutableStateOf("") }
        var approverName by remember { mutableStateOf(currentRole.displayName) }

        AlertDialog(
            onDismissRequest = { activeActionPr = null },
            title = {
                Text(
                    text = if (isApprove) "Approve PR #${pr.requestNumber}" else "Reject PR #${pr.requestNumber}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Requisition: ${pr.title}")
                    Text("Total Estimated Cost: $${pr.totalEstimatedCost}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = approverName,
                        onValueChange = { approverName = it },
                        label = { Text("Approver Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Approval / Rejection Comments") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveOrReject(pr.id, isApprove, approverName, comments)
                        activeActionPr = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) Color(0xFF10B981) else Color(0xFFEF4444)
                    ),
                    modifier = Modifier.testTag("confirm_approval_btn")
                ) {
                    Text(if (isApprove) "Confirm Approval" else "Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeActionPr = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ApprovalCard(
    pr: PurchaseRequestEntity,
    currentRole: UserRole,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("approval_card_${pr.requestNumber.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pr.requestNumber,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PriorityBadge(priority = pr.priority)
                    ProcurementStatusBadge(status = pr.status)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pr.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${pr.department} • Requester: ${pr.requesterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${pr.totalEstimatedCost}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Justification: ${pr.justification}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onApproveClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}
