package com.ace.playstation.model.admin

import kotlinx.serialization.SerialName
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

@Serializable
data class ProductDto(
    val produk_id: Int? = null,
    val nama_produk: String,
    val harga: Double,
    val kategori: String,

    @SerialName("stok_persediaan")
    val stok_tersedia: Int
)