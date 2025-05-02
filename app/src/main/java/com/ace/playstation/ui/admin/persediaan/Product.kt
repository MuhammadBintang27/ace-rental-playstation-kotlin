package com.ace.playstation.ui.admin.persediaan

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val nama_produk: String,
    val harga: Double,
    val stok_persediaan: Int,
    val kategori: String,

    // Define a threshold for low stock warning
    val isStokMenipis: Boolean = stok_persediaan < 10
)