package com.ace.playstation.adapter

import PlayStation
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.databinding.ItemPlaystationBinding

class PlayStationAdapter(
    private val playstationList: List<PlayStation>,
    private val onItemClick: (PlayStation) -> Unit
) : RecyclerView.Adapter<PlayStationAdapter.ViewHolder>() {

    class ViewHolder(
        private val binding: ItemPlaystationBinding,
        private val onItemClick: (PlayStation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playStation: PlayStation) {
            binding.tvPlaystationId.text = playStation.nomorUnit
            binding.tvStatus.text = playStation.status

            // Set click listener for the entire item
            itemView.setOnClickListener {
                onItemClick(playStation)
            }

            if (playStation.status == "Available") {
                binding.tvPlaystationType.visibility = View.GONE
                binding.tvPlaystationId.setBackgroundResource(R.drawable.bg_available)
            } else if (playStation.status == "Rented") {
                binding.tvPlaystationType.text = playStation.tipeMain
                binding.tvPlaystationType.visibility = View.VISIBLE
                binding.tvPlaystationId.setBackgroundResource(R.drawable.bg_rented)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaystationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playstationList[position])
    }

    override fun getItemCount() = playstationList.size
}