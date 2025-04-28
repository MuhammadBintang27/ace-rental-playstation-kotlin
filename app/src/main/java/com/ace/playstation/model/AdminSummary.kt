package com.ace.playstation.model

data class AdminSummaryItem(
    val name: String,                    // produk.nama_produk or "PlayStation Unit #X"
    val category: String,                // same as TransactionItem.type
    val totalCountOrMinutes: Int,        // sum of jumlah for PENJUALAN or sum of durasi for RENTAL
    val totalIncome: Double              // sum of total_harga or harga
)
