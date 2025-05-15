package com.ace.playstation.repository.admin

import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.model.admin.Pengeluaran
import com.ace.playstation.model.admin.PengeluaranDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminPengeluaranRepository {
    private val client = SupabaseClientInstance.getClient()

    suspend fun fetchPengeluaran(
        category: String? = null,
        sortBy: String = "tanggal",
        descending: Boolean = true
    ): List<Pengeluaran> = withContext(Dispatchers.IO) {
        try {
            val query = client.from("pengeluaran")
                .select {
                    if (category != null) {
                        filter { eq("kategori", category) }
                    }
                    order(sortBy, if (descending) Order.DESCENDING else Order.ASCENDING)
                    limit(10)
                }
            query.decodeList<PengeluaranDto>().map {
                Pengeluaran(
                    nama = it.nama_pengeluaran ?: "-",
                    kategori = it.kategori ?: "Lainnya",
                    tanggal = it.tanggal ?: "-",
                    jumlah = it.jumlah_pengeluaran ?: 0.0,
                    stok_tambahan = it.stok_tambahan ?: 0,
                    bukti = it.bukti
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addPengeluaran(pengeluaran: PengeluaranDto): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            client.from("pengeluaran")
                .insert(pengeluaran)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}