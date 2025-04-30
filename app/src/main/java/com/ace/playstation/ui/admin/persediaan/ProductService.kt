package com.ace.playstation.ui.admin.persediaan

import com.ace.playstation.auth.SupabaseClientInstance
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductService {
    private val client = SupabaseClientInstance.getClient()

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            return@withContext client
                .from("produk")
                .select {
                    order("kategori", Order.ASCENDING)
                    order("nama_produk", Order.ASCENDING)
                }
                .decodeList<Product>()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Product>()
        }
    }

    suspend fun getProductsByCategory(category: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            return@withContext client
                .from("produk")
                .select {
                    filter {
                        eq("kategori", category)
                    }
                }
                .decodeList<Product>()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Product>()
        }
    }


    suspend fun getLowStockProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            return@withContext client
                .from("produk")
                .select {
                    filter {
                        lt("stok_persediaan", 10)
                    }
                    order("stok_persediaan", Order.ASCENDING)
                }
                .decodeList<Product>()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Product>()
        }
    }
}