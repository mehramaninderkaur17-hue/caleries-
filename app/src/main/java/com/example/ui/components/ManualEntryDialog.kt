package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.MealType
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ManualEntryDialog(
    initialMealType: MealType = MealType.LUNCH,
    onDismiss: () -> Unit,
    onAnalyzeText: (String) -> Unit,
    onDirectSave: (
        title: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        portion: String,
        mealType: MealType
    ) -> Unit
) {
    var textDescription by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(initialMealType) }

    // Direct manual fields
    var manualName by remember { mutableStateOf("") }
    var manualCalories by remember { mutableIntStateOf(0) }
    var manualProtein by remember { mutableDoubleStateOf(0.0) }
    var manualCarbs by remember { mutableDoubleStateOf(0.0) }
    var manualFat by remember { mutableDoubleStateOf(0.0) }
    var manualPortion by remember { mutableStateOf("1 serving") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("manual_entry_dialog"),
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Meal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Category
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.values().forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Describe with AI
                Text(
                    text = "Option A: Describe with AI",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = textDescription,
                    onValueChange = { textDescription = it },
                    placeholder = { Text("e.g., 2 eggs, 1 slice toast with butter, black coffee") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ai_description"),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (textDescription.isNotBlank()) {
                            onAnalyzeText(textDescription)
                        }
                    },
                    enabled = textDescription.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_estimate_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Nutrition Estimate", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Direct Manual Entry
                Text(
                    text = "Option B: Direct Entry",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it },
                    label = { Text("Food / Meal Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_input_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (manualCalories > 0) manualCalories.toString() else "",
                        onValueChange = { manualCalories = it.toIntOrNull() ?: 0 },
                        label = { Text("Calories") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_input_calories"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = if (manualProtein > 0.0) manualProtein.toInt().toString() else "",
                        onValueChange = { manualProtein = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (manualCarbs > 0.0) manualCarbs.toInt().toString() else "",
                        onValueChange = { manualCarbs = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = if (manualFat > 0.0) manualFat.toInt().toString() else "",
                        onValueChange = { manualFat = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            if (manualName.isNotBlank() && manualCalories > 0) {
                                onDirectSave(
                                    manualName,
                                    manualCalories,
                                    manualProtein,
                                    manualCarbs,
                                    manualFat,
                                    manualPortion,
                                    selectedMealType
                                )
                            }
                        },
                        enabled = manualName.isNotBlank() && manualCalories > 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_manual_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Meal")
                    }
                }
            }
        }
    }
}
