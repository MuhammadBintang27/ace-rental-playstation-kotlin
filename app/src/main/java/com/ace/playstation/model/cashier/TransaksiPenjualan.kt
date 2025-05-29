package com.ace.playstation.model

import kotlinx.serialization.Serializable

// Model Entities
// Model untuk Produk
@Serializable
data class Produk(
    val produk_id: Int,
    val nama_produk: String,
    val harga: Double,
    val kategori: String, // "makanan" atau "minuman"
    val stok_persediaan: Int // Added stock field
)

// Model untuk Transaksi Penjualan
@Serializable
data class TransaksiPenjualan(
    val penjualan_id: Int? = null,
    val produk_id: Int,
    val jumlah: Int,
    val total_harga: Double,
    val created_at: String? = null
)

// Item penjualan untuk ditampilkan di RecyclerView
data class PenjualanItem(
    val produk: Produk,
    var jumlah: Int = 0,
    var totalHarga: Double = 0.0
)