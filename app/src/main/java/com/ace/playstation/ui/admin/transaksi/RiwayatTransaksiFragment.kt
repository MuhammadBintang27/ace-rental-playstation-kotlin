package com.ace.playstation.ui.admin.transaksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ace.playstation.R
import com.ace.playstation.adapter.TransactionHistoryAdapterAdmin
import com.ace.playstation.databinding.FragmentAdminRiwayatTransaksiBinding
import com.ace.playstation.model.admin.AdminOverviewViewModel
import com.ace.playstation.model.TransactionItem
import com.ace.playstation.repository.admin.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RiwayatTransaksiFragment : Fragment() {

    private var _binding: FragmentAdminRiwayatTransaksiBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AdminOverviewViewModel
    private lateinit var adapter: TransactionHistoryAdapterAdmin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRiwayatTransaksiBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this)[AdminOverviewViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = TransactionHistoryAdapterAdmin()
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RiwayatTransaksiFragment.adapter
        }
    }

    private fun setupFilterSpinner() {
        // Time filter setup
        val timeFilterOptions = arrayOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini", "Kustom")
        val timeSpinnerAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, // Teks putih
            timeFilterOptions)
        timeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinnerTimeFilterAdmin.adapter = timeSpinnerAdapter
        binding.spinnerTimeFilterAdmin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 5) {
                    binding.datePickerLayoutAdmin.visibility = View.VISIBLE // show date picker
                } else {
                    val timeFilter = when (position) {
                        0 -> "all"
                        1 -> "today"
                        2 -> "week"
                        3 -> "month"
                        4 -> "year"
                        else -> "all"
                    }
                    applyCurrentFilters(timeFilter = timeFilter, categoryFilter = getSelectedCategory())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Category filter setup
        val categoryFilterOptions = arrayOf("Semua", "Rental", "Makanan", "Minuman")

        // Gunakan layout custom untuk teks spinner
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, // Teks putih
            categoryFilterOptions
        )

        // Gunakan layout custom untuk dropdown spinner
        categorySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCategoryFilterAdmin.adapter = categorySpinnerAdapter
        binding.spinnerCategoryFilterAdmin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyCurrentFilters(
                    timeFilter = getSelectedTimeFilter(),
                    categoryFilter = getSelectedCategory()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    // Helper to get current time filter
    private fun getSelectedTimeFilter(): String {
        return when (binding.spinnerTimeFilterAdmin.selectedItemPosition) {
            0 -> "all"
            1 -> "today"
            2 -> "week"
            3 -> "month"
            4 -> "year"
            5 -> "custom"
            else -> "all"
        }
    }

    // Helper to get current category filter
    private fun getSelectedCategory(): String {
        return when (binding.spinnerCategoryFilterAdmin.selectedItemPosition) {
            0 -> TransactionRepository.CATEGORY_ALL
            1 -> TransactionRepository.CATEGORY_RENTAL
            2 -> TransactionRepository.CATEGORY_MAKANAN
            3 -> TransactionRepository.CATEGORY_MINUMAN
            else -> TransactionRepository.CATEGORY_ALL
        }
    }

    // Apply both filters
    private fun applyCurrentFilters(
        timeFilter: String = getSelectedTimeFilter(),
        categoryFilter: String = getSelectedCategory()
    ) {
        // compute from/to strings based on the selected timeFilter
        val (fromDate, toDate) = when (timeFilter) {
            "today" -> {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                today to today
            }
            "week" -> {
                val cal = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                df.format(cal.time) to df.format(Date())
            }
            "month" -> {
                val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                df.format(cal.time) to df.format(Date())
            }
            "year" -> {
                val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, 1) }
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                df.format(cal.time) to df.format(Date())
            }
            "custom" -> {
                binding.tvDateFromAdmin.text.toString() to binding.tvDateToAdmin.text.toString()
            }
            else -> {
                // fallback to all time
                "1970-01-01" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }
        }

        // hide the custom‐date picker if we’re not in "custom"
        if (timeFilter != "custom") {
            binding.datePickerLayoutAdmin.visibility = View.GONE
        }

        // now load summaries with the computed range + selected category
        viewModel.loadSummaries(
            from     = fromDate,
            to       = toDate,
            category = categoryFilter
        )
    }

    private fun setupDatePicker() {
        binding.btnDateFromAdmin.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.btnDateToAdmin.setOnClickListener {
            showDatePickerDialog(false)
        }

        binding.btnApplyDateFilterAdmin.setOnClickListener {
            val from = binding.tvDateFromAdmin.text.toString()
            val to   = binding.tvDateToAdmin.text.toString()
            if (from.isNotBlank() && to.isNotBlank()) {
                applyCurrentFilters(timeFilter = "custom", categoryFilter = getSelectedCategory())
                binding.datePickerLayoutAdmin.visibility = View.GONE
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
                    binding.tvDateFromAdmin.text = formattedDate
                } else {
                    binding.tvDateToAdmin.text = formattedDate
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun observeTransactions() {
        viewModel.summaries.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val totalIncome = list.sumOf { it.totalIncome }
            binding.tvTotalAmountAdmin.text =
                String.format("Rp %,.0f", totalIncome)
        }
    }


    private fun updateTotalAmount(transactions: List<TransactionItem>) {
        val totalAmount = transactions.sumOf { it.amount }
        binding.tvTotalAmountAdmin.text = String.format("Rp %,.0f", totalAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
