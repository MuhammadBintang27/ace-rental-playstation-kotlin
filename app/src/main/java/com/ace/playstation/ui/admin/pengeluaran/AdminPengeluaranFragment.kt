package com.ace.playstation.ui.admin.pengeluaran

import PengeluaranAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ace.playstation.R
import com.ace.playstation.repository.admin.AdminPengeluaranRepository
import kotlinx.coroutines.launch

class AdminPengeluaranFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: PengeluaranAdapter
    private val repository = AdminPengeluaranRepository()
    private var currentSort = "tanggal"
    private var descending = true
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_pengeluaran, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewPengeluaran)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        adapter = PengeluaranAdapter(mutableListOf())
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        // Set up add stock button click
        val btnAdd = view.findViewById<View>(R.id.btn_tambah_pengeluaran)
        btnAdd?.setOnClickListener {
            showAddPengeluaranDialog()
        }


        setupFilters(view)
        refreshData()
    }

    private fun showAddPengeluaranDialog() {
        val dialog = AddPengeluaranDialog()
        dialog.setPengeluaranActionListener(object : AddPengeluaranDialog.PengeluaranActionListener {
            override fun onPengeluaranAdded() {
                refreshData() // Refresh data when new expense is added
            }
        })
        dialog.show(childFragmentManager, "AddPengeluaranDialog")
    }

    private fun setupFilters(view: View) {
        val radioGroup: RadioGroup = view.findViewById(R.id.radioGroupFilter)
        val btnSort: ImageButton = view.findViewById(R.id.btn_pengeluaran_sort)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.radio_pengeluaran_makanan -> "Makanan"
                R.id.radio_pengeluaran_minuman -> "Minuman"
                R.id.radio_pengeluaran_lainnya -> "Lainnya"
                else -> null
            }
            refreshData()
        }

        btnSort.setOnClickListener { v ->
            PopupMenu(requireContext(), v).apply {
                menuInflater.inflate(R.menu.admin_sort_pengeluaran_menu, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.sort_name_asc -> { currentSort = "nama_pengeluaran"; descending = false }
                        R.id.sort_name_desc -> { currentSort = "nama_pengeluaran"; descending = true }
                        R.id.sort_price_asc -> { currentSort = "jumlah_pengeluaran"; descending = false }
                        R.id.sort_price_desc -> { currentSort = "jumlah_pengeluaran"; descending = true }
                    }
                    refreshData()
                    true
                }
                show()
            }
        }
    }

    private fun refreshData() {
        swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            val pengeluaranList = repository.fetchPengeluaran(
                category = selectedCategory,
                sortBy = currentSort,
                descending = descending
            )
            adapter.updateData(pengeluaranList)
            swipeRefreshLayout.isRefreshing = false
        }
    }
}