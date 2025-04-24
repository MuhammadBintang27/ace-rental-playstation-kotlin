package com.ace.playstation.ui.home

import PlayStation
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ace.playstation.data.repository.PlayStationRepository
import com.ace.playstation.model.TransactionItem
import com.ace.playstation.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val playStationRepository = PlayStationRepository()
    private val transactionRepository = TransactionRepository()

    // LiveData for UI state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // LiveData for PlayStation units
    private val _allPlayStationUnits = MutableLiveData<List<PlayStation>>()
    private val _playStationUnits = MutableLiveData<List<PlayStation>>()
    val playStationUnits: LiveData<List<PlayStation>> = _playStationUnits

    // LiveData for recent transactions
    private val _recentTransactions = MutableLiveData<List<TransactionItem>>()
    val recentTransactions: LiveData<List<TransactionItem>> = _recentTransactions

    // LiveData for revenue information
    private val _todayRevenue = MutableLiveData<Double>()
    val todayRevenue: LiveData<Double> = _todayRevenue

    private val _weeklyRevenue = MutableLiveData<Double>()
    val weeklyRevenue: LiveData<Double> = _weeklyRevenue

    private val _monthlyRevenue = MutableLiveData<Double>()
    val monthlyRevenue: LiveData<Double> = _monthlyRevenue

    // Current filter for PlayStation units
    private var currentPlayStationFilter = "ALL"

    /**
     * Load all dashboard data
     */
    suspend fun loadDashboardData(context: Context) {
        _isLoading.value = true

        try {
            // Load PlayStation units
            loadPlayStationUnits()

            // Load transactions
            loadTransactions()

            // Load revenue data
            calculateRevenue()

        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error loading dashboard data: ${e.message}")
            _errorMessage.value = "Failed to load dashboard data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Load all PlayStation units
     */
    private suspend fun loadPlayStationUnits() {
        try {
            val units = playStationRepository.getAllPlaystations()
            _allPlayStationUnits.postValue(units)
            filterPlayStationUnits(currentPlayStationFilter)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error loading PlayStation units: ${e.message}")
            _errorMessage.postValue("Failed to load PlayStation units")
        }
    }

    /**
     * Load recent transactions
     */
    private suspend fun loadTransactions() {
        try {
            val result = transactionRepository.getAllTransactions()
            if (result.isSuccess) {
                // Sort by date descending and take only the 5 most recent transactions
                val sortedTransactions = result.getOrNull()?.sortedByDescending {
                    parseTransactionDate(it.datetime)
                }?.take(5) ?: emptyList()

                _recentTransactions.postValue(sortedTransactions)
            } else {
                _errorMessage.postValue("Failed to load transactions")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error loading transactions: ${e.message}")
            _errorMessage.postValue("Failed to load transactions")
        }
    }

    /**
     * Calculate revenue for different time periods
     */
    private suspend fun calculateRevenue() {
        try {
            val result = transactionRepository.getAllTransactions()
            if (result.isSuccess) {
                val allTransactions = result.getOrNull() ?: emptyList()

                // Calculate today's revenue
                val todayTransactions = transactionRepository.filterByToday(allTransactions)
                val todayTotal = todayTransactions.sumOf { it.amount }
                _todayRevenue.postValue(todayTotal)

                // Calculate weekly revenue
                val weeklyTransactions = transactionRepository.filterByWeek(allTransactions)
                val weeklyTotal = weeklyTransactions.sumOf { it.amount }
                _weeklyRevenue.postValue(weeklyTotal)

                // Calculate monthly revenue
                val monthlyTransactions = transactionRepository.filterByMonth(allTransactions)
                val monthlyTotal = monthlyTransactions.sumOf { it.amount }
                _monthlyRevenue.postValue(monthlyTotal)
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating revenue: ${e.message}")
            _errorMessage.postValue("Failed to calculate revenue")
        }
    }

    /**
     * Filter PlayStation units based on status
     */
    fun filterPlayStationUnits(filter: String) {
        currentPlayStationFilter = filter
        val allUnits = _allPlayStationUnits.value ?: emptyList()

        val filteredUnits = when (filter) {
            "Available" -> allUnits.filter { it.status == "Available" }
            "Rented" -> allUnits.filter { it.status == "Rented" }
            else -> allUnits
        }

// Sort PlayStation units by their numeric value (PS-1, PS-2, ..., PS-10)
        val sortedUnits = filteredUnits.sortedBy {
            // Extract the number part after "PS-" and convert to Int for proper numerical sorting
            val numberPart = it.nomorUnit.substringAfter("PS-").toIntOrNull() ?: Int.MAX_VALUE
            numberPart
        }

        _playStationUnits.value = sortedUnits    }

    /**
     * Handle PlayStation unit click
     */
    fun onPlayStationUnitClicked(playStation: PlayStation) {
        // Handle navigation to PlayStation detail or rental screen
        // This would typically use a navigation component or interface
        Log.d("HomeViewModel", "PlayStation unit clicked: ${playStation.nomorUnit}")
        // Implementation depends on navigation approach (e.g., NavController, interface callback)
    }

    /**
     * Parse transaction date string to Date object for sorting
     */
    private fun parseTransactionDate(dateString: String): Date {
        return try {
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error parsing date: $dateString", e)
            Date() // Return current date as fallback
        }
    }
}