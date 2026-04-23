package io.github.louaynasr.globex.core.util

fun validateCurrencyInput(input: String, currentText: String): String {
    val normalizedInput = input.replace(',', '.')

    if (normalizedInput.count { it == '.' } > 1) {
        return currentText
    }

    val filtered = normalizedInput.filter { it.isDigit() || it == '.' }

    if (filtered.startsWith(".")) {
        return "0."
    }

    if (filtered.length > 1 && filtered.startsWith("0") && filtered[1] != '.') {
        return filtered.substring(1)
    }

    if (filtered.contains(".")) {
        val parts = filtered.split(".")
        if (parts.size > 1 && parts[1].length > 2) {
            return currentText
        }
    }
    if (filtered.length > 12) return currentText

    return filtered
}


// TODO: 4/2/2026 add a function to calculate the date based on now and date range: this will be used in two places: 1. to draw the graph 2. to show indicator on home screen for currency change