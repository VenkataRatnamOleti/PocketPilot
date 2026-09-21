package com.pocketpilot.app.domain

import com.pocketpilot.app.viewmodel.Expense
import com.pocketpilot.app.ui.util.formatInr
import kotlin.math.max

data class PurchaseAnalysis(
    val affordable: Boolean,
    val amount: Double,
    val remaining: Double,
    val impactPercent: Double,
    val message: String,
    val recommendation: String
)

object FinancialEngine {
    fun analyzePurchase(amount: Double, available: Double, upcoming: Double): PurchaseAnalysis {
        val remaining = available - amount
        val impact = if (available > 0) amount / available * 100 else 100.0
        val affordable = remaining >= 0
        val message = if (affordable) {
            "Yes — this fits your current discretionary budget. After this purchase, you would have ${formatInr(remaining)} left for flexible spending."
        } else {
            "This would put your flexible budget ${formatInr(-remaining)} below zero after your planned commitments."
        }
        val recommendation = when {
            !affordable -> "Try delaying it or set aside ${formatInr(max(0.0, -remaining))} first. Your upcoming commitments total ${formatInr(upcoming)}."
            impact > 50 -> "It is affordable, but it uses more than half of what is left. Consider a cheaper option or wait until your next income."
            impact > 25 -> "It is manageable, though it will noticeably reduce the rest-of-month buffer."
            else -> "This has a low impact on your remaining monthly buffer."
        }
        return PurchaseAnalysis(affordable, amount, remaining, impact, message, recommendation)
    }

    fun foodInsight(expenses: List<Expense>): String {
        val food = expenses.filter { it.category.equals("Food", true) }.sumOf { it.amount }
        return if (food == 0.0) "No food expenses have been logged yet." else "You have logged ${formatInr(food)} in Food. Recording every receipt will make this insight more accurate."
    }
}
