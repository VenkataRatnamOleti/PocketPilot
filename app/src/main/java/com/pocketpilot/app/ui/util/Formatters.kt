package com.pocketpilot.app.ui.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/** Formats an amount as Indian rupees, e.g. 125000.0 -> "₹1,25,000". */
fun formatInr(amount: Double): String {

    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    val sign = if (amount < 0) "-" else ""

    return sign + "₹" + formatter.format(abs(amount))
}
