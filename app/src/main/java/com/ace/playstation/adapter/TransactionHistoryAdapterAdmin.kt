package com.ace.playstation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.databinding.ItemTransactionAdminBinding
import com.ace.playstation.model.AdminSummaryItem
import java.text.NumberFormat
import java.util.Locale

class TransactionHistoryAdapterAdmin :
    ListAdapter<AdminSummaryItem, TransactionHistoryAdapterAdmin.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTransactionAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

    inner class VH(private val b: ItemTransactionAdminBinding) :
        RecyclerView.ViewHolder(b.root) {

        private val fmt = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        fun bind(item: AdminSummaryItem) {
            b.tvNamaItemAdmin.text = item.name
            b.tvKategoriItemAdmin.text = item.category // now shows "RENTAL" or "PENJUALAN"

            // Choose unit based on transaction type
            val isRental = item.category.equals("Tetap", true)
                    || item.category.equals("Personal", true)
            val unit = if (isRental) "minutes" else "pcs"
            val countLabel = "${item.totalCountOrMinutes} $unit"
            b.tvDetailsAdmin.text = countLabel

            b.tvAmountAdmin.text = fmt.format(item.totalIncome)
        }
    }

    class Diff : DiffUtil.ItemCallback<AdminSummaryItem>() {
        override fun areItemsTheSame(a: AdminSummaryItem, b: AdminSummaryItem) =
            a.name == b.name && a.category == b.category

        override fun areContentsTheSame(a: AdminSummaryItem, b: AdminSummaryItem) =
            a == b
    }
}
