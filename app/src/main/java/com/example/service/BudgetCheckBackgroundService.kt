package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.data.local.BudgetEntity
import com.example.data.local.PurchaseRequestEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BudgetAlertNotification(
    val id: String,
    val prId: Long,
    val requestNumber: String,
    val title: String,
    val department: String,
    val estimatedCost: Double,
    val totalBudget: Double,
    val spentAmount: Double,
    val remainingBudget: Double,
    val excessAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String
)

/**
 * Background Service that monitors purchase request totals against department budgets
 * and triggers real-time system notifications and reactive UI alerts when budget limits are exceeded.
 */
class BudgetCheckBackgroundService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    inner class LocalBinder : Binder() {
        fun getService(): BudgetCheckBackgroundService = this@BudgetCheckBackgroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prId = intent?.getLongExtra(EXTRA_PR_ID, -1L) ?: -1L
        if (prId != -1L) {
            val reqNumber = intent?.getStringExtra(EXTRA_REQ_NUMBER) ?: "PR"
            val title = intent?.getStringExtra(EXTRA_TITLE) ?: ""
            val dept = intent?.getStringExtra(EXTRA_DEPT) ?: ""
            val cost = intent?.getDoubleExtra(EXTRA_COST, 0.0) ?: 0.0
            val totalBudget = intent?.getDoubleExtra(EXTRA_TOTAL_BUDGET, 0.0) ?: 0.0
            val spent = intent?.getDoubleExtra(EXTRA_SPENT, 0.0) ?: 0.0

            serviceScope.launch {
                evaluateAndTriggerAlert(
                    context = applicationContext,
                    prId = prId,
                    requestNumber = reqNumber,
                    title = title,
                    department = dept,
                    cost = cost,
                    totalBudget = totalBudget,
                    spentAmount = spent
                )
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val EXTRA_PR_ID = "extra_pr_id"
        const val EXTRA_REQ_NUMBER = "extra_req_number"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DEPT = "extra_dept"
        const val EXTRA_COST = "extra_cost"
        const val EXTRA_TOTAL_BUDGET = "extra_total_budget"
        const val EXTRA_SPENT = "extra_spent"

        private const val CHANNEL_ID = "budget_limit_alerts_channel"

        private val _realtimeAlerts = MutableStateFlow<List<BudgetAlertNotification>>(emptyList())
        val realtimeAlerts: StateFlow<List<BudgetAlertNotification>> = _realtimeAlerts.asStateFlow()

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Department Budget Limit Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Triggers real-time notifications when a Purchase Request exceeds assigned department budget limits"
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        /**
         * Core evaluation logic: compares purchase request total against department budget
         */
        fun evaluateAndTriggerAlert(
            context: Context,
            prId: Long,
            requestNumber: String,
            title: String,
            department: String,
            cost: Double,
            totalBudget: Double,
            spentAmount: Double
        ): BudgetAlertNotification? {
            val remainingBudget = totalBudget - spentAmount
            // Check if request exceeds remaining budget OR total budget limit
            if (cost > remainingBudget || (totalBudget > 0 && cost > totalBudget)) {
                val excess = cost - remainingBudget
                val msg = "PR #$requestNumber ($title) for $department ($${String.format("%,.2f", cost)}) exceeds remaining budget ($${String.format("%,.2f", remainingBudget)} of $${String.format("%,.2f", totalBudget)}) by $${String.format("%,.2f", excess)}!"
                
                val alert = BudgetAlertNotification(
                    id = "alert_${prId}_${System.currentTimeMillis()}",
                    prId = prId,
                    requestNumber = requestNumber,
                    title = title,
                    department = department,
                    estimatedCost = cost,
                    totalBudget = totalBudget,
                    spentAmount = spentAmount,
                    remainingBudget = remainingBudget,
                    excessAmount = excess,
                    message = msg
                )

                // 1. Post to Real-time StateFlow for UI banner notifications
                val currentList = _realtimeAlerts.value.filterNot { it.prId == prId }
                _realtimeAlerts.value = listOf(alert) + currentList

                // 2. Trigger System Notification via NotificationManager
                triggerSystemNotification(context, alert)

                return alert
            }
            return null
        }

        /**
         * Evaluates all purchase requests against budgets reactively.
         */
        fun evaluateAllRequests(
            context: Context,
            requests: List<PurchaseRequestEntity>,
            budgets: List<BudgetEntity>
        ) {
            val budgetMap = budgets.associateBy { it.department.lowercase() }
            val generatedAlerts = mutableListOf<BudgetAlertNotification>()

            requests.forEach { pr ->
                val b = budgetMap[pr.department.lowercase()]
                val totalBudget = b?.totalBudget ?: 100000.0
                val spent = b?.spentAmount ?: 0.0
                val remaining = totalBudget - spent

                if (pr.totalEstimatedCost > remaining || (totalBudget > 0 && pr.totalEstimatedCost > totalBudget)) {
                    val excess = pr.totalEstimatedCost - remaining
                    val msg = "PR #${pr.requestNumber} (${pr.title}) for ${pr.department} ($${String.format("%,.2f", pr.totalEstimatedCost)}) exceeds available budget ($${String.format("%,.2f", remaining)})."
                    val alert = BudgetAlertNotification(
                        id = "alert_${pr.id}",
                        prId = pr.id,
                        requestNumber = pr.requestNumber,
                        title = pr.title,
                        department = pr.department,
                        estimatedCost = pr.totalEstimatedCost,
                        totalBudget = totalBudget,
                        spentAmount = spent,
                        remainingBudget = remaining,
                        excessAmount = excess,
                        message = msg
                    )
                    generatedAlerts.add(alert)
                }
            }

            _realtimeAlerts.value = generatedAlerts
        }

        private fun triggerSystemNotification(context: Context, alert: BudgetAlertNotification) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                createNotificationChannel(context)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("🚨 Budget Exceeded: ${alert.department}")
                    .setContentText(alert.message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify(alert.prId.toInt(), builder.build())
            } catch (e: Exception) {
                android.util.Log.e("BudgetCheckService", "Error posting notification: ${e.message}")
            }
        }

        fun dismissAlert(alertId: String) {
            _realtimeAlerts.value = _realtimeAlerts.value.filterNot { it.id == alertId }
        }

        fun clearAllAlerts() {
            _realtimeAlerts.value = emptyList()
        }
    }
}
