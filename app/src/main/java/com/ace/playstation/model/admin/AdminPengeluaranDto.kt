package com.ace.playstation.model.admin

import kotlinx.serialization.Serializable

@Serializable
data class PengeluaranDto(
    val nama_pengeluaran: String?,
    val kategori: String?,
    val tanggal: String?,
    val jumlah_pengeluaran: Double?,
    val stok_tambahan: Int?,
    val bukti: String?
)

data class Pengeluaran(
    val nama: String,
    val kategori: String,
    val tanggal: String,
    val jumlah: Double,
    val stok_tambahan: Int,
    val bukti: String?
)
