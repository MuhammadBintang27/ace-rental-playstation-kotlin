package com.ace.playstation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.databinding.ItemPenjualanBinding
import com.ace.playstation.model.PenjualanItem

class PenjualanAdapter(
    private val onQuantityChanged: (position: Int, jumlah: Int) -> Unit
) : ListAdapter<PenjualanItem, PenjualanAdapter.PenjualanViewHolder>(PenjualanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenjualanViewHolder {
        val binding = ItemPenjualanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PenjualanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PenjualanViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PenjualanViewHolder(
        private val binding: ItemPenjualanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PenjualanItem, position: Int) {
            binding.textViewNamaProduk.text = item.produk.nama_produk
            binding.textViewHarga.text = "Rp ${formatPrice(item.produk.harga)}"
            binding.textViewStok.text = "Stok: ${item.produk.stok_persediaan}"
            binding.editTextJumlah.text = if (item.jumlah > 0) item.jumlah.toString() else "0"

            // Set the appropriate image based on category
            when (item.produk.kategori) {
                "Makanan" -> binding.imageViewProduk.setImageResource(R.drawable.ic_logo_fnb)
                "Minuman" -> binding.imageViewProduk.setImageResource(R.drawable.ic_logo_beverage)
            }

            // Setup quantity change listener
            binding.buttonMinus.setOnClickListener {
                val currentQuantity = item.jumlah
                if (currentQuantity > 0) {
                    val newQuantity = currentQuantity - 1
                    binding.editTextJumlah.text = if (newQuantity > 0) newQuantity.toString() else "0"
                    onQuantityChanged(position, newQuantity)
                }
            }

            binding.buttonPlus.setOnClickListener {
                val currentQuantity = item.jumlah
                if (currentQuantity < item.produk.stok_persediaan) {
                    val newQuantity = currentQuantity + 1
                    binding.editTextJumlah.text = newQuantity.toString()
                    onQuantityChanged(position, newQuantity)
                } else {
                    // Notify user if stock is insufficient
                    binding.root.context.getString(R.string.stock_insufficient).also {
                        android.widget.Toast.makeText(binding.root.context, it, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun formatPrice(price: Double): String {
            return String.format("%,.0f", price)
        }
    }

    class PenjualanDiffCallback : DiffUtil.ItemCallback<PenjualanItem>() {
        override fun areItemsTheSame(oldItem: PenjualanItem, newItem: PenjualanItem): Boolean {
            return oldItem.produk.produk_id == newItem.produk.produk_id
        }

        override fun areContentsTheSame(oldItem: PenjualanItem, newItem: PenjualanItem): Boolean {
            return oldItem == newItem
        }
    }
}