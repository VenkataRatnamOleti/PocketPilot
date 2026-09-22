package com.pocketpilot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketpilot.app.ui.components.PocketPilotNavigationBar
import com.pocketpilot.app.ui.components.PocketPilotScreen
import com.pocketpilot.app.ui.screens.AddExpenseScreen
import com.pocketpilot.app.ui.screens.AskPocketPilotScreen
import com.pocketpilot.app.ui.screens.HomeScreen
import com.pocketpilot.app.ui.screens.CalendarScreen
import com.pocketpilot.app.ui.screens.ExpenseDetailScreen
import com.pocketpilot.app.ui.screens.PeopleScreen
import com.pocketpilot.app.ui.screens.ScanReceiptScreen
import com.pocketpilot.app.ui.theme.PocketPilotTheme
import com.pocketpilot.app.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {

            PocketPilotTheme {

                PocketPilotApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketPilotApp(
    expenseViewModel: ExpenseViewModel = viewModel()
) {

    // rememberSaveable keeps the current screen across rotation
    var currentScreen by rememberSaveable {
        mutableStateOf(PocketPilotScreen.HOME)
    }
    var selectedExpenseId by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler(
        enabled = currentScreen != PocketPilotScreen.HOME
    ) {

        currentScreen = PocketPilotScreen.HOME
        selectedExpenseId = null
    }

    Scaffold(

        bottomBar = {

            // Hide the bottom bar while typing a new expense
            if (currentScreen != PocketPilotScreen.ADD_EXPENSE) {

                PocketPilotNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { selected ->
                        currentScreen = selected
                    }
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier.padding(paddingValues)
        ) {

            when (currentScreen) {

                PocketPilotScreen.HOME -> {

                    HomeScreen(

                        availableToSpend =
                        expenseViewModel.availableToSpend,

                        totalExpenses =
                        expenseViewModel.totalExpenses,

                        totalProfit =
                        expenseViewModel.totalProfit,

                        upcomingExpenses =
                        expenseViewModel.upcomingExpenses,

                        expenses =
                        expenseViewModel.expenses,

                        onAddExpense = {
                            currentScreen =
                                PocketPilotScreen.ADD_EXPENSE
                        },

                        onAskPocketPilot = {
                            currentScreen =
                                PocketPilotScreen.ASK
                        },

                        monthlyIncome = expenseViewModel.monthlyIncome,

                        onBudgetUpdated = { income, upcoming ->
                            expenseViewModel.updateBudget(income, upcoming)
                        },
                        onDeleteExpense = expenseViewModel::deleteExpense
                        ,onExpenseSelected = { selectedExpenseId = it.id }
                    )
                }

                PocketPilotScreen.CALENDAR -> CalendarScreen(
                    expenses = expenseViewModel.expenses,
                    onExpenseSelected = { selectedExpenseId = it.id }
                )

                PocketPilotScreen.PEOPLE -> PeopleScreen(
                    loans = expenseViewModel.loans,
                    onAddLoan = expenseViewModel::addLoan,
                    onDeleteLoan = expenseViewModel::deleteLoan,
                    onSettleLoan = expenseViewModel::setLoanSettled
                )

                PocketPilotScreen.ADD_EXPENSE -> {

                    AddExpenseScreen(

                        onBack = {
                            currentScreen =
                                PocketPilotScreen.HOME
                        },

                        onExpenseAdded = { amount, category, description, isProfit ->

                            expenseViewModel.addExpense(
                                amount,
                                category,
                                description,
                                isProfit = isProfit
                            )
                        }
                    )
                }

                PocketPilotScreen.SCAN -> {

                    ScanReceiptScreen(
                        onBack = {
                            currentScreen =
                                PocketPilotScreen.HOME
                        },
                        onReceiptSaved = { amount, category, description, imagePath, merchant ->
                            expenseViewModel.addExpense(amount, category, description, imagePath, merchant)
                        }
                    )
                }

                PocketPilotScreen.ASK -> {

                    AskPocketPilotScreen(

                        availableToSpend =
                        expenseViewModel.availableToSpend,

                        upcomingExpenses = expenseViewModel.upcomingExpenses,

                        expenses = expenseViewModel.expenses,

                        onBack = {
                            currentScreen =
                                PocketPilotScreen.HOME
                        },
                        onAddExpense = { expense ->
                            expenseViewModel.addExpense(
                                expense.amount,
                                expense.category,
                                expense.description,
                                expense.receiptImagePath,
                                expense.merchant,
                                expense.isProfit
                            )
                        }
                    )
                }
            }

            selectedExpenseId?.let { id ->
                expenseViewModel.expenses.firstOrNull { it.id == id }?.let { expense ->
                    ExpenseDetailScreen(
                        expense = expense,
                        onBack = { selectedExpenseId = null },
                        onDelete = {
                            expenseViewModel.deleteExpense(expense.id)
                            selectedExpenseId = null
                        }
                    )
                }
            }
        }
    }
}
