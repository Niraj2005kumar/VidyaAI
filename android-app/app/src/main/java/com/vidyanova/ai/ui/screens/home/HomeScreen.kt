package com.vidyanova.ai.ui.screens.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.ui.components.AppLogoIcon
import com.vidyanova.ai.ui.components.ProgressCard
import com.vidyanova.ai.ui.components.SubjectCard
import com.vidyanova.ai.ui.models.ModelExamItem
import com.vidyanova.ai.ui.theme.BannerOrange
import com.vidyanova.ai.ui.theme.BannerPurple
import com.vidyanova.ai.ui.theme.HeaderGradientBottom
import com.vidyanova.ai.ui.theme.HeaderGradientTop
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.PrimaryText
import com.vidyanova.ai.ui.theme.Secondary
import com.vidyanova.ai.ui.theme.SecondaryText
import com.vidyanova.ai.ui.theme.Success
import com.vidyanova.ai.ui.theme.Warning
import kotlinx.coroutines.launch

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val quickActions = listOf(
        QuickActionItem("Ask ViyaAI", Icons.Default.AutoAwesome, Primary, "tutor"),
        QuickActionItem("Scan & Learn", Icons.Default.CameraAlt, Secondary, "scanLearn"),
        QuickActionItem("Practice Quiz", Icons.Default.Quiz, Color(0xFF0284C7), "quiz"),
        QuickActionItem("My Progress", Icons.Default.Leaderboard, Success, "progress"),
        QuickActionItem("Focus Mode", Icons.Default.HourglassTop, Warning, "focusMode"),
        QuickActionItem("Opportunities", Icons.Default.Star, Color(0xFFD97706), "opportunities"),
        QuickActionItem("Teacher Slip", Icons.Default.Description, Color(0xFF0D9488), "teacherSlip"),
        QuickActionItem("Parent Zone", Icons.Default.Security, Color(0xFF475569), "parentPin")
    )

    val modelExams = listOf(
        ModelExamItem("1", "41st BCS Model Test", "Full syllabus coverage & practice", BannerOrange),
        ModelExamItem("2", "42nd BCS Model Test", "Special medical & general test", BannerPurple),
        ModelExamItem("3", "Primary Teacher Exam", "Complete primary mock test", BannerOrange)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogoIcon(modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VidyaAI Menu",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = true,
                        onClick = { coroutineScope.launch { drawerState.close() } }
                    )
                    NavigationDrawerItem(
                        label = { Text("ViyaAI Tutor") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateTo("tutor")
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("My Progress") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateTo("progress")
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateTo("settings")
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateTo("tutor") },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask ViyaAI",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask ViyaAI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            bottomBar = {
                HomeBottomNavigationBar(
                    selectedTab = "home",
                    onSelectTab = { tab ->
                        when (tab) {
                            "home" -> {}
                            "subjects" -> onNavigateTo("subjects")
                            "viya_ai" -> onNavigateTo("tutor")
                            "progress" -> onNavigateTo("progress")
                            "settings" -> onNavigateTo("settings")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(innerPadding)
            ) {
                // Curved Orange Header Banner
                item {
                    HeaderBanner(
                        onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                    )
                }

                // Quick Actions Section
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Quick Actions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(quickActions) { action ->
                                Card(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .shadow(3.dp, RoundedCornerShape(16.dp))
                                        .clickable { onNavigateTo(action.route) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(action.color.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = action.title,
                                                tint = action.color,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = action.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryText,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Today's Progress Grid Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            text = "Today's Progress",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProgressCard(
                                title = "Study Time",
                                value = "45m",
                                subtitle = "Goal: 60m",
                                accent = Primary,
                                modifier = Modifier.weight(1f)
                            )
                            ProgressCard(
                                title = "Topics Learned",
                                value = "3",
                                subtitle = "Science & Math",
                                accent = Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProgressCard(
                                title = "Quiz Score",
                                value = "88%",
                                subtitle = "Last: Physics",
                                accent = Success,
                                modifier = Modifier.weight(1f)
                            )
                            ProgressCard(
                                title = "Current Streak",
                                value = "5 Days",
                                subtitle = "Personal Best 🔥",
                                accent = Warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Subjects Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subjects",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = "View All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.clickable { onNavigateTo("subjects") }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SubjectCard(
                            name = "Mathematics",
                            chapters = 15,
                            progress = 72,
                            icon = Icons.Default.Calculate,
                            accentColor = Primary,
                            onClick = { onNavigateTo("subjects") }
                        )
                        SubjectCard(
                            name = "Science",
                            chapters = 16,
                            progress = 58,
                            icon = Icons.Default.Science,
                            accentColor = Secondary,
                            onClick = { onNavigateTo("subjects") }
                        )
                        SubjectCard(
                            name = "English",
                            chapters = 12,
                            progress = 85,
                            icon = Icons.Default.Language,
                            accentColor = Color(0xFF0284C7),
                            onClick = { onNavigateTo("subjects") }
                        )
                    }
                }

                // Model Exam Section
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Explore our new model exam",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(modelExams) { exam ->
                                ModelExamCard(
                                    item = exam,
                                    onClick = { onNavigateTo("quiz") }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderBanner(
    onOpenMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // Curved Wave Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                lineTo(0f, height - 70f)
                cubicTo(
                    width * 0.35f, height + 10f,
                    width * 0.65f, height - 120f,
                    width, height - 40f
                )
                lineTo(width, 0f)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(HeaderGradientTop, HeaderGradientBottom)
                )
            )

            // Top-Right Orange Abstract Shape
            drawCircle(
                color = Color(0xFFFF9800).copy(alpha = 0.4f),
                radius = 110f,
                center = Offset(width - 20f, 30f)
            )
            drawCircle(
                color = Color(0xFFFFB74D).copy(alpha = 0.3f),
                radius = 70f,
                center = Offset(width - 60f, 80f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header Top Bar with Golden V Emblem Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenMenu) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = PrimaryText,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppLogoIcon(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VidyaAI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column Text
                Column(modifier = Modifier.weight(1.0f)) {
                    val annotatedText = buildAnnotatedString {
                        append("You can ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryText)) {
                            append("start your\n")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryText)) {
                            append("test")
                        }
                        append(" here")
                    }

                    Text(
                        text = annotatedText,
                        fontSize = 20.sp,
                        color = PrimaryText,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create your own Test, keep practicing & stay on top for your next exam",
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }

                // Right Illustration (Girl studying on laptop at desk with books)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(200.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    StudyIllustration()
                }
            }
        }
    }
}

@Composable
fun StudyIllustration() {
    Canvas(modifier = Modifier.size(200.dp, 190.dp)) {
        val width = size.width
        val height = size.height

        val deskY = height - 38f

        // 1. Chair (White Backrest and Support)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(width - 70f, height - 130f),
            size = Size(40f, 60f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRect(
            color = Color(0xFFB0BEC5),
            topLeft = Offset(width - 56f, height - 70f),
            size = Size(10f, 32f)
        )

        // 2. Girl Sitting at Chair (Hair, Face, Orange Shirt, Dark Green Pants)
        // Hair (Black)
        drawCircle(
            color = Color(0xFF212121),
            radius = 28f,
            center = Offset(width - 50f, height - 148f)
        )
        // Face / Neck (Skin Tone)
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = 20f,
            center = Offset(width - 58f, height - 144f)
        )
        // Hair Ponytail
        drawOval(
            color = Color(0xFF212121),
            topLeft = Offset(width - 32f, height - 158f),
            size = Size(20f, 36f)
        )
        // Orange Top Shirt
        drawRoundRect(
            color = Color(0xFFF4511E),
            topLeft = Offset(width - 74f, height - 120f),
            size = Size(42f, 48f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Dark Green/Teal Pants
        drawRoundRect(
            color = Color(0xFF00695C),
            topLeft = Offset(width - 80f, height - 72f),
            size = Size(32f, 60f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // 3. Wooden Table
        // Tabletop
        drawRoundRect(
            color = Color(0xFF795548),
            topLeft = Offset(5f, deskY),
            size = Size(width - 20f, 14f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Table Legs
        drawRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(20f, deskY + 14f),
            size = Size(12f, 24f)
        )
        drawRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(width - 55f, deskY + 14f),
            size = Size(12f, 24f)
        )

        // 4. Laptop on Table
        // Base
        drawRoundRect(
            color = Color(0xFF455A64),
            topLeft = Offset(82f, deskY - 6f),
            size = Size(60f, 6f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Screen Lid
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(90f, deskY - 50f),
            size = Size(48f, 44f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Screen Display
        drawRect(
            color = Color(0xFFECEFF1),
            topLeft = Offset(93f, deskY - 47f),
            size = Size(42f, 38f)
        )

        // 5. Stack of Books on Table
        drawRoundRect(
            color = Color(0xFF00897B),
            topLeft = Offset(18f, deskY - 11f),
            size = Size(46f, 11f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFFE53935),
            topLeft = Offset(21f, deskY - 22f),
            size = Size(40f, 11f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFF1E88E5),
            topLeft = Offset(19f, deskY - 33f),
            size = Size(43f, 11f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@Composable
fun ModelExamCard(
    item: ModelExamItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Top Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(item.bannerColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VidyaAI Model Test",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Card Body
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = SecondaryText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeBottomNavigationBar(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("home", "Home", Icons.Default.Home),
                Triple("subjects", "Subjects", Icons.AutoMirrored.Filled.MenuBook),
                Triple("viya_ai", "ViyaAI", Icons.Default.AutoAwesome),
                Triple("progress", "Progress", Icons.Default.Leaderboard),
                Triple("settings", "Settings", Icons.Default.Settings)
            )

            tabs.forEach { (route, label, icon) ->
                val isSelected = selectedTab == route
                val tint = if (isSelected) Primary else SecondaryText

                Column(
                    modifier = Modifier
                        .clickable { onSelectTab(route) }
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = tint
                    )
                }
            }
        }
    }
}
