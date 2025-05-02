package com.ace.playstation.model

data class FinancialEntry(
    val date: String,    // "YYYY-MM-DD"
    val income: Double,  // total income that day
    val outcome: Double  // total expense that day
)
