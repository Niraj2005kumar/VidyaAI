package com.vidyanova.ai.ui.screens.opportunities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidyanova.ai.ui.components.AppTopBar
import com.vidyanova.ai.ui.components.OpportunityCard
import com.vidyanova.ai.ui.theme.Primary

data class OpportunityItem(
    val id: String,
    val title: String,
    val organization: String,
    val deadline: String,
    val category: String,
    val status: String
)

@Composable
fun OpportunitiesScreen(
    onBack: () -> Unit,
    onOpenOpportunity: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All",
        "Scholarships",
        "Exams",
        "Competitions",
        "Hackathons",
        "Educational Programs"
    )

    val opportunities = remember {
        listOf(
            OpportunityItem(
                id = "1",
                title = "National Talent Search Examination (NTSE)",
                organization = "NCERT Government of India",
                deadline = "15 Nov 2026",
                category = "Scholarships",
                status = "Open"
            ),
            OpportunityItem(
                id = "2",
                title = "KVPY Science Fellowship",
                organization = "Department of Science and Technology",
                deadline = "30 Nov 2026",
                category = "Scholarships",
                status = "Open"
            ),
            OpportunityItem(
                id = "3",
                title = "National Science Olympiad (NSO)",
                organization = "Science Olympiad Foundation",
                deadline = "25 Oct 2026",
                category = "Competitions",
                status = "Closing Soon"
            ),
            OpportunityItem(
                id = "4",
                title = "Smart India Hackathon Junior",
                organization = "Ministry of Education AICTE",
                deadline = "05 Dec 2026",
                category = "Hackathons",
                status = "Upcoming"
            ),
            OpportunityItem(
                id = "5",
                title = "CBSE Science Exhibition 2026",
                organization = "Central Board of Secondary Education",
                deadline = "10 Nov 2026",
                category = "Educational Programs",
                status = "Open"
            )
        )
    }

    val filteredList = if (selectedCategory == "All") {
        opportunities
    } else {
        opportunities.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(
            title = "Opportunity Alerts",
            subtitle = "Scholarships & Competitions",
            onBack = onBack,
            showOfflineBadge = true
        )

        // Categories Horizontal Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Primary else MaterialTheme.colorScheme.surface
                        )
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // List of Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredList) { opp ->
                OpportunityCard(
                    title = opp.title,
                    organization = opp.organization,
                    deadline = opp.deadline,
                    category = opp.category,
                    status = opp.status,
                    onClick = onOpenOpportunity
                )
            }
        }
    }
}
