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
import com.example.ui.components.SectionHeader

@Composable
fun InventoryScreen(
    inventoryItems: List<InventoryItemEntity>,
    stockMovements: List<StockMovementEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Stock On Hand, 1 = Stock Movements
    val lowStockCount = inventoryItems.count { it.stockQty <= it.minAlertQty }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Inventory & Warehouse Stock",
            subtitle = "Multi-Location Real-time Stock Engine"
        )

        // Low stock warning banner if applicable
        if (lowStockCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("low_stock_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFDC2626)
                    )
                    Column {
                        Text(
                            text = "Low Stock Warning ($lowStockCount Items)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Inventory is below minimum reorder threshold. Reorder required.",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Stock On Hand (${inventoryItems.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Movements Log (${stockMovements.size})") })
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inventoryItems) { item ->
                    val isLow = item.stockQty <= item.minAlertQty
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("SKU: ${item.sku} • Location: ${item.warehouseName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.stockQty} units",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (isLow) Color(0xFFDC2626) else Color(0xFF10B981)
                                    )
                                    if (isLow) {
                                        Text("Low Stock Alert!", fontSize = 10.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stockMovements) { mov ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SKU: ${mov.sku} • Ref: ${mov.referenceNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("By ${mov.performedBy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (mov.type == "IN") Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${if (mov.type == "IN") "+" else "-"}${mov.qty} (${mov.type})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (mov.type == "IN") Color(0xFF166534) else Color(0xFF991B1B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
