package com.ace.playstation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.databinding.ItemTransactionUnitBinding
import com.ace.playstation.model.TransactionItem

class RecentTransactionAdapter :
    ListAdapter<TransactionItem, RecentTransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionUnitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class ViewHolder(private val binding: ItemTransactionUnitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionItem) {
            binding.tvTransactionName.text = transaction.name
            binding.tvTransactionDetails.text = transaction.details
            binding.tvTransactionDate.text = transaction.datetime
            binding.tvTransactionAmount.text = formatCurrency(transaction.amount)

            // Set icon and background color based on transaction type
            when (transaction.type) {
                "RENTAL" -> {
                    binding.ivTransactionIcon.setImageResource(R.drawable.ic_logo_console)
                }
                "PENJUALAN" -> {
                    when (transaction.category) {
                        "Makanan" -> {

                            binding.ivTransactionIcon.setImageResource(R.drawable.ic_logo_fnb)
                        }
                        "Minuman" -> {

                            binding.ivTransactionIcon.setImageResource(R.drawable.ic_logo_beverage)
                        }
                        else -> {

                            binding.ivTransactionIcon.setImageResource(R.drawable.ic_logo_logout)
                        }
                    }
                }
                else -> {

                    binding.ivTransactionIcon.setImageResource(R.drawable.ic_logo_transaksi)
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return "Rp${String.format("%,.0f", amount)}"
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionItem>() {
        override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
            return oldItem == newItem
        }
    }
}