# PocketLedger — Personal Finance Tracker

[![Android CI](https://github.com/krit-vardhan-mishra/PocketLedger/actions/workflows/android.yml/badge.svg)](https://github.com/krit-vardhan-mishra/PocketLedger/actions/workflows/android.yml)

**PocketLedger** is a high-performance, offline-first personal finance application built natively for Android. It combines modern architecture with a privacy-centric design to help users track their spending without compromising their data.

## 🚀 Key Features

- **Privacy-First Storage:** All data stays on your device in a secure Room database. No cloud, no tracking.
- **Modern Jetpack Compose UI:** A fluid, interactive interface inspired by Material 3 design principles.
- **Intelligent Budgeting:** Set monthly limits per category and get real-time visual warnings (Red chips) when you're over-budget.
- **Proactive Notifications:** Receive daily summaries at your preferred time using Android's WorkManager API.
- **Rich Analytics:** Beautiful Donut and Bar charts to visualize spending categories and daily trends.
- **CSV Export:** Share your transaction records for external auditing or backups.

## 🛠️ Technical Deep Dive

### **Architecture & State Management**
PocketLedger follows the **MVVM (Model-View-ViewModel)** architecture pattern.
- **UI Layer:** Built with **Jetpack Compose**, observing state from ViewModels.
- **Domain Layer:** Uses **UseCases** to encapsulate business logic (e.g., calculating monthly summaries).
- **Data Layer:** A single source of truth using **Room Database**.

### **Tech Stack**
- **Language:** Kotlin (100%)
- **UI:** Jetpack Compose (Declarative UI)
- **Dependency Injection:** Hilt (Dagger)
- **Asynchronous:** Kotlin Coroutines & Flow
- **Local Storage:** Room Persistence Library
- **Background Tasks:** WorkManager
- **Charts:** MPAndroidChart
- **CI/CD:** GitHub Actions (automated testing on every push)

## 🧪 Testing
Quality is baked into the code:
- **Unit Testing:** ViewModel and Repository layers tested using JUnit4 and MockK.
- **Database Testing:** In-memory Room tests to ensure data integrity.
- **Continuous Integration:** Every commit is verified by our GitHub Actions pipeline.

## 📁 Repository Structure
```text
app/src/main/java/com/kritvm/pocketledger/
├── data/       # Persistence (Room), Models, and Repositories
├── domain/     # Logic specific to business rules (UseCases)
├── ui/         # All UI components, screens, and ViewModels
├── worker/     # Android WorkManager background tasks
└── di/         # Dependency Injection modules for Hilt
```

## 📦 Getting Started
1. Clone the repository.
2. Open in **Android Studio Hedgehog** or later.
3. Sync Gradle and run on a physical device or emulator (API 26+ recommended).

---
**Author:** Kirti Vardhan Mishra  
**License:** MIT
