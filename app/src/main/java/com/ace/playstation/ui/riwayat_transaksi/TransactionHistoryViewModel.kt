package com.ace.playstation.ui.riwayat_transaksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.playstation.model.TransactionItem
import kotlinx.coroutines.launch

class TransactionHistoryViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val transactions: LiveData<List<TransactionItem>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allTransactions = listOf<TransactionItem>()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getAllTransactions()
                .onSuccess { transactions ->
                    allTransactions = transactions
                    _transactions.value = transactions
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Terjadi kesalahan saat memuat data"
                }

            _isLoading.value = false
        }
    }

    fun filterTransactionsByToday() {
        viewModelScope.launch {
            _transactions.value = repository.filterByToday(allTransactions)
        }
    }

    fun filterTransactionsByWeek() {
        viewModelScope.launch {
            _transactions.value = repository.filterByWeek(allTransactions)
        }
    }

    fun filterTransactionsByMonth() {
        viewModelScope.launch {
            _transactions.value = repository.filterByMonth(allTransactions)
        }
    }

    fun filterTransactionsByYear() {
        viewModelScope.launch {
            _transactions.value = repository.filterByYear(allTransactions)
        }
    }

    fun filterTransactionsByDateRange(fromDate: String, toDate: String) {
        viewModelScope.launch {
            _transactions.value = repository.filterByDateRange(allTransactions, fromDate, toDate)
        }
    }

    fun resetFilter() {
        viewModelScope.launch {
            _transactions.value = allTransactions
        }
    }

    fun filterTransactions(timeFilter: String, fromDate: String = "", toDate: String = "", categoryFilter: String = TransactionRepository.CATEGORY_ALL) {
        viewModelScope.launch {
            val allTransactions = allTransactions
            val filtered = repository.filterTransactions(allTransactions, timeFilter, fromDate, toDate, categoryFilter)
            _transactions.value = filtered
        }
    }
}