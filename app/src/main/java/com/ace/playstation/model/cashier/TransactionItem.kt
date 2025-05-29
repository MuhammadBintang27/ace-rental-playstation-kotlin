package com.ace.playstation.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionItem(
    val id: Int?,
    val type: String, // "RENTAL" atau "PENJUALAN"
    val category: String, // kategori_main untuk rental atau kategori untuk produk
    val name: String, // Untuk rental: "PlayStation Unit #X", untuk penjualan: nama_produk
    val details: String, // Untuk rental: waktu_mulai - waktu_selesai, durasi, untuk penjualan: jumlah
    var amount: Double, // harga untuk rental atau total_harga untuk penjualan
    val datetime: String, // waktu transaksi
)