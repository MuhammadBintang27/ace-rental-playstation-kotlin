package com.ace.playstation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.databinding.ItemKonfirmasiBinding
import com.ace.playstation.model.PenjualanItem

class KonfirmasiAdapter : RecyclerView.Adapter<KonfirmasiAdapter.KonfirmasiViewHolder>() {

    private var items: List<PenjualanItem> = emptyList()

    fun submitList(newItems: List<PenjualanItem>) {
        items = newItems.filter { it.jumlah > 0 }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KonfirmasiViewHolder {
        val binding = ItemKonfirmasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KonfirmasiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KonfirmasiViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class KonfirmasiViewHolder(private val binding: ItemKonfirmasiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PenjualanItem) {
            binding.textViewNamaProduk.text = item.produk.nama_produk // Adjusted to use nama_produk
            binding.textViewJumlah.text = "${item.jumlah}x"
            binding.textViewHargaSatuan.text = "@ Rp ${formatPrice(item.produk.harga)}"
            binding.textViewTotalHarga.text = "Rp ${formatPrice(item.totalHarga)}"
        }

        private fun formatPrice(price: Double): String {
            return String.format("%,.0f", price)
        }
    }
}