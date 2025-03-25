package com.ace.playstation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _pendapatanKategori = MutableLiveData<Map<String, Float>>().apply {
        value = mapOf("Game 1" to 150000f, "Game 2" to 100000f, "Game 3" to 50000f)
    }
    val pendapatanKategori: LiveData<Map<String, Float>> = _pendapatanKategori

    private val _statusUnit = MutableLiveData<String>().apply {
        value = "5 Unit Tersedia, 2 Dalam Perbaikan"
    }
    val statusUnit: LiveData<String> = _statusUnit

    private val _informasiShift = MutableLiveData<String>().apply {
        value = "Shift 1 (09:00 - 15:00), Petugas: Andi"
    }
    val informasiShift: LiveData<String> = _informasiShift
}
