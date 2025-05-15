package com.ace.playstation.ui.transaksi_penjualan.TransaksiPenjualanViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ace.playstation.model.PenjualanItem
import com.ace.playstation.model.Produk
import com.ace.playstation.model.TransaksiPenjualan
import com.ace.playstation.repository.PenjualanRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PenjualanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PenjualanRepository(application)

    private val _produkList = MutableLiveData<List<Produk>>()
    val produkList: LiveData<List<Produk>> = _produkList

    private val _cartItems = mutableMapOf<Int, PenjualanItem>()
    private val _penjualanItems = MutableLiveData<List<PenjualanItem>>()
    val penjualanItems: LiveData<List<PenjualanItem>> = _penjualanItems

    private val _totalPenjualan = MutableLiveData<Double>(0.0)
    val totalPenjualan: LiveData<Double> = _totalPenjualan

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var _selectedCategory = MutableLiveData<String>("semua")
    val selectedCategory: LiveData<String> = _selectedCategory

    init {
        loadProduk()
    }

    fun loadProduk(kategori: String = "semua") {
        viewModelScope.launch {
            try {
                val produkList = if (kategori == "semua") {
                    repository.getAllProduk()
                } else {
                    repository.getProdukByKategori(kategori)
                }
                _produkList.value = produkList
                _selectedCategory.value = kategori
                updateDisplayedItems(produkList, kategori)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load products: ${e.message}"
            }
        }
    }

    private fun updateDisplayedItems(produkList: List<Produk>, kategori: String) {
        val displayItems = produkList.map { produk ->
            _cartItems[produk.produk_id] ?: PenjualanItem(produk)
        }
        _penjualanItems.value = displayItems
    }

    fun updateJumlahItem(position: Int, jumlah: Int) {
        val currentItems = _penjualanItems.value?.toMutableList() ?: mutableListOf()
        if (position >= 0 && position < currentItems.size) {
            val item = currentItems[position]
            if (jumlah >= 0 && jumlah <= item.produk.stok_persediaan) {
                item.jumlah = jumlah
                item.totalHarga = jumlah * item.produk.harga
                currentItems[position] = item

                if (jumlah > 0) {
                    _cartItems[item.produk.produk_id] = item
                } else {
                    _cartItems.remove(item.produk.produk_id)
                }

                _penjualanItems.value = currentItems
                calculateTotal()
            } else if (jumlah > item.produk.stok_persediaan) {
                _errorMessage.value = "Jumlah melebihi stok yang tersedia untuk ${item.produk.nama_produk}"
            }
        }
    }

    private fun calculateTotal() {
        val total = _cartItems.values.sumOf { it.totalHarga }
        _totalPenjualan.value = total
    }

    fun saveTransaksi() {
        viewModelScope.launch {
            try {
                val itemsToSave = _cartItems.values.toList()

                if (itemsToSave.isEmpty()) {
                    _errorMessage.value = "Tidak ada item yang dipilih"
                    return@launch
                }

                // Check stock availability
                for (item in itemsToSave) {
                    if (item.jumlah > item.produk.stok_persediaan) {
                        _errorMessage.value = "Stok tidak mencukupi untuk ${item.produk.nama_produk}"
                        return@launch
                    }
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentTime = dateFormat.format(Date())

                var allSuccess = true

                for (item in itemsToSave) {
                    val transaksi = TransaksiPenjualan(
                        produk_id = item.produk.produk_id,
                        jumlah = item.jumlah,
                        total_harga = item.totalHarga,
                        created_at = currentTime
                    )

                    val saveSuccess = repository.saveTransaksiPenjualan(transaksi)

                    if (saveSuccess) {
                        // Update stock
                        val stockUpdateSuccess = repository.updateStokProduk(item.produk.produk_id, item.jumlah)
                        if (!stockUpdateSuccess) {
                            allSuccess = false
                            _errorMessage.value = "Gagal memperbarui stok untuk ${item.produk.nama_produk}"
                        }
                    } else {
                        allSuccess = false
                    }
                }

                _saveStatus.value = allSuccess

                if (allSuccess) {
                    _cartItems.clear()
                    loadProduk(_selectedCategory.value ?: "semua")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save transaction: ${e.message}"
                _saveStatus.value = false
            }
        }
    }

    fun getAllCartItems(): List<PenjualanItem> {
        return _cartItems.values.toList()
    }
}