package com.vidyanova.ai.ui.screens.subject

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.ui.models.SubjectItem
import com.vidyanova.ai.ui.theme.CatBankBg
import com.vidyanova.ai.ui.theme.CatBankIcon
import com.vidyanova.ai.ui.theme.CatBcsBg
import com.vidyanova.ai.ui.theme.CatBcsIcon
import com.vidyanova.ai.ui.theme.CatJudiciaryBg
import com.vidyanova.ai.ui.theme.CatJudiciaryIcon
import com.vidyanova.ai.ui.theme.CatOthersBg
import com.vidyanova.ai.ui.theme.CatOthersIcon
import com.vidyanova.ai.ui.theme.CatPrimarySchoolBg
import com.vidyanova.ai.ui.theme.CatPrimarySchoolIcon
import com.vidyanova.ai.ui.theme.CatUniBg
import com.vidyanova.ai.ui.theme.CatUniIcon
import com.vidyanova.ai.ui.theme.HeaderGradientBottom
import com.vidyanova.ai.ui.theme.HeaderGradientTop
import com.vidyanova.ai.ui.theme.Primary
import com.vidyanova.ai.ui.theme.PrimaryText
import com.vidyanova.ai.ui.theme.SecondaryText

@Composable
fun SubjectListScreen(
    categoryName: String = "BCS",
    onBack: () -> Unit,
    onSelectSubject: (String) -> Unit
) {
    var selectedToggleTab by remember { mutableStateOf("subject_wise") }

    val subjects = listOf(
        SubjectItem("bangla1", "Bangla", "Total Test : 100+", CatPrimarySchoolBg, CatPrimarySchoolIcon, Icons.Default.MenuBook),
        SubjectItem("english", "English", "Total Test : 100+", CatBcsBg, CatBcsIcon, Icons.Default.Language),
        SubjectItem("math", "Mathematics", "Total Test : 100+", CatUniBg, CatUniIcon, Icons.Default.Calculate),
        SubjectItem("physics", "Physics", "Total Test : 100+", CatBankBg, CatBankIcon, Icons.Default.Science),
        SubjectItem("chemistry", "Chemistry", "Total Test : 100+", CatJudiciaryBg, CatJudiciaryIcon, Icons.Default.Biotech),
        SubjectItem("biology", "Biological Science", "Total Test : 100+", CatOthersBg, CatOthersIcon, Icons.Default.Psychology),
        SubjectItem("gk", "General Knowledge", "Total Test : 100+", CatUniBg, CatUniIcon, Icons.Default.Lightbulb)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Decorative Top-Right Orange Shape Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(width * 0.4f, 0f)
                quadraticTo(width * 0.6f, height * 0.9f, width, height * 0.7f)
                lineTo(width, 0f)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(HeaderGradientTop, HeaderGradientBottom)
                )
            )

            // Decorative shapes inside top-right corner
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 30f,
                center = Offset(width - 50f, 60f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Back Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Screen Titles
            Text(
                text = "$categoryName New Question Practice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select a subject to start practicing questions and test your knowledge.",
                fontSize = 13.sp,
                color = SecondaryText,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2-Tab Toggle Pills
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    // Subject Wise Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedToggleTab == "subject_wise") Primary else Color.Transparent)
                            .clickable { selectedToggleTab = "subject_wise" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Subject Wise",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedToggleTab == "subject_wise") Color.White else PrimaryText
                        )
                    }

                    // All Subject Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedToggleTab == "all_subject") Primary else Color.Transparent)
                            .clickable { selectedToggleTab = "all_subject" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All Subject",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedToggleTab == "all_subject") Color.White else PrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects) { subject ->
                    SubjectRowCard(
                        subject = subject,
                        onClick = { onSelectSubject(subject.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectRowCard(
    subject: SubjectItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pastel Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(subject.pastelBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subject.icon,
                    contentDescription = subject.name,
                    tint = subject.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center Subject Name & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subject.totalTests,
                    fontSize = 12.sp,
                    color = SecondaryText
                )
            }

            // Right Play Arrow Icon Button
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Test",
                    tint = PrimaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
