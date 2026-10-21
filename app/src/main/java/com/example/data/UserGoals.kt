package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class UserGoals(
    val dailyCalories: Int = 2000,
    val dailyProteinG: Int = 130,
    val dailyCarbsG: Int = 220,
    val dailyFatG: Int = 65
)

class GoalsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nutrisnap_user_goals", Context.MODE_PRIVATE)

    fun getGoals(): UserGoals {
        return UserGoals(
            dailyCalories = prefs.getInt("daily_calories", 2000),
            dailyProteinG = prefs.getInt("daily_protein", 130),
            dailyCarbsG = prefs.getInt("daily_carbs", 220),
            dailyFatG = prefs.getInt("daily_fat", 65)
        )
    }

    fun saveGoals(goals: UserGoals) {
        prefs.edit()
            .putInt("daily_calories", goals.dailyCalories)
            .putInt("daily_protein", goals.dailyProteinG)
            .putInt("daily_carbs", goals.dailyCarbsG)
            .putInt("daily_fat", goals.dailyFatG)
            .apply()
    }
}
