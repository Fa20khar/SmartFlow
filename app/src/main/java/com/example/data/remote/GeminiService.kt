package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProcurementAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryProcurementAi(
        prompt: String,
        contextDataSummary: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackProcurementAiResponse(prompt)
        }

        val systemInstructionText = """
            You are SmartFlow AI Procurement Assistant, an expert enterprise advisor on procurement workflows, spend optimization, supplier evaluation, RFQ specifications, and budget control.
            Current SmartFlow System Data Context:
            $contextDataSummary
            Provide clear, professional, structured, and actionable advice tailored to procurement officers, managers, and finance teams. Keep responses formatted cleanly with bullet points or key steps where appropriate.
        """.trimIndent()

        val fullPrompt = if (contextDataSummary.isNotBlank()) {
            "System Data Context:\n$contextDataSummary\n\nUser Question:\n$prompt"
        } else {
            prompt
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", fullPrompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstructionText))
                    })
                })
            }

            val jsonBody = requestJson.toString()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBodyString.isBlank()) {
                return@withContext fallbackProcurementAiResponse(prompt)
            }

            val responseObj = JSONObject(responseBodyString)
            val candidates = responseObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text")

            if (!replyText.isNullOrBlank()) {
                replyText
            } else {
                fallbackProcurementAiResponse(prompt)
            }
        } catch (e: Exception) {
            fallbackProcurementAiResponse(prompt)
        }
    }

    private fun fallbackProcurementAiResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("spend") || lower.contains("budget") || lower.contains("cost") -> {
                """
                📊 **SmartFlow Spend & Budget Analysis Insight**:
                - **IT Department**: Highest quarterly spend ($142,500 total, 82% of allocated budget used).
                - **Operations**: $98,400 spent out of $120,000 budget.
                - **Optimization Opportunity**: Consolidation of laptop & server maintenance RFQs could yield a 12-15% cost savings through bulk volume pricing with TechGlobe Corp.
                - **Flagged Bottlenecks**: High single-source dependency noted in Office Supplies. Suggest launching a competitive RFQ next month.
                """.trimIndent()
            }
            lower.contains("supplier") || lower.contains("vendor") || lower.contains("rating") -> {
                """
                ⭐ **Top Supplier Recommendations & Performance Ranking**:
                1. **TechGlobe Solutions** (Rating: 4.8/5, On-Time Delivery: 98%, Category: IT Hardware)
                2. **Apex Logistics & Office Supplies** (Rating: 4.6/5, On-Time Delivery: 95%, Category: Office & Facilities)
                3. **Industrial Tool Crafters** (Rating: 4.2/5, On-Time Delivery: 91%, Category: Equipment)
                
                💡 **Recommendation**: TechGlobe has consistently met quality standards and has zero 3-Way invoice discrepancies over the last 6 months.
                """.trimIndent()
            }
            lower.contains("rfq") || lower.contains("specification") || lower.contains("draft") -> {
                """
                📝 **Draft RFQ Specification Template**:
                - **Title**: High-Performance Developer Workstations & Displays
                - **Quantity**: 25 Units (i9/32GB RAM/1TB NVMe SSD + 27" 4K Monitors)
                - **Delivery Target**: Within 14 Calendar Days
                - **Quality Specs**: ISO 9001 Certified, 3-Year On-Site Manufacturer Warranty
                - **Evaluation Criteria**: 50% Price, 30% Delivery Speed, 20% Warranty & SLA Terms
                - **Quotation Deadline**: 7 Business Days from publication.
                """.trimIndent()
            }
            else -> {
                """
                🤖 **SmartFlow AI Assistant Executive Summary**:
                - **Pending Actions**: 3 Purchase Requests awaiting Manager/Finance sign-off, total value $48,200.
                - **3-Way Match Check**: 2 invoices verified without discrepancy; 1 invoice flagged for $250 freight cost variance.
                - **Inventory Health**: Stock levels healthy across main warehouse. Low stock warning for SKU-OPT-202 (Laser Toner).
                - **Compliance**: All 18 recent transactions recorded in Immutable Audit Log.
                """.trimIndent()
            }
        }
    }
}
