package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.GeminiProcurementAssistant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AIAssistantViewModel provides procurement-related AI insights, predictions,
 * and executive recommendations powered by Gemini API (utilizing BuildConfig.GEMINI_API_KEY).
 */
class AIAssistantViewModel(
    private val geminiAssistant: GeminiProcurementAssistant = GeminiProcurementAssistant()
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "Hello! I am your SmartFlow Procurement AI Copilot powered by Gemini. Ask me about spend bottlenecks, RFQ generation, 3-Way invoice compliance, or supplier performance recommendations." to false
        )
    )
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _recommendedTopics = MutableStateFlow(
        listOf(
            "Analyze department spend bottlenecks",
            "Evaluate top 3 suppliers & ratings",
            "Draft RFQ specification for IT Workstations",
            "Explain 3-Way Matching tolerance rules",
            "Forecast Q3 budget allocation"
        )
    )
    val recommendedTopics: StateFlow<List<String>> = _recommendedTopics.asStateFlow()

    /**
     * Sends a user question to Gemini AI service along with live database context summary.
     */
    fun sendAiQuery(userQuery: String, contextDataSummary: String = "") {
        if (userQuery.isBlank()) return

        val currentList = _chatMessages.value.toMutableList()
        currentList.add(userQuery to true)
        _chatMessages.value = currentList

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val reply = geminiAssistant.queryProcurementAi(userQuery, contextDataSummary)
                val updated = _chatMessages.value.toMutableList()
                updated.add(reply to false)
                _chatMessages.value = updated
            } catch (e: Exception) {
                val updated = _chatMessages.value.toMutableList()
                updated.add("Error connecting to Gemini API: ${e.localizedMessage ?: "Unknown error"}" to false)
                _chatMessages.value = updated
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the current chat history back to welcome state.
     */
    fun clearChat() {
        _chatMessages.value = listOf(
            "Chat history cleared. I am ready to assist with your next procurement inquiry." to false
        )
    }

    /**
     * Quick action to trigger predefined executive procurement insights.
     */
    fun generateExecutiveInsight(topic: String, contextDataSummary: String = "") {
        sendAiQuery("Provide executive analysis for: $topic", contextDataSummary)
    }
}
