package com.ace.playstation.ui.home

import PlayStation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ace.playstation.adapter.PlayStationUnitAdapter
import com.ace.playstation.adapter.RecentTransactionAdapter
import com.ace.playstation.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var playstationAdapter: PlayStationUnitAdapter
    private lateinit var recentTransactionAdapter: RecentTransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        loadData()
    }

    private fun setupUI() {
        // Setup PlayStation unit recycler view
        playstationAdapter = PlayStationUnitAdapter {
            // Handle PlayStation unit click
            homeViewModel.onPlayStationUnitClicked(it)
        }
        binding.rvPlaystationUnits.apply {
            adapter = playstationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Setup Recent Transactions recycler view
        recentTransactionAdapter = RecentTransactionAdapter()
        binding.rvRecentTransactions.apply {
            adapter = recentTransactionAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Setup tabs for filtering PlayStation units
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> homeViewModel.filterPlayStationUnits("ALL")
                    1 -> homeViewModel.filterPlayStationUnits("Available")
                    2 -> homeViewModel.filterPlayStationUnits("Rented")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Setup refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
    }

    private fun setupObservers() {
        // Observe PlayStation units
        homeViewModel.playStationUnits.observe(viewLifecycleOwner) { units ->
            playstationAdapter.submitList(units)
            updatePlayStationSummary(units)
        }

        // Observe recent transactions
        homeViewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            recentTransactionAdapter.submitList(transactions)
            binding.tvNoTransactions.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observe error messages
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                // Show error message (e.g., using Snackbar)
                // Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        }

        // Observe revenue data
        homeViewModel.todayRevenue.observe(viewLifecycleOwner) { revenue ->
            binding.tvTodayRevenueValue.text = formatCurrency(revenue)
        }

        homeViewModel.weeklyRevenue.observe(viewLifecycleOwner) { revenue ->
            binding.tvWeeklyRevenueValue.text = formatCurrency(revenue)
        }

        homeViewModel.monthlyRevenue.observe(viewLifecycleOwner) { revenue ->
            binding.tvMonthlyRevenueValue.text = formatCurrency(revenue)
        }
    }

    private fun updatePlayStationSummary(units: List<PlayStation>) {
        val totalUnits = units.size
        val availableUnits = units.count { it.status == "Available" }
        val inUseUnits = units.count { it.status == "Rented" }

        binding.tvTotalUnits.text = totalUnits.toString()
        binding.tvAvailableUnits.text = availableUnits.toString()
        binding.tvInUseUnits.text = inUseUnits.toString()

        // Set units status visibility
        binding.tvNoUnits.visibility = if (units.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        lifecycleScope.launch {
            homeViewModel.loadDashboardData(requireContext())
        }
    }

    private fun formatCurrency(amount: Double): String {
        return "Rp${String.format("%,.0f", amount)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}