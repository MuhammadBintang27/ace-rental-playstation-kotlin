package com.ace.playstation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.playstation.model.FinancialEntry
import com.ace.playstation.repository.FinancialRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class BalanceViewModel : ViewModel() {
    private val repo = FinancialRepository()

    private val _totalBalance   = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    private val _monthlyIncome  = MutableLiveData<Double>()
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    private val _monthlyOutcome = MutableLiveData<Double>()
    val monthlyOutcome: LiveData<Double> = _monthlyOutcome

    private val _trend          = MutableLiveData<List<FinancialEntry>>()
    val trend: LiveData<List<FinancialEntry>> = _trend

    init {
        refreshAll()
    }

    /** Refresh balance, income & outcome for last 30 days, and trend(30). */
    fun refreshAll() {
        loadTotalBalance()
        loadMonthlySummary()
        loadTrend(30)
    }

    /** Expose manual refresh (e.g. for pull-to-refresh) */
    fun refreshData() = refreshAll()

    fun loadTrend(days: Int) = viewModelScope.launch {
        val now = Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        // Subtract 'days' days
        val startDate = now.minus(days.toLong(), DateTimeUnit.DAY).toString()
        val endDate   = now.toString()

        _trend.value = repo.getFinancialTrendData(startDate, endDate)
    }

    fun loadTotalBalance() = viewModelScope.launch {
        _totalBalance.value = repo.getCurrentBalance()
    }

    fun loadMonthlySummary() = viewModelScope.launch {
        // Get current LocalDate in system time zone
        val now = Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        // Calculate date from 30 days ago
        val start30Days = now.minus(30, DateTimeUnit.DAY).toString()
        val today = now.toString()

        _monthlyIncome.value = repo.getIncomeInRange(start30Days, today)
        _monthlyOutcome.value = repo.getOutcomeInRange(start30Days, today)
    }
}