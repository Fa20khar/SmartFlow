package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseRequestsScreen(
    purchaseRequests: List<PurchaseRequestEntity>,
    currentRole: UserRole,
    onCreateRequest: (String, String, String, String, String, String, List<Pair<String, Pair<Int, Double>>>) -> Unit,
    onCreateRfq: (Long, String, String, String) -> Unit,
    onApproveOrReject: ((Long, Boolean, String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPrDetails by remember { mutableStateOf<PurchaseRequestEntity?>(null) }

    val filteredRequests = purchaseRequests.filter { pr ->
        val matchesSearch = pr.title.contains(searchQuery, ignoreCase = true) ||
                pr.requestNumber.contains(searchQuery, ignoreCase = true) ||
                pr.department.contains(searchQuery, ignoreCase = true)
        val matchesStatus = if (selectedStatusFilter == "All") true else pr.status == selectedStatusFilter
        matchesSearch && matchesStatus
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Request") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_pr_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Purchase Requests",
                subtitle = "Requisitions & Approval Tracker"
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pr_search_input"),
                placeholder = { Text("Search by title, PR number or department...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", RequestStatus.PENDING_MANAGER.label, RequestStatus.APPROVED.label, RequestStatus.REJECTED.label).forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status, fontSize = 11.sp) }
                    )
                }
            }

            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No purchase requests match filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRequests) { pr ->
                        PurchaseRequestCard(
                            pr = pr,
                            currentRole = currentRole,
                            onClick = { selectedPrDetails = pr },
                            onCreateRfq = {
                                onCreateRfq(pr.id, "RFQ: ${pr.title}", "2026-08-15", "Net 30, standard tender terms.")
                            },
                            onApprove = {
                                onApproveOrReject?.invoke(pr.id, true, currentRole.displayName, "Approved from Purchase Requests tab")
                            }
                        )
                    }
                }
            }
        }
    }

    // Create PR Dialog
    if (showCreateDialog) {
        CreatePurchaseRequestDialog(
            currentRole = currentRole,
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, dept, priority, date, just, items ->
                onCreateRequest(title, currentRole.displayName, dept, priority, date, just, items)
                showCreateDialog = false
            }
        )
    }

    // PR Detail Dialog
    selectedPrDetails?.let { pr ->
        AlertDialog(
            onDismissRequest = { selectedPrDetails = null },
            title = {
                Text(
                    text = "${pr.requestNumber} Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${pr.title}", fontWeight = FontWeight.SemiBold)
                    Text("Department: ${pr.department}")
                    Text("Requester: ${pr.requesterName}")
                    Text("Required Date: ${pr.requiredDate}")
                    Text("Total Cost: $${pr.totalEstimatedCost}")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:")
                        ProcurementStatusBadge(status = pr.status)
                        PriorityBadge(priority = pr.priority)
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Justification:", fontWeight = FontWeight.Bold)
                    Text(
                        text = pr.justification,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPrDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PurchaseRequestCard(
    pr: PurchaseRequestEntity,
    currentRole: UserRole,
    onClick: () -> Unit,
    onCreateRfq: () -> Unit,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pr_card_${pr.requestNumber.lowercase()}")
            .clip(RoundedCornerShape(14.dp)),
        onClick = onClick,
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${pr.department} • By ${pr.requesterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${pr.totalEstimatedCost}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (pr.status == RequestStatus.APPROVED.label) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onCreateRfq,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Initiate RFQ Tender", fontSize = 12.sp)
                }
            } else if (pr.status != RequestStatus.REJECTED.label) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("pr_quick_approve_btn_${pr.requestNumber.lowercase()}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Approve Requisition (${currentRole.displayName})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CreatePurchaseRequestDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, List<Pair<String, Pair<Int, Double>>>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Software Engineering") }
    var priority by remember { mutableStateOf("Medium") }
    var requiredDate by remember { mutableStateOf("2026-08-30") }
    var justification by remember { mutableStateOf("") }

    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("100.0") }

    val itemsList = remember { mutableStateListOf<Pair<String, Pair<Int, Double>>>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Purchase Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Request Title") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_pr_title")
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_pr_dept")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = requiredDate,
                        onValueChange = { requiredDate = it },
                        label = { Text("Required Date") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = justification,
                    onValueChange = { justification = it },
                    label = { Text("Justification / Purpose") },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()
                Text("Add Line Items", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = itemQty,
                        onValueChange = { itemQty = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val q = itemQty.toIntOrNull() ?: 1
                        val p = itemPrice.toDoubleOrNull() ?: 50.0
                        if (itemName.isNotBlank()) {
                            itemsList.add(itemName to (q to p))
                            itemName = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add Item")
                }

                if (itemsList.isNotEmpty()) {
                    Text("Line Items Added (${itemsList.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    itemsList.forEach { item ->
                        Text("• ${item.first}: ${item.second.first} x $${item.second.second}", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && itemsList.isNotEmpty()) {
                        onSubmit(title, department, priority, requiredDate, justification, itemsList.toList())
                    }
                },
                modifier = Modifier.testTag("submit_pr_button")
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
