package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.components.UniqueSmartFlowLoader
import com.example.viewmodel.AIAssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    viewModel: AIAssistantViewModel,
    contextDataSummary: String = "",
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recommendedTopics by viewModel.recommendedTopics.collectAsState()

    AiAssistantScreenContent(
        messages = messages,
        isLoading = isLoading,
        recommendedTopics = recommendedTopics,
        onSendMessage = { query -> viewModel.sendAiQuery(query, contextDataSummary) },
        onClearChat = { viewModel.clearChat() },
        modifier = modifier
    )
}

@Composable
fun AiAssistantScreen(
    messages: List<Pair<String, Boolean>>, // text to isUser
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AiAssistantScreenContent(
        messages = messages,
        isLoading = isLoading,
        recommendedTopics = listOf(
            "Analyze spend bottlenecks",
            "Draft RFQ specifications",
            "Evaluate top suppliers",
            "Explain 3-Way matching policy"
        ),
        onSendMessage = onSendMessage,
        onClearChat = null,
        modifier = modifier
    )
}

@Composable
private fun AiAssistantScreenContent(
    messages: List<Pair<String, Boolean>>,
    isLoading: Boolean,
    recommendedTopics: List<String>,
    onSendMessage: (String) -> Unit,
    onClearChat: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ai_assistant_viewmodel_chat"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "AI Procurement Assistant",
                subtitle = "Gemini Enterprise Copilot with Live MongoDB Context"
            )
            if (onClearChat != null && messages.isNotEmpty()) {
                IconButton(onClick = onClearChat, modifier = Modifier.testTag("clear_chat_btn")) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.outline)
                }
            }
        }

        // AI Assistant Status Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2027)),
            modifier = Modifier.fillMaxWidth().testTag("ai_assistant_banner")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF13AA52)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Copilot",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SmartFlow Copilot (gemini-3.5-flash)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = "Connected via GEMINI_API_KEY | MongoDB Ledger & Room SQLite",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA7F3D0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }

        // Quick Suggestion Chips
        Text("Suggested Analysis Prompts:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            recommendedTopics.take(2).forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onSendMessage(prompt) },
                    label = { Text(prompt, fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    modifier = Modifier.testTag("ai_quick_prompt_chip")
                )
            }
        }
        if (recommendedTopics.size > 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recommendedTopics.drop(2).take(2).forEach { prompt ->
                    FilterChip(
                        selected = false,
                        onClick = { onSendMessage(prompt) },
                        label = { Text(prompt, fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        modifier = Modifier.testTag("ai_quick_prompt_chip")
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Chat Conversation List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { (text, isUser) ->
                ChatBubble(text = text, isUser = isUser)
            }

            if (isLoading) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("ai_loading_card")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UniqueSmartFlowLoader(
                                size = 80.dp,
                                title = "SmartFlow Copilot Thinking...",
                                subtitle = "Querying MongoDB Cluster & Analyzing Ledger"
                            )
                        }
                    }
                }
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                placeholder = { Text("Ask SmartFlow AI anything...") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        onSendMessage(textToSend)
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("ai_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    isUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
