package com.ace.playstation.ui.admin.laporanKeuangan

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ace.playstation.R
import com.ace.playstation.databinding.FragmentAdminLaporanKeuanganBinding
import com.ace.playstation.viewmodel.BalanceViewModel
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.*
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


        // Swipe to refresh logic
        vb.adminLaporanKeuangan.setOnRefreshListener {
            refreshData()
        }

        // Observe totals
        vm.totalBalance.observe(viewLifecycleOwner) {
            vb.totalBalanceText.text = String.format("IDR %, .0f", it)
            vb.adminLaporanKeuangan.isRefreshing = false // Stop animation after load
        }
        vm.monthlyIncome.observe(viewLifecycleOwner) {
            vb.incomeText.text = String.format("IDR %, .0f", it)
        }
        vm.monthlyOutcome.observe(viewLifecycleOwner) {
            vb.outcomeText.text = String.format("IDR %, .0f", abs(it))
        }

        // Observe and draw trend
        vm.trend.observe(viewLifecycleOwner) { list ->
                drawChart(list)
        }

        // Radio buttons
        vb.hariButton.setOnClickListener  { vm.loadTrend(7) }
        vb.bulanButton.setOnClickListener { vm.loadTrend(30) }
        vb.tahunButton.setOnClickListener { vm.loadTrend(365) }

        // Initial load
        refreshData()
    }

    private fun drawChart(data: List<com.ace.playstation.model.FinancialEntry>) {
        val entriesIncome  = data.mapIndexed { i, e -> Entry(i.toFloat(), e.income.toFloat()) }
        val entriesOutcome = data.mapIndexed { i, e -> Entry(i.toFloat(), e.outcome.toFloat()) }

        val setInc  = LineDataSet(entriesIncome,  "Pemasukan").apply { setDrawCircles(false) }
        val setOut = LineDataSet(entriesOutcome,"Pengeluaran").apply { setDrawCircles(false) }

        val lineData = LineData(setInc, setOut)
        vb.lineChart.data = lineData

        // X-axis as date labels
        vb.lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt().coerceIn(0, data.lastIndex)
                    return data[idx].date.substring(5) // "MM-DD"
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
        vm.loadTotalBalance()
        vm.loadMonthlySummary()
    }

    private fun refreshData() {
        vm.loadTotalBalance()
        vm.loadMonthlySummary()
        vm.loadTrend(30)
    }



}
