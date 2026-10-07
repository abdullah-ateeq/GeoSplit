# GeoSplit 📐

**GeoSplit** is a modern, 100% offline Android utility app designed for engineers, architects, carpenters, fabricators, students, and DIY enthusiasts. It enables users to divide common geometric shapes into equal sections and automatically calculates section areas, percentages, precise cut dimensions, and visual blueprint diagrams.

---

## 🌟 Key Features

### 📐 Geometric Shape Splitting
* **Rectangle**: Split across length or width into equal sections with exact section length and width calculations.
* **Square**: 
  * **Equal Strips**: Cut across length or width.
  * **Custom Grid**: Custom grid matrix up to 20×20 rows and columns with custom presets.
* **Circle**: Divide circles into equal sectors with exact central angle calculations and sector dimensions.
* **Triangle**: Calculate equal-area section boundaries parallel to the base using exact height ratio formulas ($y_k = h \cdot \sqrt{k/n}$).

### 🔢 Comprehensive Calculation Breakdown
* **Primary Results**: Section area, section percentage of total area, perimeter/circumference.
* **Section Breakdown**: Exact length, width, sector angle, and triangle height boundaries.
* **Step-by-Step Formulas**: Full formula visibility and step-by-step mathematical breakdown for learning and verification.
* **Technical Blueprint Diagrams**: Interactive visual diagram showing cut lines and dimensions.

### 📏 Unit System & Conversions
* Supports **Millimeters (`mm`)**, **Centimeters (`cm`)**, **Meters (`m`)**, **Inches (`in`)**, and **Feet (`ft`)**.
* Internal precision powered by `BigDecimal` math contexts.

### 💾 History & Settings
* **Calculation History**: Stores up to 50 calculation records locally.
* **Pinned Favorites**: Pin frequently used calculations for quick access.
* **Share & Clipboard**: Instant copy formatted calculation summaries or share via Android system share sheet.
* **Theme Options**: Dark Mode, Light Mode, and System Default with Android Dynamic Color support.

### 🔒 100% Offline & Private
* Zero network permissions.
* No tracking, analytics, or ads.
* All data remains strictly on device via Android DataStore.

---

## 🛠 Tech Stack & Architecture

* **Language**: Kotlin 100%
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose)
* **Architecture**: Clean MVVM (Model-ViewModel-UiState) pattern
* **Data Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
* **Asynchrony**: Kotlin Coroutines & `StateFlow`
* **Precision**: `java.math.BigDecimal` with customizable rounding/decimal places

---

## 📱 Screenshots & Previews

The application includes technical canvas blueprint renderings for each geometric split:
* **Rectangle / Square Strips & Grids**
* **Circular Sectors & Central Angles**
* **Triangular Equal-Area Boundaries**

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** (Ladybug or newer recommended)
* **JDK 17**
* **Android SDK**: Compile SDK 37 / Min SDK 24 (Android 7.0+)

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/GeoSplit.git
   cd GeoSplit
   ```

2. **Build debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run unit tests**:
   ```bash
   ./gradlew test
   ```

---

## ⚠️ Disclaimer

GeoSplit performs basic geometric and mathematical calculations. Always verify measurements, material tolerances, and project requirements before performing actual construction, fabrication, or engineering cuts.

---

## 📄 License

Created by **Abdullah Ateeq**. Licensed under the [MIT License](LICENSE).
