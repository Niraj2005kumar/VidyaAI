package com.vidyanova.ai.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.data.local.database.AppDatabase
import com.vidyanova.ai.data.local.datastore.UserPreferences
import com.vidyanova.ai.data.local.sync.SyncWorker
import com.vidyanova.ai.data.repository.AuthRepository
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.BottomNavigationBar
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.Secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userPrefs = remember(context) { UserPreferences(context) }
    val db = remember(context) { AppDatabase.getInstance(context) }

    val syncConsent by userPrefs.syncConsentFlow.collectAsState(initial = false)
    val lastSyncTime by userPrefs.lastSyncTimeFlow.collectAsState(initial = 0L)
    val studentName by userPrefs.studentNameFlow.collectAsState(initial = "Guest Student")
    val classLevel by userPrefs.classLevelFlow.collectAsState(initial = 10)
    val selectedLanguage by userPrefs.preferredLanguageFlow.collectAsState(initial = "Hinglish")
    val selectedExplanationStyle by userPrefs.preferredStyleFlow.collectAsState(initial = "Step-by-Step")

    var showConsentDialog by remember { mutableStateOf(false) }
    var showClassDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(studentName) }

    var selectedTheme by remember { mutableStateOf("System Default") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val languages = listOf("English", "Hindi", "Hinglish")
    val themes = listOf("Light", "Dark", "System Default")
    val styles = listOf("Simple", "Detailed", "Step-by-Step", "Exam Ready")

    fun formatLastSync(timeMs: Long): String {
        if (timeMs == 0L) return "Never synced"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMs))
    }

    // Edit Profile Name Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Name", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (editNameInput.isNotBlank()) {
                                userPrefs.saveStudentName(editNameInput.trim())
                            }
                            showEditProfileDialog = false
                            Toast.makeText(context, "Student name updated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Class Selector Dialog
    if (showClassDialog) {
        AlertDialog(
            onDismissRequest = { showClassDialog = false },
            title = { Text("Select Class Level", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    (1..10).forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        userPrefs.saveClassLevel(level)
                                        showClassDialog = false
                                        Toast.makeText(context, "Class $level selected!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Class $level", fontSize = 16.sp, fontWeight = if (level == classLevel) FontWeight.Bold else FontWeight.Normal)
                            if (level == classLevel) {
                                Text("✓", color = Primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClassDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

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
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editNameInput = studentName
                                        showEditProfileDialog = true
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Tap to edit student name", style = MaterialTheme.typography.bodySmall, color = Primary)
                                }
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Primary)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClassDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Class $classLevel • Secondary Level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Tap to change class level", style = MaterialTheme.typography.bodySmall, color = Primary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary)
                            }
                        }
                    }
                }

                // Section: Cloud Sync & Backup
                item {
                    SettingsCard(title = "Cloud Sync & Backup", icon = Icons.Default.CloudSync) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Auto Cloud Sync", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(text = "Backup progress to MongoDB server", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = syncConsent == true,
                                onCheckedChange = { consent ->
                                    coroutineScope.launch {
                                        userPrefs.saveSyncConsent(consent)
                                        if (consent) {
                                            SyncWorker.scheduleOneTimeSync(context)
                                            Toast.makeText(context, "Cloud sync enabled!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Cloud sync disabled.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last synced: ${formatLastSync(lastSyncTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        userPrefs.saveSyncConsent(true)
                                        SyncWorker.scheduleOneTimeSync(context)
                                        Toast.makeText(context, "Syncing with cloud started...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section: Learning Preferences
                item {
                    SettingsCard(title = "Learning", icon = Icons.Default.School) {
                        SettingsRow(label = "Target Goal", value = "60 min / day")
                        Spacer(modifier = Modifier.height(10.dp))
                        SettingsRow(label = "Class Level", value = "Class $classLevel")
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
                                val isSelected = lang.equals(selectedLanguage, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            coroutineScope.launch {
                                                userPrefs.savePreferredLanguage(lang)
                                                Toast.makeText(context, "Language set to $lang", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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
                                val isSelected = style.equals(selectedExplanationStyle, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            coroutineScope.launch {
                                                userPrefs.savePreferredStyle(style)
                                                Toast.makeText(context, "Explanation style set to $style", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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

                // Section: Storage & Clear Data
                item {
                    SettingsCard(title = "Storage & Data", icon = Icons.Default.Storage) {
                        SettingsRow(label = "Cached Local Database", value = "Room DB Active")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        db.clearAllTables()
                                        userPrefs.clearAllData()
                                        launch(Dispatchers.Main) {
                                            Toast.makeText(context, "All local data cleared successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        launch(Dispatchers.Main) {
                                            Toast.makeText(context, "Error clearing data: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear All My Local Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section: Account & Logout
                item {
                    SettingsCard(title = "Account & Logout", icon = Icons.AutoMirrored.Filled.Logout) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    AuthRepository.logout()
                                    userPrefs.clearAllData()
                                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                                    onNavigate?.invoke("login")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
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
