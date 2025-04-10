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

    // Maintain a master list of all cart items across categories
    private val _cartItems = mutableMapOf<Int, PenjualanItem>()

    // The filtered items to display based on current category
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

    // Inisialisasi dengan mengambil semua produk
    init {
        loadProduk()
    }

    // Fungsi untuk memuat produk berdasarkan kategori
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

                // Update the displayed items based on the selected category
                updateDisplayedItems(produkList, kategori)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load products: ${e.message}"
            }
        }
    }

    // Update the displayed items while preserving quantities
    private fun updateDisplayedItems(produkList: List<Produk>, kategori: String) {
        // Create displayed items from the product list, preserving quantities from cart
        val displayItems = produkList.map { produk ->
            // If this product is already in the cart, use that item with its quantity
            _cartItems[produk.produk_id] ?: PenjualanItem(produk)
        }

        // Update the displayed items
        _penjualanItems.value = displayItems
    }

    // Fungsi untuk memperbarui jumlah item yang dibeli
    fun updateJumlahItem(position: Int, jumlah: Int) {
        val currentItems = _penjualanItems.value?.toMutableList() ?: mutableListOf()
        if (position >= 0 && position < currentItems.size) {
            val item = currentItems[position]
            if (jumlah >= 0) {
                item.jumlah = jumlah
                item.totalHarga = jumlah * item.produk.harga
                currentItems[position] = item

                // Update the master cart with this item
                if (jumlah > 0) {
                    _cartItems[item.produk.produk_id] = item
                } else {
                    // Remove item from cart if quantity is 0
                    _cartItems.remove(item.produk.produk_id)
                }

                _penjualanItems.value = currentItems

                // Recalculate total
                calculateTotal()
            }
        }
    }

    // Fungsi untuk menghitung total penjualan
    private fun calculateTotal() {
        // Calculate total from the master cart items, not just displayed items
        val total = _cartItems.values.sumOf { it.totalHarga }
        _totalPenjualan.value = total
    }

    // Fungsi untuk menyimpan transaksi penjualan
    fun saveTransaksi() {
        viewModelScope.launch {
            try {
                // Use cart items instead of displayed items
                val itemsToSave = _cartItems.values.toList()

                if (itemsToSave.isEmpty()) {
                    _errorMessage.value = "Tidak ada item yang dipilih"
                    return@launch
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentTime = dateFormat.format(Date()) // Format waktu sekarang sebagai String

                var allSuccess = true

                for (item in itemsToSave) {
                    val transaksi = TransaksiPenjualan(
                        produk_id = item.produk.produk_id,
                        jumlah = item.jumlah,
                        total_harga = item.totalHarga,
                        created_at = currentTime // Simpan sebagai string
                    )

                    val saveSuccess = repository.saveTransaksiPenjualan(transaksi)

                    if (!saveSuccess) {
                        allSuccess = false
                    }
                }

                _saveStatus.value = allSuccess

                if (allSuccess) {
                    // Clear cart
                    _cartItems.clear()
                    // Reset displayed items
                    loadProduk(_selectedCategory.value ?: "semua")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save transaction: ${e.message}"
                _saveStatus.value = false
            }
        }
    }
    // Add this method to PenjualanViewModel
    fun getAllCartItems(): List<PenjualanItem> {
        return _cartItems.values.toList()
    }
}