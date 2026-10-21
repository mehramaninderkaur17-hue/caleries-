package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyGoalProgress
import com.example.data.GoalPreset
import com.example.data.GoalsRepository
import com.example.data.NutriDatabase
import com.example.data.UserGoals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GoalTrackingState(
    val goals: UserGoals = UserGoals(),
    val progress: DailyGoalProgress = DailyGoalProgress(),
    val activePreset: GoalPreset = GoalPreset.BALANCED,
    val selectedDateKey: String = "",
    val feedbackMessage: String? = null
)

class GoalTrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val goalsRepo = GoalsRepository(application)
    private val database = NutriDatabase.getDatabase(application)
    private val mealDao = database.mealDao()

    private val dateFormatKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _state = MutableStateFlow(
        GoalTrackingState(
            goals = goalsRepo.getGoals(),
            selectedDateKey = dateFormatKey.format(Date())
        )
    )
    val state: StateFlow<GoalTrackingState> = _state.asStateFlow()

    init {
        observeMealsAndRecalculate()
    }

    fun setDateKey(dateKey: String) {
        _state.update { it.copy(selectedDateKey = dateKey) }
        observeMealsAndRecalculate()
    }

    private fun observeMealsAndRecalculate() {
        viewModelScope.launch {
            val key = _state.value.selectedDateKey
            mealDao.getMealsForDate(key).collectLatest { meals ->
                val totalCal = meals.sumOf { it.calories }
                val totalP = meals.sumOf { it.proteinGrams }
                val totalC = meals.sumOf { it.carbsGrams }
                val totalF = meals.sumOf { it.fatGrams }
                val currentGoals = _state.value.goals

                val computedProgress = DailyGoalProgress.calculate(
                    dateKey = key,
                    consumedCalories = totalCal,
                    consumedProtein = totalP,
                    consumedCarbs = totalC,
                    consumedFat = totalF,
                    goals = currentGoals
                )

                _state.update { it.copy(progress = computedProgress) }
            }
        }
    }

    /**
     * Update goals directly with customized limits
     */
    fun setLimits(
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        preset: GoalPreset = GoalPreset.CUSTOM
    ) {
        val safeCal = calories.coerceIn(800, 6000)
        val safeP = proteinG.coerceIn(20, 400)
        val safeC = carbsG.coerceIn(20, 800)
        val safeF = fatG.coerceIn(15, 300)

        val newGoals = UserGoals(
            dailyCalories = safeCal,
            dailyProteinG = safeP,
            dailyCarbsG = safeC,
            dailyFatG = safeF
        )

        goalsRepo.saveGoals(newGoals)

        _state.update { current ->
            val updatedProgress = DailyGoalProgress.calculate(
                dateKey = current.selectedDateKey,
                consumedCalories = current.progress.calories.current.toInt(),
                consumedProtein = current.progress.protein.current,
                consumedCarbs = current.progress.carbs.current,
                consumedFat = current.progress.fat.current,
                goals = newGoals
            )
            current.copy(
                goals = newGoals,
                activePreset = preset,
                progress = updatedProgress,
                feedbackMessage = "Goals updated (${newGoals.dailyCalories} kcal)"
            )
        }
    }

    /**
     * Apply standard macro percentage split preset to a chosen calorie limit
     */
    fun applyPreset(preset: GoalPreset, calorieLimit: Int = _state.value.goals.dailyCalories) {
        if (preset == GoalPreset.CUSTOM) {
            _state.update { it.copy(activePreset = GoalPreset.CUSTOM) }
            return
        }

        val safeCal = calorieLimit.coerceIn(800, 6000)
        // Protein: 4 kcal/g, Carbs: 4 kcal/g, Fat: 9 kcal/g
        val proteinCalories = (safeCal * (preset.proteinPercent / 100.0))
        val carbsCalories = (safeCal * (preset.carbsPercent / 100.0))
        val fatCalories = (safeCal * (preset.fatPercent / 100.0))

        val proteinG = (proteinCalories / 4.0).toInt().coerceAtLeast(20)
        val carbsG = (carbsCalories / 4.0).toInt().coerceAtLeast(20)
        val fatG = (fatCalories / 9.0).toInt().coerceAtLeast(15)

        setLimits(
            calories = safeCal,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG,
            preset = preset
        )
    }

    fun clearFeedback() {
        _state.update { it.copy(feedbackMessage = null) }
    }
}
