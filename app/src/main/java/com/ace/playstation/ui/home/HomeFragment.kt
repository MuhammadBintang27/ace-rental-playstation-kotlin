package com.ace.playstation.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ace.playstation.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Setup Pie Chart
        val pieChart: PieChart = binding.pieChart
        homeViewModel.pendapatanKategori.observe(viewLifecycleOwner) { data ->
            setupPieChart(pieChart, data)
        }

        // Update Status Unit PlayStation
        homeViewModel.statusUnit.observe(viewLifecycleOwner) {
            binding.statusUnit.text = it
        }

        // Update Informasi Shift
        homeViewModel.informasiShift.observe(viewLifecycleOwner) {
            binding.informasiShift.text = it
        }

        return root
    }

    private fun setupPieChart(pieChart: PieChart, data: Map<String, Float>) {
        val entries = data.map { PieEntry(it.value, it.key) }
        val dataSet = PieDataSet(entries, "Kategori Pendapatan").apply {
            colors = listOf(Color.parseColor("#FF5733"), Color.parseColor("#33FF57"), Color.parseColor("#3357FF"))
            valueTextSize = 14f
        }

        pieChart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}