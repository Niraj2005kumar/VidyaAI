package com.vidyanova.ai.ui.screens.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.ui.components.OfflineIndicatorBadge
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.Secondary
import kotlinx.coroutines.launch

@Composable
fun TutorScreen(
    initialPrompt: String? = null,
    onBack: () -> Unit,
    onOpenScan: (() -> Unit)? = null
) {
    var inputText by remember { mutableStateOf(initialPrompt ?: "") }
    var selectedLanguage by remember { mutableStateOf("Hinglish") }
    var selectedStyle by remember { mutableStateOf(ExplanationStyle.StepByStep) }

    val languages = listOf("English", "Hindi", "Hinglish")

    val quickPrompts = listOf(
        "bhai basic se samjha",
        "2 line mein batao",
        "step by step samjhao",
        "example ke saath batao",
        "exam ke liye answer do"
    )

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Namaste! Main hoon ViyaAI, aapka offline study companion. 📚\nAap kisi bhi concept ya question ke baare mein pooch sakte hain, bina internet ke!",
                isFromUser = false,
                timestamp = "Just now",
                styleTag = "Step-by-Step"
            )
        )
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        messages.add(
            ChatMessage(
                text = userText,
                isFromUser = true,
                timestamp = "Just now"
            )
        )

        val reply = generateOfflineMockResponse(userText, selectedStyle, selectedLanguage)

        messages.add(
            ChatMessage(
                text = reply,
                isFromUser = false,
                timestamp = "Just now",
                styleTag = selectedStyle.displayName
            )
        )

        inputText = ""

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Tutor Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ViyaAI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Offline AI Tutor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OfflineIndicatorBadge()
                }

                // Language & Style Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Switcher
                    languages.forEach { lang ->
                        val isLangSelected = lang == selectedLanguage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isLangSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedLanguage = lang }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = lang,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLangSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Explanation Style Pills
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ExplanationStyle.values()) { style ->
                        val isStyleSelected = style == selectedStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isStyleSelected) Secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isStyleSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStyleSelected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Quick Natural Prompts Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { sendMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scan Button
                IconButton(
                    onClick = { onOpenScan?.invoke() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask ViyaAI anything...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice Placeholder Button
                IconButton(
                    onClick = { /* Voice input placeholder */ },
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = { sendMessage(inputText) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromUser) 18.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) Primary else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(if (message.isFromUser) 1.dp else 2.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.styleTag != null) {
                    Text(
                        text = "Mode: ${message.styleTag}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = if (message.isFromUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun generateOfflineMockResponse(
    query: String,
    style: ExplanationStyle,
    language: String
): String {
    val q = query.lowercase()

    return when {
        q.contains("basic") || style == ExplanationStyle.Basic -> {
            "Simple shabdon mein samjho: Kisi bhi concept ki shuruaat basic foundation se hoti hai.\nJaise Quadratic Equation ka standard form hota hai: ax² + bx + c = 0.\nYahan 'a' kabhi zero nahi ho sakta kyunki tab ye quadratic nahi bachega!"
        }
        q.contains("2 line") || style == ExplanationStyle.Simple -> {
            "Newton's 3rd Law ke mutabiq: Har action ka ek barabar aur opposite reaction hota hai.\nJaise jab aap zameen par chalte hain, toh aap zameen ko peeche push karte hain aur zameen aapko aage!"
        }
        q.contains("step by step") || style == ExplanationStyle.StepByStep -> {
            "Aaiye ise step-by-step solve karte hain:\nStep 1: Given equation ko standard form mein likhein.\nStep 2: Coefficients identify karein (a, b, c).\nStep 3: Discriminant D = b² - 4ac calculate karein.\nStep 4: D > 0 hai toh do real roots milenge: x = (-b ± √D) / 2a."
        }
        q.contains("example") || style == ExplanationStyle.Example -> {
            "Ek practical example dekhte hain:\nMaano ek train 60 km/h ki speed se chal rahi hai aur 2 ghante travel karti hai.\nDistance = Speed × Time\nDistance = 60 × 2 = 120 km!\nIs tarah physics daily life se judti hai."
        }
        q.contains("exam") || style == ExplanationStyle.ExamReady -> {
            "Board Exam Answer Key Format:\n• Definition (1 Mark): Photosynthesis is the biochemical process by which plants synthesize glucose from CO₂ and H₂O in presence of sunlight.\n• Equation (1 Mark): 6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂.\n• Key Organelle: Chloroplasts."
        }
        else -> {
            "ViyaAI Offline Response:\nAapka concept bilkul clear ho jayega! ViyaAI offline database mein Class 10 ke core concepts cached hain. Aap detail se pooch sakte hain ya camera se question scan kar sakte hain."
        }
    }
}
