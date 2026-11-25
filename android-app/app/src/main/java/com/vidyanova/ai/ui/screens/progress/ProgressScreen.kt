package com.vidyanova.ai.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.data.local.database.AppDatabase
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.BottomNavigationBar
import com.vidyanova.ai.ui.components.ProgressCard
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.Secondary
import com.vidyanova.ai.ui.theme.Success
import com.vidyanova.ai.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SubjectMetric(
    val name: String,
    val progress: Int,
    val color: Color
)

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onNavigate: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context) }

    val totalStudyMinutes by db.studySessionDao().getTotalStudyTimeMinutesFlow().collectAsState(initial = 45)
    val topicsCount by db.topicProgressDao().getTopicsLearnedCountFlow().collectAsState(initial = 3)
    val avgQuizScore by db.quizAttemptDao().getAverageQuizScoreFlow().collectAsState(initial = 88.0)
    val quizAttempts by db.quizAttemptDao().getAllAttemptsFlow().collectAsState(initial = emptyList())

    val studyMinutesDisplay = totalStudyMinutes ?: 45
    val hours = studyMinutesDisplay / 60
    val mins = studyMinutesDisplay % 60
    val formattedStudyTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    val formattedAvgScore = String.format(Locale.getDefault(), "%.0f%%", avgQuizScore ?: 88.0)

    val subjectProgress = listOf(
        SubjectMetric("Mathematics", 82, Primary),
        SubjectMetric("Science", 74, Secondary),
        SubjectMetric("English", 90, Color(0xFF0284C7)),
        SubjectMetric("Social Science", 65, Color(0xFFD97706))
    )

    Scaffold(
        bottomBar = {
            if (onNavigate != null) {
                BottomNavigationBar(currentRoute = "progress", onNavigate = onNavigate)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            AppTopBar(
                title = "My Progress",
                subtitle = "Student Learning Analytics",
                onBack = onBack,
                showOfflineBadge = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Key Progress Metrics Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProgressCard(
                                title = "Total Study Time",
                                value = formattedStudyTime,
                                subtitle = "This Month",
                                accent = Primary,
                                icon = Icons.Default.AccessTime,
                                modifier = Modifier.weight(1f)
                            )
                            ProgressCard(
                                title = "Topics Learned",
                                value = "$topicsCount",
                                subtitle = "Across subjects",
                                accent = Secondary,
                                icon = Icons.Default.AssignmentTurnedIn,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProgressCard(
                                title = "Quiz Average",
                                value = formattedAvgScore,
                                subtitle = "Overall Score",
                                accent = Success,
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            ProgressCard(
                                title = "Current Streak",
                                value = "5 Days",
                                subtitle = "Keep the flame lit! 🔥",
                                accent = Warning,
                                icon = Icons.Default.LocalFireDepartment,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Subject Progress Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Subject Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            subjectProgress.forEach { item ->
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.progress}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = item.color
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { item.progress / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = item.color,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Quiz Attempts Section
                if (quizAttempts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Recent Quiz Attempts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                quizAttempts.take(5).forEach { attempt ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = attempt.quizTitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Score: ${attempt.score}/${attempt.totalQuestions} (${String.format(Locale.getDefault(), "%.0f%%", attempt.percentage)})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = sdf.format(Date(attempt.completedAt)),
                                            fontSize = 11.sp,
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
    }
}
