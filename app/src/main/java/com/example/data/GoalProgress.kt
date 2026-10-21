package com.example.data

enum class GoalStatus(val label: String) {
    UNDER_BUDGET("Under Budget"),
    ON_TRACK("On Track"),
    NEAR_LIMIT("Near Limit"),
    EXCEEDED_LIMIT("Limit Exceeded")
}

enum class GoalPreset(
    val title: String,
    val description: String,
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int
) {
    BALANCED("Balanced", "30% Protein / 40% Carbs / 30% Fat", 30, 40, 30),
    HIGH_PROTEIN("High Protein", "40% Protein / 35% Carbs / 25% Fat", 40, 35, 25),
    LOW_CARB("Low Carb", "35% Protein / 20% Carbs / 45% Fat", 35, 20, 45),
    CUSTOM("Custom", "User defined limits", 0, 0, 0)
}

data class GoalProgressItem(
    val label: String,
    val current: Double,
    val limit: Double,
    val unit: String
) {
    val remaining: Double
        get() = (limit - current).coerceAtLeast(0.0)

    val overLimit: Double
        get() = (current - limit).coerceAtLeast(0.0)

    val progressFraction: Float
        get() = if (limit > 0) (current / limit).coerceIn(0.0, 1.0).toFloat() else 0f

    val percentage: Int
        get() = if (limit > 0) ((current / limit) * 100).toInt() else 0

    val status: GoalStatus
        get() = when {
            limit <= 0 -> GoalStatus.ON_TRACK
            current > limit -> GoalStatus.EXCEEDED_LIMIT
            current >= limit * 0.88 -> GoalStatus.NEAR_LIMIT
            current >= limit * 0.40 -> GoalStatus.ON_TRACK
            else -> GoalStatus.UNDER_BUDGET
        }

    val statusMessage: String
        get() = when (status) {
            GoalStatus.EXCEEDED_LIMIT -> "+${overLimit.toInt()} $unit over limit"
            GoalStatus.NEAR_LIMIT -> "${remaining.toInt()} $unit remaining (near limit)"
            GoalStatus.ON_TRACK -> "${remaining.toInt()} $unit remaining"
            GoalStatus.UNDER_BUDGET -> "${remaining.toInt()} $unit to reach target"
        }
}

data class MacroRatio(
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int
)

data class DailyGoalProgress(
    val dateKey: String = "",
    val calories: GoalProgressItem = GoalProgressItem("Calories", 0.0, 2000.0, "kcal"),
    val protein: GoalProgressItem = GoalProgressItem("Protein", 0.0, 130.0, "g"),
    val carbs: GoalProgressItem = GoalProgressItem("Carbohydrates", 0.0, 220.0, "g"),
    val fat: GoalProgressItem = GoalProgressItem("Fat", 0.0, 65.0, "g"),
    val macroRatio: MacroRatio = MacroRatio(30, 40, 30),
    val adherenceScore: Int = 100,
    val insights: List<String> = emptyList()
) {
    val isAnyLimitExceeded: Boolean
        get() = calories.status == GoalStatus.EXCEEDED_LIMIT ||
                protein.status == GoalStatus.EXCEEDED_LIMIT ||
                carbs.status == GoalStatus.EXCEEDED_LIMIT ||
                fat.status == GoalStatus.EXCEEDED_LIMIT

    val summaryStatusText: String
        get() = when {
            calories.status == GoalStatus.EXCEEDED_LIMIT -> "Calorie limit exceeded by ${calories.overLimit.toInt()} kcal"
            calories.status == GoalStatus.NEAR_LIMIT -> "Approaching daily calorie limit (${calories.remaining.toInt()} kcal left)"
            calories.current > 0 -> "On track: ${calories.remaining.toInt()} kcal remaining"
            else -> "Ready to log today's first meal"
        }

    companion object {
        fun calculate(
            dateKey: String,
            consumedCalories: Int,
            consumedProtein: Double,
            consumedCarbs: Double,
            consumedFat: Double,
            goals: UserGoals
        ): DailyGoalProgress {
            val calItem = GoalProgressItem("Calories", consumedCalories.toDouble(), goals.dailyCalories.toDouble(), "kcal")
            val pItem = GoalProgressItem("Protein", consumedProtein, goals.dailyProteinG.toDouble(), "g")
            val cItem = GoalProgressItem("Carbohydrates", consumedCarbs, goals.dailyCarbsG.toDouble(), "g")
            val fItem = GoalProgressItem("Fat", consumedFat, goals.dailyFatG.toDouble(), "g")

            // Calculate actual macro caloric contributions: Protein=4kcal/g, Carbs=4kcal/g, Fat=9kcal/g
            val pCals = consumedProtein * 4
            val cCals = consumedCarbs * 4
            val fCals = consumedFat * 9
            val totalMacroCals = (pCals + cCals + fCals).coerceAtLeast(1.0)

            val pPct = ((pCals / totalMacroCals) * 100).toInt()
            val cPct = ((cCals / totalMacroCals) * 100).toInt()
            val fPct = (100 - pPct - cPct).coerceAtLeast(0)

            // Adherence score (100 is ideal adherence without severe deficit or overshooting)
            var score = 100
            if (consumedCalories > goals.dailyCalories) {
                val excessPct = ((consumedCalories - goals.dailyCalories).toDouble() / goals.dailyCalories) * 100
                score -= (excessPct * 0.8).toInt()
            }
            if (fItem.status == GoalStatus.EXCEEDED_LIMIT) {
                score -= 10
            }
            if (cItem.status == GoalStatus.EXCEEDED_LIMIT) {
                score -= 10
            }
            score = score.coerceIn(20, 100)

            val insightsList = mutableListOf<String>()
            if (calItem.status == GoalStatus.EXCEEDED_LIMIT) {
                insightsList.add("Calorie limit surpassed by ${calItem.overLimit.toInt()} kcal.")
            } else if (calItem.status == GoalStatus.NEAR_LIMIT) {
                insightsList.add("Only ${calItem.remaining.toInt()} kcal left before reaching daily target.")
            }

            if (pItem.percentage >= 100) {
                insightsList.add("Protein goal achieved (${consumedProtein.toInt()}g / ${goals.dailyProteinG}g)!")
            } else if (pItem.remaining > 0) {
                insightsList.add("${pItem.remaining.toInt()}g protein needed to meet daily target.")
            }

            if (cItem.status == GoalStatus.EXCEEDED_LIMIT) {
                insightsList.add("Carbs exceeded by ${cItem.overLimit.toInt()}g.")
            }
            if (fItem.status == GoalStatus.EXCEEDED_LIMIT) {
                insightsList.add("Fat exceeded by ${fItem.overLimit.toInt()}g.")
            }

            return DailyGoalProgress(
                dateKey = dateKey,
                calories = calItem,
                protein = pItem,
                carbs = cItem,
                fat = fItem,
                macroRatio = MacroRatio(pPct, cPct, fPct),
                adherenceScore = score,
                insights = insightsList
            )
        }
    }
}
