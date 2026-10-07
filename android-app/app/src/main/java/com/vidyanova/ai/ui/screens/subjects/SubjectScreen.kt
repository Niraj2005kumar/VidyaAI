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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.SubjectCard
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.Secondary

data class SubjectItem(
    val name: String,
    val chapters: Int,
    val progress: Int,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun SubjectScreen(
    onBack: () -> Unit,
    onOpenTopic: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val subjects = remember {
        listOf(
            SubjectItem("Mathematics", 15, 72, Icons.Default.Calculate, Primary),
            SubjectItem("Science", 16, 58, Icons.Default.Science, Secondary),
            SubjectItem("English", 12, 85, Icons.Default.Language, Color(0xFF0284C7)),
            SubjectItem("Social Science", 18, 45, Icons.Default.Public, Color(0xFFD97706)),
            SubjectItem("Computer Science", 10, 90, Icons.Default.Computer, Color(0xFF0D9488)),
            SubjectItem("Hindi", 14, 65, Icons.Default.Book, Color(0xFFE11D48))
        )
    }

    val filteredSubjects = subjects.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Subjects",
            subtitle = "Class 10 Curriculum",
            onBack = onBack,
            showOfflineBadge = true
        )

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search subjects...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredSubjects) { subject ->
                SubjectCard(
                    name = subject.name,
                    chapters = subject.chapters,
                    progress = subject.progress,
                    icon = subject.icon,
                    accentColor = subject.accentColor,
                    onClick = onOpenTopic
                )
            }
        }
    }
}
