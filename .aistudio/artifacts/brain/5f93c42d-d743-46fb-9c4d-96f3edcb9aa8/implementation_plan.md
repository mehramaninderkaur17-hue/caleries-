# NutriSnap AI - Calorie & Macro Counter

NutriSnap AI is an intelligent nutrition tracking and meal logging Android app that analyzes food photos via Gemini Vision AI to estimate calorie and macronutrient breakdowns, calculate portion estimates, track daily targets, and manage daily food logs.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification:
> - **Input Experience**: Dual photo input — camera snap with preview capture plus Android photo picker.
> - **Tracking Scope**: Daily calorie budget with comprehensive macronutrient breakdowns (Protein, Carbohydrates, Fat) and progress indicators.
> - **Visual Identity**: Fresh and vibrant Emerald Health theme with high-contrast nutrient badges, clean typography, and smooth transitions.

---

### 1. Overview & Core Concept

- **What It Does**: Users take a photo of a meal or select one from their gallery (or type a meal description). The Gemini multimodal model (`gemini-3.5-flash`) inspects the meal, identifies food items and estimated portions, calculates calories, protein, carbs, and fat, and returns structured nutritional data. Users can review, adjust portions or values, choose the meal category (Breakfast, Lunch, Dinner, Snack), and log it.
- **Target Audience / Persona**: Everyday fitness and health enthusiasts, mindful eaters, and calorie-trackers who want frictionless, instant visual meal tracking without manually searching hundreds of ingredients.
- **Key Value**: Replaces tedious manual entry with 3-second camera estimation, while keeping full manual control over logged values and daily goals.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Daily Dashboard**:
   - Header with current date, daily calorie progress ring (Consumed vs Budget vs Remaining), and macro progress bars (Protein, Carbs, Fat).
   - Meal categories cards: Breakfast, Lunch, Dinner, Snacks showing logged meals and quick-add action.
   - Quick action Floating Action Button (FAB) or bottom banner for "Snap Meal".
2. **AI Meal Scanner & Analyzer**:
   - Camera snap or gallery picker launcher.
   - Live loading state with playful animated AI scanning indicators ("Analyzing ingredients...", "Estimating portion sizes...", "Calculating macros...").
   - Result preview screen: Displays captured food photo, AI-detected meal name, estimated calories, macro badges, confidence rating, itemized ingredients breakdown, and editable fields for portion and values before logging.
3. **Manual / Text Search Fallback**:
   - Ability to describe a meal in natural text (e.g. "2 poached eggs on sourdough toast with half an avocado") for instant AI breakdown without a photo.
4. **Meal Details & Daily History Log**:
   - Chronological meal log with timestamps, photo thumbnails, item names, and macro chips.
   - Swipe-to-delete or tap-to-edit logged meals with real-time recalculation of daily totals.
5. **Nutrition Goals & Settings**:
   - Custom daily calorie target (default: 2,000 kcal) and macro ratio split (e.g., 30% Protein, 40% Carbs, 30% Fat or custom grams).

#### Visual Identity & Theme
- **Theme Direction**: Vibrant Emerald Health & Vitality.
- **Color Palette**:
  - `Primary`: Rich Emerald Green (`#0F9D58` / `#056A39`)
  - `Secondary / Accent`: Golden Honey Amber (`#F4B400`) & Coral Tangerine (`#FF7043`) for calories and macros
  - `Surface`: Clean crisp mint-tinted whites (`#F7FAF8`) and soft sage containers (`#E8F5E9`)
  - `Macro Tokens`: Protein (Ocean Cyan `#0288D1`), Carbs (Golden Wheat `#FFA000`), Fat (Warm Rose `#E91E63`)
- **Typography & Rhythm**:
  - Bold hero stats with large numbers for calorie progress.
  - Compact rounded pill chips for macro badges.
  - High-touch interactive buttons with ripple feedback and minimum 48dp touch targets.

---

### 3. Key Product Decisions & Trade-Offs

- **Model Choice: `gemini-3.5-flash` with REST API**:
  - *Chosen Approach*: Direct REST API call with `responseMimeType = "application/json"` and strict JSON schema for structured food nutritional estimates.
  - *Why*: Ultra-fast multimodal processing with high accuracy on food recognition and guaranteed structured JSON output for immediate parsing into UI models.
  - *Alternatives Considered*: Firebase AI (requires external Firebase console enablement not automated here); older models (`gemini-1.5-flash` is strictly forbidden).
- **Data Persistence: Local Room Database**:
  - *Chosen Approach*: Room Database with `MealEntryEntity` and `DailySummary` views, with Flow observation.
  - *Why*: Instant local offline access, zero cloud latency or sign-in walls, smooth 60fps local updates, and pre-configured KSP Room toolchain.
- **Zero-Permission Photo Picker & Camera Contract**:
  - *Chosen Approach*: Standard Android `ActivityResultContracts.PickVisualMedia` and `ActivityResultContracts.TakePicturePreview`.
  - *Why*: Zero unnecessary dangerous permissions required for photo selection, meeting modern Android privacy guidelines.

---

### 4. Technical Architecture & Data Strategy

```
┌───────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                      │
│  ┌────────────────────┐ ┌──────────────────┐ ┌──────────────┐ │
│  │  Dashboard Screen  │ │  Meal Scanner    │ │  Meal Log &  │ │
│  │  (Calories/Macros) │ │  (Camera/Picker) │ │  Edit Sheet  │ │
│  └─────────┬──────────┘ └────────┬─────────┘ └───────┬──────┘ │
└────────────┼─────────────────────┼───────────────────┼────────┘
             ▼                     ▼                   ▼
┌───────────────────────────────────────────────────────────────┐
│                    NutriSnap ViewModel                        │
│   - Daily Calorie & Macro Aggregations (StateFlow)            │
│   - Image Base64 Encoding & Gemini Analysis Dispatcher        │
│   - Goal Settings & Log Management Operations                 │
└────────────┬─────────────────────────────────┬────────────────┘
             │                                 │
             ▼                                 ▼
┌───────────────────────────┐     ┌─────────────────────────────┐
│  Gemini Vision REST API   │     │   Room Database Layer       │
│  - Multimodal prompt      │     │   - MealEntryDao            │
│  - Structured JSON schema │     │   - MealEntryEntity         │
│  - Calorie & macro parser │     │   - Flow<List<MealEntry>>   │
└───────────────────────────┘     └─────────────────────────────┘
```

#### Data Entities & Models
- **MealEntry**:
  - `id: Long`
  - `timestamp: Long` (epoch millis)
  - `dateKey: String` (YYYY-MM-DD for fast aggregation)
  - `mealType: String` (Breakfast, Lunch, Dinner, Snack)
  - `name: String` (e.g. "Grilled Chicken Quinoa Bowl")
  - `calories: Int`
  - `proteinGrams: Double`
  - `carbsGrams: Double`
  - `fatGrams: Double`
  - `portionDescription: String` (e.g., "1 bowl (~350g)")
  - `notes: String`
  - `imageUri: String?` (optional local thumbnail path)
- **NutritionalAnalysisResult**:
  - Structured output from Gemini parsing food items, individual calories, macros, and confidence.
- **UserGoals**:
  - `dailyCalorieTarget: Int` (e.g., 2000 kcal)
  - `targetProteinG: Int`
  - `targetCarbsG: Int`
  - `targetFatG: Int`
