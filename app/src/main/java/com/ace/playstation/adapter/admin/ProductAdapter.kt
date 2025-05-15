package com.ace.playstation.adapter.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.model.admin.Product

class ProductAdapter(private val context: Context) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var productList: List<Product> = ArrayList()

    fun setProducts(products: List<Product>) {
        this.productList = products
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.imageViewProduk)
        private val tvNamaProduk: TextView = itemView.findViewById(R.id.tv_nama_produk)
        private val tvKategori: TextView = itemView.findViewById(R.id.tv_kategori)
        private val tvHarga: TextView = itemView.findViewById(R.id.tv_harga)
        private val tvStok: TextView = itemView.findViewById(R.id.tv_stok)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        fun bind(product: Product) {
            tvNamaProduk.text = product.nama_produk
            tvKategori.text = product.kategori
            tvHarga.text = "Rp ${String.format("%,.0f", product.harga)}"
            tvStok.text = product.stok_persediaan.toString()

            // Set the background color of kategori tag
            if (product.kategori.equals("Makanan", ignoreCase = true)) {
                tvKategori.setBackgroundResource(R.drawable.card_bg_food)
                tvKategori.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                tvKategori.setBackgroundResource(R.drawable.card_bg_drink)
                tvKategori.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }

            // Check stock status
            when {
                product.stok_persediaan <= 0 -> {
                    tvStatus.text = "Habis"
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.warning_red))
                }
                product.isStokMenipis -> {
                    tvStatus.text = "Menipis"
                    tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                }
                else -> {
                    tvStatus.text = "Tersedia"
                    tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                }
            }

            when (product.kategori) {
                "Makanan" -> ivIcon.setImageResource(R.drawable.ic_logo_fnb)
                "Minuman" -> ivIcon.setImageResource(R.drawable.ic_logo_beverage)
                else       -> ivIcon.setImageResource(R.drawable.ic_logo_console)
            }
        }
    }

}