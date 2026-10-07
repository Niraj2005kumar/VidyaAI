package com.vidyanova.ai.ui.screens.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.BottomNavigationBar
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.Secondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: ((String) -> Unit)? = null
) {
    var selectedLanguage by remember { mutableStateOf("Hinglish") }
    var selectedTheme by remember { mutableStateOf("System Default") }
    var selectedExplanationStyle by remember { mutableStateOf("Step-by-Step") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val languages = listOf("English", "Hindi", "Hinglish")
    val themes = listOf("Light", "Dark", "System Default")
    val styles = listOf("Simple", "Detailed", "Step-by-Step", "Exam Ready")

    Scaffold(
        bottomBar = {
            if (onNavigate != null) {
                BottomNavigationBar(currentRoute = "settings", onNavigate = onNavigate)
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
                title = "Settings",
                subtitle = "App & Learning Preferences",
                onBack = onBack,
                showOfflineBadge = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Section: Profile
                item {
                    SettingsCard(title = "Profile", icon = Icons.Default.Person) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Aarav Sharma", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "Class 10 • Secondary Level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Section: Learning Preferences
                item {
                    SettingsCard(title = "Learning", icon = Icons.Default.School) {
                        SettingsRow(label = "Target Goal", value = "60 min / day")
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(label = "Target Exam", value = "Class 10 CBSE Boards")
                    }
                }

                // Section: Language
                item {
                    SettingsCard(title = "Language", icon = Icons.Default.Language) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            languages.forEach { lang ->
                                val isSelected = lang == selectedLanguage
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedLanguage = lang }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Explanation Style
                item {
                    SettingsCard(title = "Explanation Style", icon = Icons.Default.Psychology) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            styles.forEach { style ->
                                val isSelected = style == selectedExplanationStyle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedExplanationStyle = style }
                                        .background(
                                            if (isSelected) Secondary.copy(alpha = 0.1f) else Color.Transparent
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = style,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Secondary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text(text = "✓", color = Secondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Appearance
                item {
                    SettingsCard(title = "Appearance", icon = Icons.Default.DarkMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themes.forEach { themeName ->
                                val isSelected = themeName == selectedTheme
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedTheme = themeName }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = themeName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Notifications
                item {
                    SettingsCard(title = "Notifications", icon = Icons.Default.Notifications) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Daily Study Reminders", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = "Keep streaks and goals alive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
                            )
                        }
                    }
                }

                // Section: Parent Controls
                item {
                    SettingsCard(title = "Parent Controls", icon = Icons.Default.Security) {
                        SettingsRow(label = "Security PIN", value = "Configured (••••)")
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(label = "Daily Screen Limit", value = "2 Hours")
                    }
                }

                // Section: Storage
                item {
                    SettingsCard(title = "Storage", icon = Icons.Default.Storage) {
                        SettingsRow(label = "Cached Model & Lessons", value = "245 MB")
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Clear Cache",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                            modifier = Modifier.clickable { /* Clear Cache */ }
                        )
                    }
                }

                // Section: About
                item {
                    SettingsCard(title = "About VidyaNova", icon = Icons.Default.Info) {
                        Text(
                            text = "VidyaNova v1.0.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Offline AI Tutor for Class 1 to 10 students.\n\"Learn anywhere. Learn offline.\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Primary)
    }
}
