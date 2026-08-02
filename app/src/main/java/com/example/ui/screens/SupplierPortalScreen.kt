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
import com.example.data.local.GoodsReceiptEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.QuotationEntity
import com.example.data.local.SupplierEntity
import com.example.ui.components.ProcurementStatusBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.SupplierPerformanceDashboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierPortalScreen(
    suppliers: List<SupplierEntity>,
    quotations: List<QuotationEntity> = emptyList(),
    purchaseOrders: List<PurchaseOrderEntity> = emptyList(),
    goodsReceipts: List<GoodsReceiptEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedPortalTab by remember { mutableIntStateOf(0) } // 0 = Analytics & Trends, 1 = Vendor Directory

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Supplier Management & Analytics",
            subtitle = "Vendor Performance Trends, Lead Time & Ratings Directory"
        )

        PrimaryTabRow(
            selectedTabIndex = selectedPortalTab,
            modifier = Modifier.fillMaxWidth().testTag("supplier_portal_tab_row")
        ) {
            Tab(
                selected = selectedPortalTab == 0,
                onClick = { selectedPortalTab = 0 },
                text = { Text("Performance Analytics") },
                icon = { Icon(Icons.Default.Analytics, contentDescription = "Performance Analytics") }
            )
            Tab(
                selected = selectedPortalTab == 1,
                onClick = { selectedPortalTab = 1 },
                text = { Text("Vendor Directory (${suppliers.size})") },
                icon = { Icon(Icons.Default.Badge, contentDescription = "Vendor Directory") }
            )
        }

        when (selectedPortalTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SupplierPerformanceDashboard(
                            suppliers = suppliers,
                            quotations = quotations,
                            purchaseOrders = purchaseOrders,
                            goodsReceipts = goodsReceipts
                        )
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suppliers) { supplier ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("supplier_card_${supplier.code.lowercase()}"),
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
                                    Column {
                                        Text(
                                            text = supplier.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Code: ${supplier.code} • Category: ${supplier.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    ProcurementStatusBadge(status = supplier.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rating: ${supplier.rating} / 5.0", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Text("Terms: ${supplier.paymentTerms}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Contact: ${supplier.email} | ${supplier.phone}",
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
}

