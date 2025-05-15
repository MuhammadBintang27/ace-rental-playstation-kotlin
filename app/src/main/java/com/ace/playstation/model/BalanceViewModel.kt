package com.ace.playstation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.playstation.model.FinancialEntry
import com.ace.playstation.repository.admin.FinancialRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class BalanceViewModel : ViewModel() {
    private val repo = FinancialRepository()

    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    private val _monthlyIncome = MutableLiveData<Double>()
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    private val _monthlyOutcome = MutableLiveData<Double>()
    val monthlyOutcome: LiveData<Double> = _monthlyOutcome

    private val _monthlyTotal = MutableLiveData<Double>()
    val monthlyTotal: LiveData<Double> = _monthlyTotal

    private val _trend = MutableLiveData<List<FinancialEntry>>()
    val trend: LiveData<List<FinancialEntry>> = _trend

    private val _selectedYear = MutableLiveData<Int>()
    val selectedYear: LiveData<Int> = _selectedYear

    private val _selectedMonth = MutableLiveData<Int>()
    val selectedMonth: LiveData<Int> = _selectedMonth

    init {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _selectedYear.value = now.year
        _selectedMonth.value = now.monthNumber
        refreshAll()
    }

    fun refreshAll() {
        loadTotalBalance()
        loadMonthlySummary()
        loadTrend(30)
    }

    fun setMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadMonthlySummary()
    }

    fun loadTrend(days: Int) = viewModelScope.launch {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startDate = now.minus(days.toLong(), DateTimeUnit.DAY).toString().let {
            val parts = it.split("-")
            "${parts[1]}/${parts[2]}/${parts[0]}" // Convert YYYY-MM-DD to MM/DD/YYYY
        }
        val endDate = now.toString().let {
            val parts = it.split("-")
            "${parts[1]}/${parts[2]}/${parts[0]}"
        }
        _trend.value = repo.getFinancialTrendData(startDate, endDate)
    }

    fun loadTotalBalance() = viewModelScope.launch {
        _totalBalance.value = repo.getCurrentBalance()
    }

    fun loadMonthlySummary() = viewModelScope.launch {
        val year = _selectedYear.value ?: return@launch
        val month = _selectedMonth.value ?: return@launch
        // Reset to 0 to avoid stale data
        _monthlyIncome.value = 0.0
        _monthlyOutcome.value = 0.0
        _monthlyIncome.value = repo.getMonthlyIncome(year, month)
        _monthlyOutcome.value = repo.getMonthlyOutcome(year, month)
        _monthlyTotal.value = (_monthlyIncome.value ?: 0.0) - (_monthlyOutcome.value ?: 0.0)
        android.util.Log.d("BalanceViewModel", "Month: $month, Year: $year, Income: ${_monthlyIncome.value}, Outcome: ${_monthlyOutcome.value}")
    }
}