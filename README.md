# PocketPilot ✈️💳

A phone-first AI financial assistant MVP built natively for Android using Kotlin and Jetpack Compose. PocketPilot tracks expenses with an offline-first Room database and dynamic reactive UI components.

---

## 🚀 Features

- **Expense Tracking:** View real-time logged expenses with categories, descriptions, and totals.
- **Offline-First Architecture:** Local data persistence powered by Room Database.
- **Reactive UI:** Modern Material 3 interface built with Jetpack Compose, updating instantly via Kotlin `StateFlow`.
- **Clean Architecture (MVVM):** Structured separation of concerns between UI, ViewModel, Repository, and Data sources.

---

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Database:** [Room Database](https://developer.android.com/training/data-storage/room)
- **Asynchronous Data:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow / StateFlow](https://kotlinlang.org/docs/flow.html)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 33 (Android 13.0)

---

## 📁 Project Structure

```text
com.pocketpilot.app/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room Database configuration
│   │   ├── ExpenseDao.kt        # Data Access Object for expenses
│   │   └── ExpenseEntity.kt     # Room entity defining expense schema
│   └── repository/
│       └── ExpenseRepository.kt # Single source of truth for expense data
├── ui/
│   └── screens/
│       └── HomeScreen.kt        # Main Jetpack Compose dashboard UI
├── viewmodel/
│   └── ExpenseViewModel.kt      # State management and UI logic
└── MainActivity.kt              # App entry point
