package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val displayName: String, val iconName: String) {
    BREAKFAST("Breakfast", "wb_sunny"),
    LUNCH("Lunch", "restaurant"),
    DINNER("Dinner", "nights_stay"),
    SNACK("Snack", "coffee")
}

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // e.g., "2026-10-20"
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK
    val title: String,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val portionDescription: String = "1 standard serving",
    val itemsSummary: String = "",
    val advice: String = "",
    val imageUri: String? = null
)
