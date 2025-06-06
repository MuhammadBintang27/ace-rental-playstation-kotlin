package com.ace.playstation.repository.admin

import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.model.admin.Product
import com.ace.playstation.model.admin.ProductDto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminProductServiceRepository {
    private val client = SupabaseClientInstance.getClient()

    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            return@withContext client
                .from("produk")
                .select {
                    order("kategori", Order.ASCENDING)
                    order("nama_produk", Order.ASCENDING)
                }
                .decodeList<ProductDto>()
                .map { dto ->
                    Product(
                        nama_produk = dto.nama_produk,
                        harga = dto.harga,
                        stok_persediaan = dto.stok_tersedia,
                        kategori = dto.kategori
                    )
                }
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
                .decodeList<ProductDto>()
                .map { dto ->
                    Product(
                        nama_produk = dto.nama_produk,
                        harga = dto.harga,
                        stok_persediaan = dto.stok_tersedia,
                        kategori = dto.kategori
                    )
                }
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
                .decodeList<ProductDto>()
                .map { dto ->
                    Product(
                        nama_produk = dto.nama_produk,
                        harga = dto.harga,
                        stok_persediaan = dto.stok_tersedia,
                        kategori = dto.kategori
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList<Product>()
        }
    }

    suspend fun addProduct(product: Product): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // First check if product with this name exists
                val existingProducts = client
                    .from("produk")
                    .select {
                        filter {
                            eq("nama_produk", product.nama_produk)
                        }
                    }
                    .decodeList<ProductDto>()

                if (existingProducts.isNotEmpty()) {
                    // If product exists, update it
                    val existingProduct = existingProducts.first()
                    client.from("produk")
                        .update({
                            set("stok_persediaan", product.stok_persediaan)
                            set("harga", product.harga)
                        }) {
                            filter {
                                eq("nama_produk", product.nama_produk)
                            }
                        }
                } else {
                    // If product doesn't exist, create new one
                    val productDto = ProductDto(
                        produk_id = null, // ID will be generated by Supabase
                        nama_produk = product.nama_produk,
                        harga = product.harga,
                        kategori = product.kategori,
                        stok_tersedia = product.stok_persediaan
                    )

                    client.from("produk")
                        .insert(productDto)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
