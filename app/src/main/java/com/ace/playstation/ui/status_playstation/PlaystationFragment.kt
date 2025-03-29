package com.ace.playstation.ui.status_playstation

import PlayStation
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ace.playstation.R
import com.ace.playstation.databinding.FragmentPlaystationBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayStationFragment : Fragment() {
    private var _binding: FragmentPlaystationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayStationViewModel by viewModels()
    private lateinit var adapter: PlayStationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaystationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewPlaystation.layoutManager = GridLayoutManager(requireContext(), 3)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playstations.collectLatest { playstations ->
                Log.d("Supabase", "Data masuk ke ViewModel: $playstations")
                adapter = PlayStationAdapter(playstations) { selectedPlayStation ->
                    // Navigate to detail fragment when a card is clicked
                    navigateToDetailFragment(selectedPlayStation)
                }
                binding.recyclerViewPlaystation.adapter = adapter
            }
        }

        viewModel.fetchPlaystations()
    }

    private fun navigateToDetailFragment(playStation: PlayStation) {
        // Create a bundle to pass data to the detail fragment
        val bundle = Bundle().apply {
            putInt("unit_id", playStation.id)
            putString("nomor_unit", playStation.nomorUnit)
            putString("status", playStation.status)
            putString("tipe_main", playStation.tipeMain)
            putString("waktu_mulai", playStation.waktuMulai)
            putString("waktu_selesai", playStation.waktuSelesai)
        }

        // Navigate to the detail fragment
        findNavController().navigate(
            R.id.action_nav_playstation_to_playstationDetailFragment,
            bundleOf(
                "unit_id" to playStation.id
            )
        )    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}