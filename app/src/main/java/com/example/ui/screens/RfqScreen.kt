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
fun RfqScreen(
    rfqs: List<RfqEntity>,
    quotations: List<QuotationEntity>,
    suppliers: List<SupplierEntity>,
    currentRole: UserRole,
    onSubmitQuote: (Long, Long, String, Double, Int, Int, String, Double, String) -> Unit,
    onAwardPo: (QuotationEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRfq by remember { mutableStateOf<RfqEntity?>(rfqs.firstOrNull()) }
    var showSubmitQuoteDialog by remember { mutableStateOf(false) }

    val activeQuotations = quotations.filter { it.rfqId == (selectedRfq?.id ?: 0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "RFQ & Quotations Management",
            subtitle = "Competitive Tender & Supplier Comparison"
        )

        if (rfqs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No active RFQs. Approve a Purchase Request to generate an RFQ.")
            }
        } else {
            // RFQ Selector Row
            Text("Select Active RFQ Tender:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rfqs.forEach { rfq ->
                    val isSelected = rfq.id == selectedRfq?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRfq = rfq },
                        label = { Text(rfq.rfqNumber) }
                    )
                }
            }

            selectedRfq?.let { rfq ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = rfq.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            ProcurementStatusBadge(status = rfq.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tender Deadline: ${rfq.deadlineDate} • Terms: ${rfq.terms}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Received Supplier Quotations (${activeQuotations.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = { showSubmitQuoteDialog = true },
                        modifier = Modifier.testTag("submit_quote_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Supplier Quote", fontSize = 11.sp)
                    }
                }

                // Quotation Matrix Table
                if (activeQuotations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No quotations received yet for this RFQ.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeQuotations) { quote ->
                            QuotationComparisonCard(
                                quote = quote,
                                onAward = {
                                    onAwardPo(quote, "SmartFlow Central Warehouse, Dock 1")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Submit Quote Modal
    if (showSubmitQuoteDialog && selectedRfq != null) {
        var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
        var unitPrice by remember { mutableStateOf("3500.0") }
        var qty by remember { mutableStateOf("5") }
        var deliveryDays by remember { mutableStateOf("10") }
        var paymentTerms by remember { mutableStateOf("Net 30") }
        var notes by remember { mutableStateOf("Standard commercial warranty") }

        AlertDialog(
            onDismissRequest = { showSubmitQuoteDialog = false },
            title = { Text("Submit Supplier Quotation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Target RFQ: ${selectedRfq!!.rfqNumber}")

                    Text("Supplier:")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        suppliers.forEach { sup ->
                            FilterChip(
                                selected = selectedSupplier?.id == sup.id,
                                onClick = { selectedSupplier = sup },
                                label = { Text(sup.name, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = unitPrice,
                            onValueChange = { unitPrice = it },
                            label = { Text("Unit Price ($)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = deliveryDays,
                            onValueChange = { deliveryDays = it },
                            label = { Text("Delivery (Days)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paymentTerms,
                            onValueChange = { paymentTerms = it },
                            label = { Text("Terms") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Value Addition / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sup = selectedSupplier ?: return@Button
                        val price = unitPrice.toDoubleOrNull() ?: 3000.0
                        val q = qty.toIntOrNull() ?: 1
                        val days = deliveryDays.toIntOrNull() ?: 7
                        onSubmitQuote(selectedRfq!!.id, sup.id, sup.name, price, q, days, paymentTerms, sup.rating, notes)
                        showSubmitQuoteDialog = false
                    },
                    modifier = Modifier.testTag("confirm_submit_quote_btn")
                ) {
                    Text("Submit Quote")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitQuoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuotationComparisonCard(
    quote: QuotationEntity,
    onAward: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quote_card_${quote.supplierName.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quote.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (quote.isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = quote.supplierName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "⭐ Supplier Rating: ${quote.rating} / 5.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (quote.isSelected) {
                    ProcurementStatusBadge(status = "Awarded")
                } else {
                    Text(
                        text = "$${quote.totalAmount}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Unit Price: $${quote.unitPrice}", fontSize = 12.sp)
                Text("Delivery: ${quote.deliveryDays} Days", fontSize = 12.sp)
                Text("Terms: ${quote.paymentTerms}", fontSize = 12.sp)
            }

            if (quote.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${quote.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!quote.isSelected) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAward,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Award Purchase Order", fontSize = 12.sp)
                }
            }
        }
    }
}
