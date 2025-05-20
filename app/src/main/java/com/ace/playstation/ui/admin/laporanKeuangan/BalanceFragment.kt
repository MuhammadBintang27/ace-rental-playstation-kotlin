package com.ace.playstation.ui.admin.laporanKeuangan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ace.playstation.R
import com.ace.playstation.databinding.FragmentAdminLaporanKeuanganBinding
import com.ace.playstation.model.admin.BalanceViewModel
import com.ace.playstation.model.admin.FinancialEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.abs

class BalanceFragment : Fragment(R.layout.fragment_admin_laporan_keuangan) {
    private var _vb: FragmentAdminLaporanKeuanganBinding? = null
    private val vb get() = _vb!!
    private val vm: BalanceViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAdminLaporanKeuanganBinding.inflate(inflater, c, false).also { _vb = it }.root

    override fun onViewCreated(v: View, b: Bundle?) {
        super.onViewCreated(v, b)

        setupMonthYearSpinners()

        vb.adminLaporanKeuangan.setOnRefreshListener {
            vm.refreshAll()
        }

        vm.totalBalance.observe(viewLifecycleOwner) {
            vb.totalBalanceText.text = String.format("IDR %, .0f", it)
            vb.adminLaporanKeuangan.isRefreshing = false
        }
        vm.monthlyIncome.observe(viewLifecycleOwner) {
            vb.incomeText.text = String.format("IDR %, .0f", it)
        }
        vm.monthlyOutcome.observe(viewLifecycleOwner) {
            vb.outcomeText.text = String.format("IDR %, .0f", abs(it))
            android.util.Log.d("BalanceFragment", "Monthly Outcome Updated: $it")
        }
        vm.monthlyTotal.observe(viewLifecycleOwner) {
            vb.monthlyTotalText.text = String.format("IDR %, .0f", it)
        }
        vm.trend.observe(viewLifecycleOwner) { list ->
            drawChart(list)
        }

        vb.hariButton.setOnClickListener { vm.loadTrend(7) }
        vb.bulanButton.setOnClickListener { vm.loadTrend(30) }
        vb.tahunButton.setOnClickListener { vm.loadTrend(365) }

        vm.refreshAll()
    }

    private fun setupMonthYearSpinners() {
        val monthHint = "Month"
        val yearHint = "Year"

        val months = listOf(monthHint) + (1..12).map { String.format("%02d", it) }
        val years = listOf(yearHint) + (2025..2030).map { it.toString() }

        vb.monthSpinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, months).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

        vb.yearSpinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, years).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

        vb.monthSpinner.setSelection(0)
        vb.yearSpinner.setSelection(0)

        // Observe selected month/year and update spinners (offset by +1 because of hint)
        vm.selectedMonth.observe(viewLifecycleOwner) { month ->
            if (month in 1..12) vb.monthSpinner.setSelection(month)
        }
        vm.selectedYear.observe(viewLifecycleOwner) { year ->
            val index = years.indexOf(year.toString())

            if (index > 0) vb.yearSpinner.setSelection(index)
        }

        vb.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedYearIndex = vb.yearSpinner.selectedItemPosition

                if (position == 0 || selectedYearIndex == 0) return // Skip if hint is selected

                val year = years[selectedYearIndex].toInt()
                val month = months[position].toInt()
                vm.setMonthYear(year, month)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        vb.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMonthIndex = vb.monthSpinner.selectedItemPosition

                if (position == 0 || selectedMonthIndex == 0) return // Skip if hint is selected

                val year = years[position].toInt()
                val month = months[selectedMonthIndex].toInt()
                vm.setMonthYear(year, month)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun drawChart(data: List<FinancialEntry>) {
        val entriesIncome = data.mapIndexed { i, e -> Entry(i.toFloat(), e.income.toFloat()) }
        val entriesOutcome = data.mapIndexed { i, e -> Entry(i.toFloat(), e.outcome.toFloat()) }

        val setInc = LineDataSet(entriesIncome, "Pemasukan").apply { setDrawCircles(false) }
        val setOut = LineDataSet(entriesOutcome, "Pengeluaran").apply { setDrawCircles(false) }

        val lineData = LineData(setInc, setOut)
        vb.lineChart.data = lineData

        vb.lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt().coerceIn(0, data.lastIndex)
                    return data[idx].date.substring(5)
                }
            }
        }

        vb.lineChart.axisRight.isEnabled = false
        vb.lineChart.description.isEnabled = false
        vb.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }

    override fun onResume() {
        super.onResume()
        vm.refreshAll()
    }
}