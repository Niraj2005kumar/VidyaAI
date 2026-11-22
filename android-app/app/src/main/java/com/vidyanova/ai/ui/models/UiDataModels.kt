package com.vidyanova.ai.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val pastelBg: Color,
    val iconColor: Color
)

data class SubjectItem(
    val id: String,
    val name: String,
    val totalTests: String = "Total Test : 100+",
    val pastelBg: Color,
    val iconColor: Color,
    val icon: ImageVector
)

data class ModelExamItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val bannerColor: Color
)
