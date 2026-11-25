package com.vidyanova.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_opportunities")
data class CachedOpportunityEntity(
    @PrimaryKey
    val opportunityId: String,
    val title: String,
    val description: String,
    val type: String = "scholarship",
    val deadline: String = "",
    val applyUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
