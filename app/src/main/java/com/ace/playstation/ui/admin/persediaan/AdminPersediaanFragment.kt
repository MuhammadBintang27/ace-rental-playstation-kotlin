package com.ace.playstation.ui.admin.persediaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ace.playstation.R
import com.ace.playstation.adapters.ProductAdapter
import kotlinx.coroutines.launch

class AdminPersediaanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var toggleGroup: RadioGroup
    private lateinit var warningText: TextView
    private val productService = ProductService()
    private var allProducts = listOf<Product>()
    private lateinit var warningOverlay: View
    private lateinit var warningTextView: TextView
    private lateinit var closeWarningBtn: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_persediaan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Swipe to refresh logic
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        warningOverlay = view.findViewById(R.id.warningOverlay)
        warningTextView = view.findViewById(R.id.tv_persediaan_warnings)
        closeWarningBtn = view.findViewById(R.id.btn_close_warning)

        closeWarningBtn.setOnClickListener {
            warningOverlay.visibility = View.GONE
        }

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewProduk)
        toggleGroup = view.findViewById(R.id.radioGroupFilter)
        warningText = view.findViewById(R.id.tv_persediaan_warnings)

        // Set up RecyclerView
        productAdapter = ProductAdapter(requireContext())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }

        // Set up filter toggle buttons
        toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_all -> showAllProducts()
                R.id.radio_makanan -> filterByCategory("Makanan")
                R.id.radio_minuman -> filterByCategory("Minuman")
            }
        }

        // Set up sort button click listener
        view.findViewById<View>(R.id.btn_sort).setOnClickListener { showSortingMenu(it) }

        // Set up add stock button click - UPDATED HERE
        view.findViewById<View>(R.id.btn_tambah_persediaan).setOnClickListener {
            showAddProductDialog()
        }

        // Load initial data
        loadProducts()
    }

    // NEW METHOD: Shows the add product dialog
    private fun showAddProductDialog() {
        val dialog = AddOrIncrementProductDialog()
        dialog.setProductActionListener(object : AddOrIncrementProductDialog.ProductActionListener {
            override fun onProductAdded() {
                refreshData() // Refresh your data display
            }
        })
        dialog.show(childFragmentManager, "AddOrIncrementProductDialog")
    }

//    // NEW METHOD: Implementing AddProductListener interface
//    override fun onProductAdded() {
//        refreshData()
//    }

    private fun loadProducts() {
        refreshData()
    }

    private fun showAllProducts() {
        productAdapter.setProducts(allProducts)
    }

    private fun filterByCategory(category: String) {
        val filtered = allProducts.filter { it.kategori.equals(category, ignoreCase = true) }
        productAdapter.setProducts(filtered)
    }

    private fun updateWarningText() {
        val menipis = allProducts.filter { it.isStokMenipis }
        if (menipis.isEmpty()) {
            warningOverlay.visibility = View.GONE
        } else {
            warningOverlay.visibility = View.VISIBLE
            warningText.text = "⚠️ Warning: ${menipis.size} produk stok menipis"
        }
    }

    private fun showSortingMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.sort_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort_name_asc -> {
                    val sorted = allProducts.sortedBy { it.nama_produk }
                    productAdapter.setProducts(sorted)
                    true
                }
                R.id.sort_name_desc -> {
                    val sorted = allProducts.sortedByDescending { it.nama_produk }
                    productAdapter.setProducts(sorted)
                    true
                }
                R.id.sort_price_asc -> {
                    val sorted = allProducts.sortedBy { it.harga }
                    productAdapter.setProducts(sorted)
                    true
                }
                R.id.sort_price_desc -> {
                    val sorted = allProducts.sortedByDescending { it.harga }
                    productAdapter.setProducts(sorted)
                    true
                }
                R.id.sort_stock_asc -> {
                    val sorted = allProducts.sortedBy { it.stok_persediaan }
                    productAdapter.setProducts(sorted)
                    true
                }
                R.id.sort_stock_desc -> {
                    val sorted = allProducts.sortedByDescending { it.stok_persediaan }
                    productAdapter.setProducts(sorted)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun refreshData() {
        swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            try {
                allProducts = productService.getAllProducts()

                // Apply the current filter
                when (toggleGroup.checkedRadioButtonId) {
                    R.id.radio_all -> showAllProducts()
                    R.id.radio_makanan -> filterByCategory("Makanan")
                    R.id.radio_minuman -> filterByCategory("Minuman")
                    else -> showAllProducts()
                }

                updateWarningText()
            } catch (e: Exception) {
                showError("Refresh failed: ${e.message}")
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}