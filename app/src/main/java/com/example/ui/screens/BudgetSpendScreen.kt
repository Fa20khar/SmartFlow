package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BudgetEntity
import com.example.ui.components.SectionHeader

@Composable
fun BudgetSpendScreen(
    budgets: List<BudgetEntity>,
    modifier: Modifier = Modifier
) {
    val totalAllocated = budgets.sumOf { it.totalBudget }
    val totalSpent = budgets.sumOf { it.spentAmount }
    val totalRemaining = totalAllocated - totalSpent

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Spend Control & Department Budgets",
            subtitle = "Fiscal Year 2026 Budget Monitoring"
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("budget_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ORGANIZATION TOTAL BUDGET",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$${(totalAllocated / 1000).toInt()}k",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Spent: $${(totalSpent / 1000).toInt()}k", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Remaining: $${(totalRemaining / 1000).toInt()}k", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (totalAllocated > 0) (totalSpent / totalAllocated).toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Text("Department Budgets (${budgets.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(budgets) { b ->
                val progress = if (b.totalBudget > 0) (b.spentAmount / b.totalBudget).toFloat() else 0f
                val remaining = b.totalBudget - b.spentAmount

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(b.department, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("$${(b.spentAmount/1000).toInt()}k / $${(b.totalBudget/1000).toInt()}k", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (progress > 0.85f) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fiscal Year: ${b.fiscalYear}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Remaining: $${remaining.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (remaining < 10000) Color(0xFFDC2626) else Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}
