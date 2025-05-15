package com.ace.playstation.model.admin

import androidx.lifecycle.*
import com.ace.playstation.repository.admin.TransactionRepository
import kotlinx.coroutines.launch

class AdminOverviewViewModel : ViewModel() {
    private val repo = TransactionRepository()

    private val _summaries = MutableLiveData<List<AdminSummaryItem>>()
    val summaries: LiveData<List<AdminSummaryItem>> = _summaries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Track current filters so we can reuse them
    private var currentCategory = TransactionRepository.CATEGORY_ALL

    fun loadSummaries(from: String, to: String, category: String) {
        currentCategory = category
        viewModelScope.launch {
            _isLoading.value = true
            repo.getAdminSummaries(from, to, category)
                .onSuccess { _summaries.value = it }
                .onFailure { /* TODO: surface error in a LiveData<String> */ }
            _isLoading.value = false
        }
    }

    /**
     * Convenience method for when the user only changes the date range
     */
    fun filterTransactionsByDateRange(fromDate: String, toDate: String) {
        // Re-use the last‐selected category
        loadSummaries(fromDate, toDate, currentCategory)
    }
}

