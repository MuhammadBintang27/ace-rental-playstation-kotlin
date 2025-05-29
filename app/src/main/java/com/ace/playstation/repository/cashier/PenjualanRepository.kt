package com.ace.playstation.repository

import android.content.Context
import android.util.Log
import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.model.Produk
import com.ace.playstation.model.TransaksiPenjualan
import io.github.jan.supabase.postgrest.from

class PenjualanRepository(private val context: Context) {

    // Fungsi untuk mendapatkan semua produk dari database
    suspend fun getAllProduk(): List<Produk> {
        return try {
            val response = SupabaseClientInstance.getClient().from("produk").select().decodeList<Produk>()
            response
        } catch (e: Exception) {
            Log.e("PenjualanRepository", "Error fetching products: ${e.message}")
            emptyList()
        }
    }

    // Fungsi untuk mendapatkan produk berdasarkan kategori
    suspend fun getProdukByKategori(kategori: String): List<Produk> {
        return try {
            val response = SupabaseClientInstance.getClient().from("produk")
                .select { filter { eq("kategori", kategori) } }
                .decodeList<Produk>()
            response
        } catch (e: Exception) {
            Log.e("PenjualanRepository", "Error fetching products by category: ${e.message}")
            emptyList()
        }
    }

    // Fungsi untuk menyimpan transaksi penjualan
    suspend fun saveTransaksiPenjualan(transaksi: TransaksiPenjualan): Boolean {
        return try {
            SupabaseClientInstance.getClient().from("transaksi_penjualan").insert(transaksi)
            true
        } catch (e: Exception) {
            Log.e("PenjualanRepository", "Error saving transaction: ${e.message}")
            false
        }
    }

    // Fungsi untuk memperbarui stok produk
    suspend fun updateStokProduk(produkId: Int, jumlah: Int): Boolean {
        return try {
            // Fetch the current product
            val produk = SupabaseClientInstance.getClient().from("produk")
                .select { filter { eq("produk_id", produkId) } }
                .decodeSingle<Produk>()

            // Calculate new stock
            val newStock = produk.stok_persediaan - jumlah

            // Ensure stock doesn't go negative
            if (newStock < 0) {
                Log.e("PenjualanRepository", "Insufficient stock for produk_id: $produkId")
                return false
            }

            // Update the stock
            SupabaseClientInstance.getClient().from("produk")
                .update(mapOf("stok_persediaan" to newStock)) {
                    filter { eq("produk_id", produkId) }
                }
            true
        } catch (e: Exception) {
            Log.e("PenjualanRepository", "Error updating stock: ${e.message}")
            false
        }
    }
}