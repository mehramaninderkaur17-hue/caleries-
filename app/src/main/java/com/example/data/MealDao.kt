package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    fun getMealsForDate(dateKey: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: Long): MealEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntry): Long

    @Update
    suspend fun updateMeal(meal: MealEntry)

    @Delete
    suspend fun deleteMeal(meal: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT COUNT(*) FROM meal_entries")
    suspend fun getCount(): Int
}
