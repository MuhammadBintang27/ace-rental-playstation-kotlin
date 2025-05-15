package com.ace.playstation.ui.admin.pengeluaran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ace.playstation.R
import com.ace.playstation.model.admin.PengeluaranDto
import com.ace.playstation.model.admin.Product
import com.ace.playstation.repository.admin.AdminPengeluaranRepository
import com.ace.playstation.repository.admin.AdminProductServiceRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddPengeluaranDialog : DialogFragment() {

    interface PengeluaranActionListener {
        fun onPengeluaranAdded()
    }

    private var listener: PengeluaranActionListener? = null
    private val productRepository = AdminProductServiceRepository()
    private val pengeluaranRepository = AdminPengeluaranRepository()
    private var products: List<Product> = emptyList()
    private var selectedProduct: Product? = null
    private var selectedCategory: String = "Lainnya"
    private var lainnyaSuggestions = listOf("Listrik", "Sewa", "Gaji Karyawan", "Internet", "Air", "Kebersihan", "Maintenance")

    fun setPengeluaranActionListener(listener: PengeluaranActionListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_pengeluaran, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryGroup = view.findViewById<RadioGroup>(R.id.rg_pengeluaran_category)
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        // Load products first
        loadProducts()

        // Set up category selection
        categoryGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_category_makanan -> {
                    selectedCategory = "Makanan"
                    setupProductSelection(view, "Makanan")
                }
                R.id.rb_category_minuman -> {
                    selectedCategory = "Minuman"
                    setupProductSelection(view, "Minuman")
                }
                R.id.rb_category_lainnya -> {
                    selectedCategory = "Lainnya"
                    setupLainnyaSelection(view)
                }
            }
        }

        // Setup initial state
        val lainnyaRadioButton = view.findViewById<RadioButton>(R.id.rb_category_lainnya)
        lainnyaRadioButton.isChecked = true
        setupLainnyaSelection(view)

        // Handle product selection
        productNameAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedText = productNameAutoComplete.adapter.getItem(position).toString()
            handleSelectionChanged(view, selectedText)
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            if (validateInputs(view)) {
                savePengeluaran(view)
            }
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                products = productRepository.getAllProducts()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load products: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupProductSelection(view: View, category: String) {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val productNameLayout = view.findViewById<TextInputLayout>(R.id.product_name_layout)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val stockDeltaLayout = view.findViewById<TextInputLayout>(R.id.stock_delta_layout)

        // Filter products by category
        val filteredProducts = products.filter { it.kategori.equals(category, ignoreCase = true) }
        val productNames = filteredProducts.map { it.nama_produk }

        // Create adapter for product names
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            productNames
        )

        productNameAutoComplete.setText("") // Clear previous selection
        productNameAutoComplete.setAdapter(adapter)
        selectedProduct = null

        // Show stock fields
        productNameLayout.hint = "Pilih produk"
        currentStockTextView.visibility = View.VISIBLE
        stockDeltaLayout.visibility = View.VISIBLE
        stockDeltaLayout.hint = "Jumlah (Qty)"

        // Reset stock info
        currentStockTextView.text = "Stok saat ini: 0"

    }

    private fun setupLainnyaSelection(view: View) {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val productNameLayout = view.findViewById<TextInputLayout>(R.id.product_name_layout)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val stockDeltaLayout = view.findViewById<TextInputLayout>(R.id.stock_delta_layout)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)

        // Clear any previous value
        stockDeltaInput.setText("")


        // Create adapter for lainnya suggestions
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            lainnyaSuggestions
        )

        productNameAutoComplete.setText("") // Clear previous selection
        productNameAutoComplete.setAdapter(adapter)
        selectedProduct = null

        // Hide stock fields
        productNameLayout.hint = "Nama pengeluaran"
        currentStockTextView.visibility = View.GONE
        stockDeltaLayout.visibility = View.GONE
    }

    private fun handleSelectionChanged(view: View, selectedText: String) {
        if (selectedCategory == "Lainnya") {
            // Nothing special to do for Lainnya
            return
        }

        // For Makanan or Minuman, find the selected product and show stock info
        val product = products.find { it.nama_produk == selectedText }
        if (product != null) {
            selectedProduct = product
            val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
            currentStockTextView.text = "Stok saat ini: ${product.stok_persediaan}"
        }
    }

    private fun validateInputs(view: View): Boolean {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)

        val name = productNameAutoComplete.text.toString().trim()
        val stockDeltaString = stockDeltaInput.text.toString().trim()
        val priceString = priceInput.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Nama pengeluaran tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (stockDeltaString.isEmpty()) {
            if (selectedCategory != "Lainnya") {
                Toast.makeText(context, "Jumlah (Qty) tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
        }


        if (priceString.isEmpty()) {
            Toast.makeText(context, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedCategory != "Lainnya") {
            try {
                val stockDelta = stockDeltaString.toInt()
                if (stockDelta <= 0) {
                    Toast.makeText(context, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return false
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        try {
            val price = priceString.toDouble()
            if (price <= 0) {
                Toast.makeText(context, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Harga harus berupa angka", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun savePengeluaran(view: View) {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)

        val nama = productNameAutoComplete.text.toString().trim()
        val qty = if (selectedCategory != "Lainnya") stockDeltaInput.text.toString().toInt() else 0
        val jumlah = priceInput.text.toString().toDouble()

        // Generate today's date in appropriate format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Process differently based on category
        if (selectedCategory == "Lainnya") {
            // For Lainnya, just record the expense
            saveLainnyaPengeluaran(nama, jumlah, currentDate)
        } else {
            // For Makanan or Minuman, update stock and record expense
            saveProductPengeluaran(nama, qty, jumlah, currentDate)
        }
    }

    private fun saveLainnyaPengeluaran(nama: String, jumlah: Double, tanggal: String) {
        val pengeluaran = PengeluaranDto(
            nama_pengeluaran = nama,
            kategori = "Lainnya",
            tanggal = tanggal,
            jumlah_pengeluaran = jumlah,
            stok_tambahan = 0,
            bukti = null
        )

        saveToDatabase(pengeluaran)
    }

    private fun saveProductPengeluaran(nama: String, qty: Int, jumlah: Double, tanggal: String) {
        // First, update product stock
        val product = selectedProduct
//        /*
        if (product != null) {
            // Calculate new stock
            if (qty < 0) {
                Toast.makeText(context, "Stok yang ditambah kurang dari 0", Toast.LENGTH_SHORT).show()
                return
            }
            val newStock = product.stok_persediaan + qty

            // Update product in database
            val updatedProduct = product.copy(
                stok_persediaan = newStock
            )

            lifecycleScope.launch {
                try {
                    val success = productRepository.addProduct(updatedProduct)
                    if (success) {
                        // Now record the expense
                        val pengeluaran = PengeluaranDto(
                            nama_pengeluaran = nama,
                            kategori = selectedCategory,
                            tanggal = tanggal,
                            jumlah_pengeluaran = jumlah,
                            stok_tambahan = qty,
                            bukti = null
                        )

                        saveToDatabase(pengeluaran)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Gagal menyimpan perubahan stok", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToDatabase(pengeluaran: PengeluaranDto) {
        lifecycleScope.launch {
            try {
                val success = pengeluaranRepository.addPengeluaran(pengeluaran)
                if (success) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Pengeluaran berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        listener?.onPengeluaranAdded()
                        dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Gagal menyimpan pengeluaran", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }
}