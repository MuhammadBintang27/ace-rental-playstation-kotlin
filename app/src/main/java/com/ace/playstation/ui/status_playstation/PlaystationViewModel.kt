package com.ace.playstation.ui.status_playstation

import PlayStation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ace.playstation.data.repository.PlayStationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayStationViewModel : ViewModel() {
    private val repository = PlayStationRepository()
    private val _playstations = MutableStateFlow<List<PlayStation>>(emptyList())
    val playstations: StateFlow<List<PlayStation>> = _playstations

    fun fetchPlaystations() {
        viewModelScope.launch {
            _playstations.value = repository.getAllPlaystations()
        }
    }
}
