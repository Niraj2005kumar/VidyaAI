package com.vidyanova.ai.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.TopicCard
import com.vidyanova.ai.ui.theme.Primary

data class ChapterTopic(
    val title: String,
    val subtopicCount: Int,
    val progress: Int,
    val isCompleted: Boolean
)

@Composable
fun TopicSelectionScreen(
    subject: String = "Mathematics",
    onBack: () -> Unit,
    onOpenTutor: (String?) -> Unit
) {
    val topics = listOf(
        ChapterTopic("Quadratic Equations", 5, 100, true),
        ChapterTopic("Arithmetic Progressions", 4, 75, false),
        ChapterTopic("Coordinate Geometry", 6, 40, false),
        ChapterTopic("Triangles & Similarity", 7, 20, false),
        ChapterTopic("Introduction to Trigonometry", 5, 0, false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "$subject Topics",
            subtitle = "Chapter 1 - 5",
            onBack = onBack,
            showOfflineBadge = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(topics) { topic ->
                TopicCard(
                    title = topic.title,
                    topicCount = topic.subtopicCount,
                    progress = topic.progress,
                    completed = topic.isCompleted,
                    onClick = { onOpenTutor(topic.title) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onOpenTutor(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Ask ViyaAI About These Topics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
