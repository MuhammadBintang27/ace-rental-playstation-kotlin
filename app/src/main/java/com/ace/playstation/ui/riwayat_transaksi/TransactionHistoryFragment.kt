package com.ace.playstation.ui.riwayat_transaksi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ace.playstation.R
import com.ace.playstation.adapter.TransactionHistoryAdapter
import com.ace.playstation.databinding.FragmentTransactionHistoryBinding
import com.ace.playstation.model.TransactionItem
import com.ace.playstation.repository.admin.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionHistoryFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionHistoryViewModel
    private lateinit var adapter: TransactionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupFilterSpinner()
        setupDatePicker()
        observeTransactions()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[TransactionHistoryViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = TransactionHistoryAdapter()
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TransactionHistoryFragment.adapter
        }
    }

    private fun setupFilterSpinner() {
        // Time filter setup
        val timeFilterOptions = arrayOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini", "Kustom")
        val timeSpinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, timeFilterOptions)
        timeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinnerTimeFilter.adapter = timeSpinnerAdapter
        binding.spinnerTimeFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.datePickerLayout.visibility = if (position == 5) View.VISIBLE else View.GONE
                when (position) {
                    0 -> applyCurrentFilters("all")
                    1 -> applyCurrentFilters("today")
                    2 -> applyCurrentFilters("week")
                    3 -> applyCurrentFilters("month")
                    4 -> applyCurrentFilters("year")
                    5 -> applyCurrentFilters("custom")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Category filter setup
        val categoryFilterOptions = arrayOf("Semua", "Rental", "Makanan", "Minuman")
        val categorySpinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, categoryFilterOptions)
        categorySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = categorySpinnerAdapter
        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = when (position) {
                    0 -> TransactionRepository.CATEGORY_ALL
                    1 -> TransactionRepository.CATEGORY_RENTAL
                    2 -> TransactionRepository.CATEGORY_MAKANAN
                    3 -> TransactionRepository.CATEGORY_MINUMAN
                    else -> TransactionRepository.CATEGORY_ALL
                }
                applyCurrentFilters(categoryFilter = category)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    // Helper to get current time filter
    private fun getSelectedTimeFilter(): String {
        return when (binding.spinnerTimeFilter.selectedItemPosition) {
            0 -> "today"
            1 -> "week"
            2 -> "month"
            3 -> "year"
            4 -> "custom"
            else -> "today"
        }
    }

    // Helper to get current category filter
    private fun getSelectedCategory(): String {
        return when (binding.spinnerCategoryFilter.selectedItemPosition) {
            0 -> TransactionRepository.CATEGORY_ALL
            1 -> TransactionRepository.CATEGORY_RENTAL
            2 -> TransactionRepository.CATEGORY_MAKANAN
            3 -> TransactionRepository.CATEGORY_MINUMAN
            else -> TransactionRepository.CATEGORY_ALL
        }
    }

    // Apply both filters
    private fun applyCurrentFilters(timeFilter: String = getSelectedTimeFilter(), categoryFilter: String = getSelectedCategory()) {
        if (timeFilter == "custom") {
            val fromDate = binding.tvDateFrom.text.toString()
            val toDate = binding.tvDateTo.text.toString()

            if (fromDate.isEmpty() || toDate.isEmpty()) {
                // Tampilkan pesan error ke pengguna
                binding.tvDateFrom.error = if (fromDate.isEmpty()) "Pilih tanggal awal" else null
                binding.tvDateTo.error = if (toDate.isEmpty()) "Pilih tanggal akhir" else null
                return
            }

            // Validasi fromDate <= toDate
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val from = dateFormat.parse(fromDate)
                val to = dateFormat.parse(toDate)
                if (from != null && to != null && from.after(to)) {
                    binding.tvDateFrom.error = "Tanggal awal tidak boleh setelah tanggal akhir"
                    return
                }
            } catch (e: Exception) {
                Log.e("TransactionHistory", "Error parsing dates for validation", e)
                return
            }

            viewModel.filterTransactions(timeFilter, fromDate, toDate, categoryFilter)
        } else {
            viewModel.filterTransactions(timeFilter, "", "", categoryFilter)
        }
    }

    private fun setupDatePicker() {
        binding.btnDateFrom.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.btnDateTo.setOnClickListener {
            showDatePickerDialog(false)
        }

        binding.btnApplyDateFilter.setOnClickListener {
            val fromDate = binding.tvDateFrom.text.toString()
            val toDate = binding.tvDateTo.text.toString()

            if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
                viewModel.filterTransactionsByDateRange(fromDate, toDate)
                binding.datePickerLayout.visibility = View.GONE
            }
        }
    }

    private fun showDatePickerDialog(isFromDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                if (isFromDate) {
                    binding.tvDateFrom.text = formattedDate
                } else {
                    binding.tvDateTo.text = formattedDate
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            updateTotalAmount(transactions)
        }
    }

    private fun updateTotalAmount(transactions: List<TransactionItem>) {
        val totalAmount = transactions.sumOf { it.amount }
        binding.tvTotalAmount.text = String.format("Rp %,.0f", totalAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}