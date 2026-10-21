package com.example.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MealType
import com.example.ui.components.CalorieHeroCard
import com.example.ui.components.DateNavigationHeader
import com.example.ui.components.EditMealDialog
import com.example.ui.components.GoalSettingsDialog
import com.example.ui.components.MacrosBreakdownCard
import com.example.ui.components.ManualEntryDialog
import com.example.ui.components.MealCategorySection
import com.example.ui.components.ScanResultDialog
import com.example.ui.components.SnapOptionsDialog
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.viewmodel.AnalysisState
import com.example.viewmodel.NutriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriApp(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSnapOptionsDialog by remember { mutableStateOf(false) }
    var showCameraUnavailableDialog by remember { mutableStateOf(false) }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.analyzeImage(bitmap = bitmap, uri = null)
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: ActivityNotFoundException) {
                showCameraUnavailableDialog = true
            } catch (e: Exception) {
                showCameraUnavailableDialog = true
            }
        } else {
            showCameraUnavailableDialog = true
        }
    }

    fun launchCameraSafely() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: ActivityNotFoundException) {
                showCameraUnavailableDialog = true
            } catch (e: Exception) {
                showCameraUnavailableDialog = true
            }
        } else {
            try {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } catch (e: Exception) {
                showCameraUnavailableDialog = true
            }
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.analyzeImage(bitmap = null, uri = uri)
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NutriSnap AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Smart Calorie & Macro Vision",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldDark
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openGoalsDialog() },
                        modifier = Modifier.testTag("topbar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Targets",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom Action Dock
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Manual/Text Log Button
                    IconButton(
                        onClick = { viewModel.openManualEntry() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("open_manual_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Text / Manual Entry",
                            tint = EmeraldDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Camera Scan Primary Action (opens options dialog safely)
                    ExtendedFloatingActionButton(
                        onClick = { showSnapOptionsDialog = true },
                        modifier = Modifier.testTag("camera_scan_fab"),
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snap Meal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Gallery Photo Picker
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("open_gallery_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick from Gallery",
                            tint = EmeraldDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Selector Header
                item {
                    DateNavigationHeader(
                        dateDisplayLabel = uiState.dateDisplayLabel,
                        onPreviousDay = { viewModel.navigateDate(-1) },
                        onNextDay = { viewModel.navigateDate(1) },
                        onTodayClick = { viewModel.goToToday() },
                        onOpenGoals = { viewModel.openGoalsDialog() }
                    )
                }

                // Calorie Target & Remaining Hero Card
                item {
                    CalorieHeroCard(
                        calorieProgress = uiState.goalProgress.calories
                    )
                }

                // Macronutrients Breakdown Card (Protein, Carbs, Fat)
                item {
                    MacrosBreakdownCard(
                        goalProgress = uiState.goalProgress
                    )
                }

                // Meal Categories (Breakfast, Lunch, Dinner, Snack)
                item {
                    Text(
                        text = "Daily Meals Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                MealType.values().forEach { type ->
                    val mealsForType = uiState.meals.filter { it.mealType == type.name }
                    item(key = type.name) {
                        MealCategorySection(
                            mealType = type,
                            meals = mealsForType,
                            onAddClick = { viewModel.openManualEntry() },
                            onEditClick = { meal -> viewModel.startEditingMeal(meal) },
                            onDeleteClick = { meal -> viewModel.deleteMeal(meal) }
                        )
                    }
                }
            }

            // AI Analyzing Overlay
            AnimatedVisibility(
                visible = uiState.analysisState is AnalysisState.Analyzing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val stepText = (uiState.analysisState as? AnalysisState.Analyzing)?.step
                    ?: "Analyzing meal..."
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .testTag("analyzing_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 10.dp,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = EmeraldPrimary,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Gemini Vision AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Snap Meal Options Sheet (Camera, Gallery, Sample Photo, Text AI)
    if (showSnapOptionsDialog) {
        SnapOptionsDialog(
            onDismiss = { showSnapOptionsDialog = false },
            onLaunchCamera = {
                showSnapOptionsDialog = false
                launchCameraSafely()
            },
            onLaunchGallery = {
                showSnapOptionsDialog = false
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onSelectSample = { sampleBitmap, mealName ->
                showSnapOptionsDialog = false
                viewModel.analyzeImage(bitmap = sampleBitmap, uri = null, textHint = mealName)
            },
            onOpenManualText = {
                showSnapOptionsDialog = false
                viewModel.openManualEntry()
            }
        )
    }

    // Fallback Dialog: Camera is unavailable (e.g. running in browser/cloud emulator)
    if (showCameraUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showCameraUnavailableDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera Not Available", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "The device camera app could not be opened (normal on browser or desktop emulators without physical camera apps). You can still analyze food using your gallery, sample photos, or meal description!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraUnavailableDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Pick from Gallery")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCameraUnavailableDialog = false
                        val sampleBmp = SampleFoodHelper.createSampleMealBitmap("Avocado Toast")
                        viewModel.analyzeImage(sampleBmp, null, "Avocado Toast & Eggs")
                    }
                ) {
                    Text("Use Sample Meal")
                }
            }
        )
    }

    // Modal: AI Scan Result Review & Log
    val currentAnalysis = uiState.analysisState
    if (currentAnalysis is AnalysisState.Review) {
        ScanResultDialog(
            result = currentAnalysis.result,
            capturedBitmap = currentAnalysis.capturedBitmap,
            initialMealType = currentAnalysis.initialMealType,
            onDismiss = { viewModel.dismissAnalysis() },
            onConfirm = { title, calories, protein, carbs, fat, portion, mealType, itemsSummary, advice, bitmap ->
                viewModel.logAnalyzedMeal(
                    title = title,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    portion = portion,
                    mealType = mealType,
                    itemsSummary = itemsSummary,
                    advice = advice,
                    bitmap = bitmap
                )
            }
        )
    }

    // Modal: Manual Entry / Text AI Prompt
    if (uiState.showManualEntryDialog) {
        ManualEntryDialog(
            onDismiss = { viewModel.closeManualEntry() },
            onAnalyzeText = { text -> viewModel.analyzeTextOnly(text) },
            onDirectSave = { title, calories, protein, carbs, fat, portion, mealType ->
                viewModel.logAnalyzedMeal(
                    title = title,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    portion = portion,
                    mealType = mealType,
                    itemsSummary = title,
                    advice = "",
                    bitmap = null
                )
                viewModel.closeManualEntry()
            }
        )
    }

    // Modal: User Goals Settings
    if (uiState.showGoalsDialog) {
        GoalSettingsDialog(
            currentGoals = uiState.goals,
            onDismiss = { viewModel.closeGoalsDialog() },
            onSaveGoals = { cal, p, c, f ->
                viewModel.updateGoals(cal, p, c, f)
            }
        )
    }

    // Modal: Edit Existing Meal
    uiState.editingMeal?.let { meal ->
        EditMealDialog(
            meal = meal,
            onDismiss = { viewModel.dismissEditMeal() },
            onSave = { updated -> viewModel.saveEditedMeal(updated) }
        )
    }
}
