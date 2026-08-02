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
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@Composable
fun GrnWarehouseScreen(
    purchaseOrders: List<PurchaseOrderEntity>,
    goodsReceipts: List<GoodsReceiptEntity>,
    currentRole: UserRole,
    onCreateGrn: (Long, String, String, Int, Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPoForReceiving by remember { mutableStateOf<PurchaseOrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Goods Receiving & GRN Inspection",
            subtitle = "Warehouse Dock Receiving & Quality Control"
        )

        Text("Select Delivered PO to Receive & Inspect:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        if (purchaseOrders.isEmpty()) {
            Text("No active purchase orders available for receiving.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(purchaseOrders) { po ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedPoForReceiving = po },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(po.poNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Supplier: ${po.supplierName}", fontSize = 11.sp)
                            }
                            ProcurementStatusBadge(status = po.status)
                        }
                    }
                }
            }
        }

        Divider()

        Text("Completed Goods Received Notes (GRNs) (${goodsReceipts.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(goodsReceipts) { grn ->
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
                            Text(grn.grnNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            ProcurementStatusBadge(status = grn.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Warehouse: ${grn.warehouseName} • Received By: ${grn.receivedBy}", fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Accepted Qty: ${grn.acceptedQty}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            Text("Rejected Qty: ${grn.rejectedQty}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                        if (grn.rejectionReason.isNotBlank()) {
                            Text("Inspection Notes: ${grn.rejectionReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Modal to Receive Goods and Perform Quality Inspection
    selectedPoForReceiving?.let { po ->
        var receivedBy by remember { mutableStateOf("Carlos Mendez") }
        var warehouseName by remember { mutableStateOf("Central Logistics Hub") }
        var totalQty by remember { mutableStateOf("5") }
        var acceptedQty by remember { mutableStateOf("5") }
        var rejectedQty by remember { mutableStateOf("0") }
        var rejectionReason by remember { mutableStateOf("Passed barcode & diagnostic inspection") }

        AlertDialog(
            onDismissRequest = { selectedPoForReceiving = null },
            title = { Text("Inspect & Receive Goods for ${po.poNumber}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Supplier: ${po.supplierName}")

                    OutlinedTextField(
                        value = warehouseName,
                        onValueChange = { warehouseName = it },
                        label = { Text("Warehouse Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = receivedBy,
                        onValueChange = { receivedBy = it },
                        label = { Text("Inspector / Received By") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = totalQty,
                            onValueChange = { totalQty = it },
                            label = { Text("Total Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = acceptedQty,
                            onValueChange = { acceptedQty = it },
                            label = { Text("Accepted Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rejectedQty,
                            onValueChange = { rejectedQty = it },
                            label = { Text("Rejected Qty") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Quality Check Remarks / Defect Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val total = totalQty.toIntOrNull() ?: 5
                        val acc = acceptedQty.toIntOrNull() ?: 5
                        val rej = rejectedQty.toIntOrNull() ?: 0
                        onCreateGrn(po.id, receivedBy, warehouseName, total, acc, rej, rejectionReason)
                        selectedPoForReceiving = null
                    },
                    modifier = Modifier.testTag("submit_grn_btn")
                ) {
                    Text("Generate GRN & Update Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPoForReceiving = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
