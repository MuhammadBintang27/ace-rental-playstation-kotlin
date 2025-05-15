package com.ace.playstation.ui.admin.persediaan

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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ace.playstation.R
import com.ace.playstation.model.admin.Product
import com.ace.playstation.repository.admin.AdminProductServiceRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddOrIncrementProductDialog : DialogFragment() {

    interface ProductActionListener {
        fun onProductAdded()
    }

    private var listener: ProductActionListener? = null
    private val adminProductServiceRepository = AdminProductServiceRepository()
    private var products: List<Product> = emptyList()
    private var selectedProduct: Product? = null
    private var isIncrementMode = false

    fun setProductActionListener(listener: ProductActionListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_or_increment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogTitle = view.findViewById<TextView>(R.id.tv_dialog_title)
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.product_price_layout)
        val categoryGroup = view.findViewById<RadioGroup>(R.id.rg_product_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        // Load products for autocomplete
        lifecycleScope.launch {
            try {
                products = adminProductServiceRepository.getAllProducts()

                // Create adapter for product names
                val productNames = products.map { it.nama_produk }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    productNames
                )

                withContext(Dispatchers.Main) {
                    productNameAutoComplete.setAdapter(adapter)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load products: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle product selection
        productNameAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val productName = productNameAutoComplete.adapter.getItem(position).toString()
            handleProductSelection(productName, view)
        }

        // Also handle when user types exact product name
        productNameAutoComplete.doAfterTextChanged { text ->
            val productName = text.toString()
            val matchingProduct = products.find { it.nama_produk == productName }

            if (matchingProduct != null) {
                handleProductSelection(productName, view)
            } else {
                // Switch to add new product mode
                switchToAddMode(view)
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            if (validateInputs(view)) {
                if (isIncrementMode) {
                    incrementStock(view)
                } else {
                    addNewProduct(view)
                }
            }
        }

        // Start in add new product mode
        switchToAddMode(view)
    }

    private fun handleProductSelection(productName: String, view: View) {
        val product = products.find { it.nama_produk == productName }
        if (product != null) {
            selectedProduct = product
            switchToIncrementMode(view)
        }
    }

    private fun switchToAddMode(view: View) {
        isIncrementMode = false
        selectedProduct = null

        // Update UI
        val dialogTitle = view.findViewById<TextView>(R.id.tv_dialog_title)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.product_price_layout)
        val categoryGroup = view.findViewById<RadioGroup>(R.id.rg_product_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        dialogTitle.text = "Tambah Produk Baru"
        currentStockTextView.visibility = View.GONE
        priceLayout.visibility = View.VISIBLE
        categoryGroup.visibility = View.VISIBLE
        btnSave.text = "Simpan"
    }

    private fun switchToIncrementMode(view: View) {
        isIncrementMode = true

        // Update UI
        val dialogTitle = view.findViewById<TextView>(R.id.tv_dialog_title)
        val currentStockTextView = view.findViewById<TextView>(R.id.tv_current_stock)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.product_price_layout)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)
        val categoryGroup = view.findViewById<RadioGroup>(R.id.rg_product_category)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        dialogTitle.text = "Tambah Stok & Ubah Harga"
        currentStockTextView.visibility = View.VISIBLE
        currentStockTextView.text = "Current stock: ${selectedProduct?.stok_persediaan ?: 0}"

        // Enable price change
        priceLayout.visibility = View.VISIBLE
        categoryGroup.visibility = View.GONE
        btnSave.text = "Update"

        // Set current price as default in price input
        priceInput.setText(selectedProduct?.harga?.toString() ?: "")
    }

    private fun validateInputs(view: View): Boolean {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)

        val name = productNameAutoComplete.text.toString().trim()
        val stockDeltaString = stockDeltaInput.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(context, "Product name cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        if (stockDeltaString.isEmpty()) {
            Toast.makeText(context, "Stock amount cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val stockDelta = stockDeltaString.toInt()
            if (stockDelta < 0) {
                Toast.makeText(context, "Stock amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid stock amount", Toast.LENGTH_SHORT).show()
            return false
        }

        // If we're adding a new product, validate price
        if (!isIncrementMode) {
            val priceString = priceInput.text.toString().trim()
            if (priceString.isEmpty()) {
                Toast.makeText(context, "Price cannot be empty", Toast.LENGTH_SHORT).show()
                return false
            }

            try {
                val price = priceString.toDouble()
                if (price <= 0) {
                    Toast.makeText(context, "Price must be greater than 0", Toast.LENGTH_SHORT).show()
                    return false
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Invalid price format", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun incrementStock(view: View) {
        if (selectedProduct == null) {
            Toast.makeText(context, "No product selected", Toast.LENGTH_SHORT).show()
            return
        }

        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)
        val stockDelta = stockDeltaInput.text.toString().toInt()

        var updatedPrice = selectedProduct!!.harga
        val priceString = priceInput.text.toString().trim()
        if (priceString.isNotEmpty()) {
            updatedPrice = priceString.toDouble()
        }

        // Create a new product with updated stock
        val updatedProduct = selectedProduct!!.copy(
            stok_persediaan = selectedProduct!!.stok_persediaan + stockDelta,
            harga = updatedPrice
        )

        saveProduct(updatedProduct)
    }

    private fun addNewProduct(view: View) {
        val productNameAutoComplete = view.findViewById<AutoCompleteTextView>(R.id.act_product_name)
        val stockDeltaInput = view.findViewById<TextInputEditText>(R.id.et_stock_delta)
        val priceInput = view.findViewById<TextInputEditText>(R.id.et_product_price)
        val categoryGroup = view.findViewById<RadioGroup>(R.id.rg_product_category)

        val name = productNameAutoComplete.text.toString().trim()
        val stockDelta = stockDeltaInput.text.toString().toInt()
        val price = priceInput.text.toString().toDouble()

        val selectedCategoryId = categoryGroup.checkedRadioButtonId
        val selectedRadioButton = view.findViewById<RadioButton>(selectedCategoryId)
        val category = selectedRadioButton.text.toString()

        // Create new product
        val newProduct = Product(
            nama_produk = name,
            harga = price,
            stok_persediaan = stockDelta,
            kategori = category
        )

        saveProduct(newProduct)
    }

    private fun saveProduct(product: Product) {
        lifecycleScope.launch {
            try {
                val success = adminProductServiceRepository.addProduct(product)
                if (success) {
                    val message = if (isIncrementMode) {
                        "Product successfully updated"
                    } else {
                        "Product successfully added"
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        listener?.onProductAdded()
                        dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save product", Toast.LENGTH_SHORT).show()
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