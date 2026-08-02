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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader

@Composable
fun InvoiceMatchingScreen(
    invoices: List<InvoiceEntity>,
    currentRole: UserRole,
    onApprovePayment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Finance & 3-Way Invoice Matching",
            subtitle = "PO vs GRN vs Supplier Invoice Automated Reconciliation"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(invoices) { inv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invoice_card_${inv.invoiceNumber.lowercase()}"),
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
                                text = inv.invoiceNumber,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            ProcurementStatusBadge(status = inv.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Supplier: ${inv.supplierName}",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3-Way Match Comparison Matrix
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (inv.is3WayMatched) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "3-WAY MATCH VERIFICATION RESULT:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = if (inv.is3WayMatched) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("1. PO Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$${inv.poAmount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("2. GRN Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$${inv.grnValue}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Column {
                                        Text("3. Invoice Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$${inv.invoiceAmount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = inv.discrepancyNotes,
                                    fontSize = 11.sp,
                                    color = if (inv.is3WayMatched) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onApprovePayment(inv.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve & Execute Payment", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
