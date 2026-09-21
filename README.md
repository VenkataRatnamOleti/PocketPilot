# PocketPilot ✈️💳

A phone-first AI financial assistant MVP built natively for Android using Kotlin and Jetpack Compose. PocketPilot tracks expenses with an offline-first Room database and dynamic reactive UI components.

---

## 🚀 Features

- **Monthly plan:** Set income and upcoming commitments; the safe-to-spend figure updates instantly.
- **Expense tracking:** Add manual expenses or capture a receipt and confirm its merchant, category, and total.
- **Voice questions:** Use the microphone to ask affordability, spending, and food-spend questions.
- **What-if analysis:** Ask “Can I afford ₹1,800?” or “What if I buy this for ₹5,000?” for a transparent budget impact and recommendation.
- **Offline-first:** Expenses and plan settings persist locally; no account, API key, or network connection is required.

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
```

## Run it

1. Open `C:\Users\venka\StudioProjects\PocketPilot` in Android Studio.
2. Select an Android emulator or a physical device running Android 7.0+.
3. Press **Run**. Grant camera permission when using receipt capture; speech recognition is provided by the device's installed speech service.
4. On Home, set the monthly plan, add an expense, then try the Scan and Ask AI tabs.

## Demo script

1. Update the monthly plan to ₹15,000 income and ₹2,000 upcoming commitments.
2. Scan a receipt, review its fields, and save it as Food.
3. Ask: “Can I afford ₹1,800?” and show the remaining buffer and impact.
4. Ask: “How much did I spend on food?” to show category-aware guidance.
