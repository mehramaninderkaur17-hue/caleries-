package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DailyGoalProgress
import com.example.data.GoalPreset
import com.example.data.GoalProgressItem
import com.example.data.GoalStatus
import com.example.data.GoalsRepository
import com.example.data.MealEntry
import com.example.data.MealType
import com.example.data.UserGoals
import com.example.viewmodel.GoalTrackingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NutriSnap AI", appName)
  }

  @Test
  fun `verify meal entry model creation`() {
    val meal = MealEntry(
      dateKey = "2026-10-20",
      mealType = MealType.LUNCH.name,
      title = "Avocado Toast",
      calories = 350,
      proteinGrams = 12.0,
      carbsGrams = 28.0,
      fatGrams = 14.0
    )
    assertEquals("Avocado Toast", meal.title)
    assertEquals(350, meal.calories)
    assertEquals(12.0, meal.proteinGrams, 0.01)
  }

  @Test
  fun `verify goals repository persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = GoalsRepository(context)
    val customGoals = UserGoals(dailyCalories = 2200, dailyProteinG = 150, dailyCarbsG = 200, dailyFatG = 70)
    repo.saveGoals(customGoals)
    val loaded = repo.getGoals()
    assertEquals(2200, loaded.dailyCalories)
    assertEquals(150, loaded.dailyProteinG)
  }

  @Test
  fun `verify goal progress item limits and status calculation`() {
    val underItem = GoalProgressItem("Calories", 500.0, 2000.0, "kcal")
    assertEquals(GoalStatus.UNDER_BUDGET, underItem.status)
    assertEquals(1500.0, underItem.remaining, 0.01)
    assertEquals(0.0, underItem.overLimit, 0.01)

    val nearLimitItem = GoalProgressItem("Calories", 1850.0, 2000.0, "kcal")
    assertEquals(GoalStatus.NEAR_LIMIT, nearLimitItem.status)
    assertEquals(150.0, nearLimitItem.remaining, 0.01)

    val exceededItem = GoalProgressItem("Calories", 2250.0, 2000.0, "kcal")
    assertEquals(GoalStatus.EXCEEDED_LIMIT, exceededItem.status)
    assertEquals(0.0, exceededItem.remaining, 0.01)
    assertEquals(250.0, exceededItem.overLimit, 0.01)
  }

  @Test
  fun `verify daily goal progress aggregate calculation`() {
    val goals = UserGoals(dailyCalories = 2000, dailyProteinG = 130, dailyCarbsG = 220, dailyFatG = 65)
    val progress = DailyGoalProgress.calculate(
      dateKey = "2026-10-20",
      consumedCalories = 1450,
      consumedProtein = 110.0,
      consumedCarbs = 160.0,
      consumedFat = 42.0,
      goals = goals
    )

    assertEquals(1450.0, progress.calories.current, 0.01)
    assertEquals(550.0, progress.calories.remaining, 0.01)
    assertFalse(progress.isAnyLimitExceeded)
    assertTrue(progress.adherenceScore >= 80)
  }

  @Test
  fun `verify goal tracking viewmodel preset macro calculation`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = GoalTrackingViewModel(app)

    // Apply high protein preset (40% Protein, 35% Carbs, 25% Fat) to 2000 kcal
    vm.applyPreset(GoalPreset.HIGH_PROTEIN, 2000)
    val state = vm.state.value

    assertEquals(2000, state.goals.dailyCalories)
    // 2000 * 0.40 = 800 kcal / 4 = 200g protein
    assertEquals(200, state.goals.dailyProteinG)
    // 2000 * 0.35 = 700 kcal / 4 = 175g carbs
    assertEquals(175, state.goals.dailyCarbsG)
    // 2000 * 0.25 = 500 kcal / 9 = 55g fat
    assertEquals(55, state.goals.dailyFatG)
    assertEquals(GoalPreset.HIGH_PROTEIN, state.activePreset)
  }
}
