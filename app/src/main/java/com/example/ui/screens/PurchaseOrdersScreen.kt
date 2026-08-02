package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@Composable
fun PurchaseOrdersScreen(
    purchaseOrders: List<PurchaseOrderEntity>,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    var selectedPoForPrint by remember { mutableStateOf<PurchaseOrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Purchase Orders (POs)",
            subtitle = "Official B2B Contracts & Delivery Tracking"
        )

        if (purchaseOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No purchase orders generated yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(purchaseOrders) { po ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("po_card_${po.poNumber.lowercase()}"),
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
                                    text = po.poNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                ProcurementStatusBadge(status = po.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Supplier: ${po.supplierName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Delivery Address: ${po.deliveryAddress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Issued: ${po.createdDate} • Delivery: ${po.expectedDeliveryDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${po.totalAmount}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { selectedPoForPrint = po },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview Printable PO Document", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Printable PO Document Modal Dialog
    selectedPoForPrint?.let { po ->
        AlertDialog(
            onDismissRequest = { selectedPoForPrint = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PURCHASE ORDER", fontWeight = FontWeight.ExtraBold)
                    Text(po.poNumber, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SMARTFLOW ENTERPRISE INC.", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        Text("Corporate Procurement Division • 100 Technology Plaza", fontSize = 11.sp, color = Color(0xFF475569))

                        Divider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("VENDOR / SUPPLIER:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(po.supplierName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("DELIVER TO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text(po.deliveryAddress, fontSize = 11.sp)
                            }
                        }

                        Divider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Item Description", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Enterprise Goods / Services Contract", fontSize = 12.sp)
                            Text("$${po.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider()

                        Text("Terms & Conditions: ${po.paymentTerms}. All deliveries subject to 3-way match & warehouse quality inspection.", fontSize = 10.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("AUTHORIZED SIGNATURE: Sarah Jenkins (Procurement Director)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F4C81))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedPoForPrint = null }) {
                    Text("Print / Export Document")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPoForPrint = null }) {
                    Text("Close")
                }
            }
        )
    }
}
