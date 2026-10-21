package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyGoalProgress
import com.example.data.GoalsRepository
import com.example.data.MealEntry
import com.example.data.MealType
import com.example.data.NutriDatabase
import com.example.data.UserGoals
import com.example.network.GeminiService
import com.example.network.MealAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface AnalysisState {
    object Idle : AnalysisState
    data class Analyzing(val step: String) : AnalysisState
    data class Review(
        val result: MealAnalysisResult,
        val capturedBitmap: Bitmap?,
        val initialMealType: MealType
    ) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

data class NutriUiState(
    val selectedDate: Calendar = Calendar.getInstance(),
    val dateKey: String = "",
    val dateDisplayLabel: String = "",
    val meals: List<MealEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val goals: UserGoals = UserGoals(),
    val goalProgress: DailyGoalProgress = DailyGoalProgress(),
    val analysisState: AnalysisState = AnalysisState.Idle,
    val showManualEntryDialog: Boolean = false,
    val showGoalsDialog: Boolean = false,
    val editingMeal: MealEntry? = null,
    val snackbarMessage: String? = null
)

class NutriViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NutriDatabase.getDatabase(application)
    private val mealDao = database.mealDao()
    private val goalsRepo = GoalsRepository(application)

    private val dateFormatKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateDisplayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    private val _uiState = MutableStateFlow(
        run {
            val initialGoals = goalsRepo.getGoals()
            val initialDateKey = dateFormatKey.format(Date())
            NutriUiState(
                dateKey = initialDateKey,
                dateDisplayLabel = formatDisplayDate(Calendar.getInstance()),
                goals = initialGoals,
                goalProgress = DailyGoalProgress.calculate(
                    dateKey = initialDateKey,
                    consumedCalories = 0,
                    consumedProtein = 0.0,
                    consumedCarbs = 0.0,
                    consumedFat = 0.0,
                    goals = initialGoals
                )
            )
        }
    )
    val uiState: StateFlow<NutriUiState> = _uiState.asStateFlow()

    init {
        observeMealsForSelectedDate()
        seedSampleDataIfEmpty()
    }

    private fun formatDisplayDate(cal: Calendar): String {
        val today = Calendar.getInstance()
        val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        if (isToday) return "Today, ${SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)}"

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) return "Yesterday, ${SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)}"

        return dateDisplayFormat.format(cal.time)
    }

    private fun observeMealsForSelectedDate() {
        viewModelScope.launch {
            val key = _uiState.value.dateKey
            mealDao.getMealsForDate(key).collectLatest { mealList ->
                val totalCal = mealList.sumOf { it.calories }
                val totalP = mealList.sumOf { it.proteinGrams }
                val totalC = mealList.sumOf { it.carbsGrams }
                val totalF = mealList.sumOf { it.fatGrams }

                _uiState.update { current ->
                    val computedProgress = DailyGoalProgress.calculate(
                        dateKey = key,
                        consumedCalories = totalCal,
                        consumedProtein = totalP,
                        consumedCarbs = totalC,
                        consumedFat = totalF,
                        goals = current.goals
                    )
                    current.copy(
                        meals = mealList,
                        totalCalories = totalCal,
                        totalProtein = totalP,
                        totalCarbs = totalC,
                        totalFat = totalF,
                        goalProgress = computedProgress
                    )
                }
            }
        }
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            if (mealDao.getCount() == 0) {
                val todayKey = dateFormatKey.format(Date())
                val morningCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 30)
                }
                val lunchCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 13)
                    set(Calendar.MINUTE, 15)
                }

                mealDao.insertMeal(
                    MealEntry(
                        dateKey = todayKey,
                        timestamp = morningCal.timeInMillis,
                        mealType = MealType.BREAKFAST.name,
                        title = "Avocado Toast & Boiled Eggs",
                        calories = 420,
                        proteinGrams = 22.0,
                        carbsGrams = 34.0,
                        fatGrams = 18.0,
                        portionDescription = "2 slices sourdough + 2 eggs",
                        itemsSummary = "Sourdough Bread, Ripe Avocado, Organic Eggs",
                        advice = "Great protein and micronutrient balance to kickstart morning energy."
                    )
                )

                mealDao.insertMeal(
                    MealEntry(
                        dateKey = todayKey,
                        timestamp = lunchCal.timeInMillis,
                        mealType = MealType.LUNCH.name,
                        title = "Mediterranean Salmon Bowl",
                        calories = 580,
                        proteinGrams = 42.0,
                        carbsGrams = 48.0,
                        fatGrams = 20.0,
                        portionDescription = "1 standard bowl (~380g)",
                        itemsSummary = "Wild Salmon, Quinoa, Cucumber, Feta, Olive Oil",
                        advice = "Rich in Omega-3 fatty acids and slow-digesting complex carbs."
                    )
                )
            }
        }
    }

    fun navigateDate(deltaDays: Int) {
        val newCal = (_uiState.value.selectedDate.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, deltaDays)
        }
        val newKey = dateFormatKey.format(newCal.time)
        val newLabel = formatDisplayDate(newCal)

        _uiState.update {
            it.copy(
                selectedDate = newCal,
                dateKey = newKey,
                dateDisplayLabel = newLabel
            )
        }
        observeMealsForSelectedDate()
    }

    fun goToToday() {
        val today = Calendar.getInstance()
        val key = dateFormatKey.format(today.time)
        _uiState.update {
            it.copy(
                selectedDate = today,
                dateKey = key,
                dateDisplayLabel = formatDisplayDate(today)
            )
        }
        observeMealsForSelectedDate()
    }

    /**
     * Start image analysis from captured Bitmap or picked Uri
     */
    fun analyzeImage(bitmap: Bitmap?, uri: Uri?, textHint: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(analysisState = AnalysisState.Analyzing("Reading food image...")) }

            val resolvedBitmap = withContext(Dispatchers.IO) {
                bitmap ?: uri?.let { decodeBitmapFromUri(it) }
            }

            _uiState.update { it.copy(analysisState = AnalysisState.Analyzing("Detecting dishes & estimating macros...")) }

            val result = GeminiService.analyzeFood(resolvedBitmap, textHint)
            result.onSuccess { analysis ->
                val suggestedType = determineMealTypeByTime()
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisState.Review(
                            result = analysis,
                            capturedBitmap = resolvedBitmap,
                            initialMealType = suggestedType
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisState.Error(err.message ?: "Failed to recognize food image")
                    )
                }
            }
        }
    }

    /**
     * Text description analysis
     */
    fun analyzeTextOnly(description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(analysisState = AnalysisState.Analyzing("Calculating nutritional breakdown...")) }
            val result = GeminiService.analyzeFood(null, description)
            result.onSuccess { analysis ->
                val suggestedType = determineMealTypeByTime()
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisState.Review(
                            result = analysis,
                            capturedBitmap = null,
                            initialMealType = suggestedType
                        ),
                        showManualEntryDialog = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisState.Error(err.message ?: "Failed to analyze meal")
                    )
                }
            }
        }
    }

    private fun determineMealTypeByTime(): MealType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..10 -> MealType.BREAKFAST
            hour in 11..15 -> MealType.LUNCH
            hour in 16..17 -> MealType.SNACK
            hour in 18..22 -> MealType.DINNER
            else -> MealType.SNACK
        }
    }

    fun dismissAnalysis() {
        _uiState.update { it.copy(analysisState = AnalysisState.Idle) }
    }

    fun logAnalyzedMeal(
        title: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        portion: String,
        mealType: MealType,
        itemsSummary: String,
        advice: String,
        bitmap: Bitmap?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageUriString = bitmap?.let { saveBitmapLocally(it) }

            val entry = MealEntry(
                dateKey = _uiState.value.dateKey,
                mealType = mealType.name,
                title = title.ifBlank { "Logged Meal" },
                calories = calories.coerceAtLeast(0),
                proteinGrams = protein.coerceAtLeast(0.0),
                carbsGrams = carbs.coerceAtLeast(0.0),
                fatGrams = fat.coerceAtLeast(0.0),
                portionDescription = portion,
                itemsSummary = itemsSummary,
                advice = advice,
                imageUri = imageUriString
            )

            mealDao.insertMeal(entry)

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        analysisState = AnalysisState.Idle,
                        snackbarMessage = "Logged \"$title\" (${calories} kcal)"
                    )
                }
            }
        }
    }

    fun deleteMeal(meal: MealEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            mealDao.deleteMeal(meal)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(snackbarMessage = "Deleted ${meal.title}") }
            }
        }
    }

    fun startEditingMeal(meal: MealEntry) {
        _uiState.update { it.copy(editingMeal = meal) }
    }

    fun dismissEditMeal() {
        _uiState.update { it.copy(editingMeal = null) }
    }

    fun saveEditedMeal(meal: MealEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            mealDao.updateMeal(meal)
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        editingMeal = null,
                        snackbarMessage = "Updated ${meal.title}"
                    )
                }
            }
        }
    }

    fun openManualEntry() {
        _uiState.update { it.copy(showManualEntryDialog = true) }
    }

    fun closeManualEntry() {
        _uiState.update { it.copy(showManualEntryDialog = false) }
    }

    fun openGoalsDialog() {
        _uiState.update { it.copy(showGoalsDialog = true) }
    }

    fun closeGoalsDialog() {
        _uiState.update { it.copy(showGoalsDialog = false) }
    }

    fun updateGoals(calories: Int, protein: Int, carbs: Int, fat: Int) {
        val newGoals = UserGoals(
            dailyCalories = calories.coerceAtLeast(500),
            dailyProteinG = protein.coerceAtLeast(10),
            dailyCarbsG = carbs.coerceAtLeast(10),
            dailyFatG = fat.coerceAtLeast(10)
        )
        goalsRepo.saveGoals(newGoals)
        _uiState.update { current ->
            val updatedProgress = DailyGoalProgress.calculate(
                dateKey = current.dateKey,
                consumedCalories = current.totalCalories,
                consumedProtein = current.totalProtein,
                consumedCarbs = current.totalCarbs,
                consumedFat = current.totalFat,
                goals = newGoals
            )
            current.copy(
                goals = newGoals,
                goalProgress = updatedProgress,
                showGoalsDialog = false,
                snackbarMessage = "Daily limits updated!"
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val context = getApplication<Application>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapLocally(bitmap: Bitmap): String? {
        return try {
            val context = getApplication<Application>()
            val filename = "meal_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
