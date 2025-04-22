package com.ace.playstation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.databinding.ItemTransactionBinding
import com.ace.playstation.model.TransactionItem
import java.text.NumberFormat
import java.util.Locale

class TransactionHistoryAdapter :
    ListAdapter<TransactionItem, TransactionHistoryAdapter.TransactionViewHolder>(
        TransactionDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionItem) {
            // Format untuk currency Indonesia
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            formatter.maximumFractionDigits = 0

            // Set type transaksi (rental atau penjualan) dan kategori
            val transactionTypeText = when(transaction.type) {
                "RENTAL" -> "Sewa Rental PS (${transaction.category})"
                "PENJUALAN" -> "Penjualan (${transaction.category})"
                else -> transaction.type
            }
            binding.tvTransactionType.text = transactionTypeText

            // Set tanggal dan waktu transaksi
            binding.tvTransactionDate.text = transaction.datetime

            // Set nama item
            binding.tvItemName.text = transaction.name

            // Set detail transaksi
            binding.tvDetails.text = transaction.details

            // Set jumlah/harga dengan format currency
            binding.tvAmount.text = formatter.format(transaction.amount)

            // Set background based on transaction type and category
            val backgroundResId = when {
                transaction.type == "RENTAL" -> R.drawable.card_bg_rental
                transaction.type == "PENJUALAN" && transaction.category == "Makanan" -> R.drawable.card_bg_food
                transaction.type == "PENJUALAN" && transaction.category == "Minuman" -> R.drawable.card_bg_drink
                else -> R.drawable.card_bg // Default background
            }

            // Apply the background to the container
            binding.root.findViewById<ViewGroup>(R.id.cardContainer).setBackgroundResource(backgroundResId)
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionItem>() {
        override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem == newItem
        }
    }
}