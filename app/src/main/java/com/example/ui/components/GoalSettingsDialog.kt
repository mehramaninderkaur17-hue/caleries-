package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GoalPreset
import com.example.data.UserGoals
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun GoalSettingsDialog(
    currentGoals: UserGoals,
    onDismiss: () -> Unit,
    onSaveGoals: (calories: Int, protein: Int, carbs: Int, fat: Int) -> Unit
) {
    var calories by remember { mutableIntStateOf(currentGoals.dailyCalories) }
    var protein by remember { mutableIntStateOf(currentGoals.dailyProteinG) }
    var carbs by remember { mutableIntStateOf(currentGoals.dailyCarbsG) }
    var fat by remember { mutableIntStateOf(currentGoals.dailyFatG) }
    var selectedPreset by remember { mutableStateOf<GoalPreset?>(null) }

    fun applyPreset(preset: GoalPreset) {
        selectedPreset = preset
        val safeCal = calories.coerceIn(800, 6000)
        val pCals = safeCal * (preset.proteinPercent / 100.0)
        val cCals = safeCal * (preset.carbsPercent / 100.0)
        val fCals = safeCal * (preset.fatPercent / 100.0)

        protein = (pCals / 4.0).toInt().coerceAtLeast(20)
        carbs = (cCals / 4.0).toInt().coerceAtLeast(20)
        fat = (fCals / 9.0).toInt().coerceAtLeast(15)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("goals_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Nutrition Limits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = if (calories > 0) calories.toString() else "",
                    onValueChange = {
                        calories = it.toIntOrNull() ?: 0
                        selectedPreset?.let { p -> applyPreset(p) }
                    },
                    label = { Text("Daily Calorie Limit (kcal)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_calories"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Macro Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(GoalPreset.BALANCED, GoalPreset.HIGH_PROTEIN, GoalPreset.LOW_CARB).forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { applyPreset(preset) },
                            label = { Text(preset.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = if (protein > 0) protein.toString() else "",
                    onValueChange = {
                        protein = it.toIntOrNull() ?: 0
                        selectedPreset = GoalPreset.CUSTOM
                    },
                    label = { Text("Protein Limit (g)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_protein"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = if (carbs > 0) carbs.toString() else "",
                    onValueChange = {
                        carbs = it.toIntOrNull() ?: 0
                        selectedPreset = GoalPreset.CUSTOM
                    },
                    label = { Text("Carbohydrates Limit (g)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_carbs"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = if (fat > 0) fat.toString() else "",
                    onValueChange = {
                        fat = it.toIntOrNull() ?: 0
                        selectedPreset = GoalPreset.CUSTOM
                    },
                    label = { Text("Fat Limit (g)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_goal_fat"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val macroCalories = (protein * 4) + (carbs * 4) + (fat * 9)
                Text(
                    text = "Macro sum: ~$macroCalories kcal ($protein g P, $carbs g C, $fat g F)",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldDark,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (calories > 0) {
                                onSaveGoals(calories, protein, carbs, fat)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_goals_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Limits")
                    }
                }
            }
        }
    }
}
