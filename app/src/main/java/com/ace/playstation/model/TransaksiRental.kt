package com.ace.playstation.data.model // Tambahkan deklarasi package

import kotlinx.serialization.Serializable

@Serializable
data class TransaksiRental(
    val unit_id: Int, // Relasi ke PlayStation
    val shift_id: Int,
    val kategori_main: String,
    val waktu_mulai: String,
    val waktu_selesai: String,
    val durasi: Int, // Dalam menit atau jam, sesuai kebutuhan
    val harga: Double
)

