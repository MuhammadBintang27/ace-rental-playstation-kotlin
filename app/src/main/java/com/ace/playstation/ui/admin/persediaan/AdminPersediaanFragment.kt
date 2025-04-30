package com.ace.playstation.ui.admin.persediaan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.adapters.ProductAdapter
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class AdminPersediaanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var warningText: TextView
    private val productService = ProductService()
    private var allProducts = listOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_persediaan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewProduk)
        toggleGroup = view.findViewById(R.id.toggleGroup)
        warningText = view.findViewById(R.id.tv_persediaan_warnings)

        // Set up RecyclerView
        productAdapter = ProductAdapter(requireContext())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }

        // Set up filter toggle buttons
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_all -> showAllProducts()
                    R.id.btn_makanan -> filterByCategory("Makanan")
                    R.id.btn_minuman -> filterByCategory("Minuman")
                }
            }
        }

        // Set up sort button click listener
        view.findViewById<View>(R.id.btn_sort).setOnClickListener { showSortingMenu(it) }

        // Set up add stock button click
        view.findViewById<View>(R.id.btn_tambah_persediaan).setOnClickListener {
            // Navigation to add product screen
            // findNavController().navigate(R.id.action_adminPersediaanFragment_to_addProductFragment)
            Toast.makeText(context, "Tambah produk baru"    , Toast.LENGTH_SHORT).show()
        }

        // Load initial data
        loadProducts()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                allProducts = productService.getAllProducts()
                productAdapter.setProducts(allProducts)
                updateWarningText()
            } catch (e: Exception) {
                // Handle error
                showError("Failed to load products: ${e.message}")
            }
        }
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
            warningText.visibility = View.GONE
        } else {
            warningText.visibility = View.VISIBLE
            warningText.text = "Warning: ${menipis.size} Stok Menipis!"
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
}