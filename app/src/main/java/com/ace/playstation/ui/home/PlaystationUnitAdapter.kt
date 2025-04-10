package com.ace.playstation.ui.home

import PlayStation
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.databinding.ItemPlaystationUnitBinding

class PlayStationUnitAdapter(private val onItemClick: (PlayStation) -> Unit) :
    ListAdapter<PlayStation, PlayStationUnitAdapter.ViewHolder>(PlayStationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaystationUnitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playStation = getItem(position)
        holder.bind(playStation)
    }

    inner class ViewHolder(private val binding: ItemPlaystationUnitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(playStation: PlayStation) {
            binding.tvUnitNumber.text = "Unit #${playStation.nomorUnit}"

            // Set status with color
            when (playStation.status) {
                "Available" -> {
                    binding.tvStatus.text = "Available"
                    binding.statusIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.available_green)
                    )
                }
                "Rented" -> {
                    binding.tvStatus.text = "Rented"
                    binding.statusIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.in_use_orange)
                    )
                }
                else -> {
                    binding.tvStatus.text = playStation.status
                    binding.statusIndicator.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.gray)
                    )
                }
            }

            // Set game type (if any)
            binding.tvGameType.text = if (playStation.tipeMain.isNotEmpty()) playStation.tipeMain else "Not set"
        }
    }

    class PlayStationDiffCallback : DiffUtil.ItemCallback<PlayStation>() {
        override fun areItemsTheSame(oldItem: PlayStation, newItem: PlayStation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlayStation, newItem: PlayStation): Boolean {
            return oldItem == newItem
        }
    }
}