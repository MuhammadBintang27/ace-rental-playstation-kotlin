package com.ace.playstation.ui.transaksi_penjualan.PenjualanFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ace.playstation.R
import com.ace.playstation.adapter.KonfirmasiAdapter
import com.ace.playstation.adapter.PenjualanAdapter
import com.ace.playstation.databinding.FragmentPenjualanBinding
import com.ace.playstation.ui.transaksi_penjualan.TransaksiPenjualanViewModel.PenjualanViewModel

class PenjualanFragment : Fragment() {
    private lateinit var binding: FragmentPenjualanBinding
    private lateinit var viewModel: PenjualanViewModel
    private lateinit var adapter: PenjualanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPenjualanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PenjualanViewModel::class.java)

        adapter = PenjualanAdapter { position, jumlah ->
            viewModel.updateJumlahItem(position, jumlah)
        }

        binding.recyclerViewProduk.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PenjualanFragment.adapter
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        binding.radioGroupKategori.setOnCheckedChangeListener { _, checkedId ->
            val kategori = when (checkedId) {
                R.id.radioButtonSemua -> "semua"
                R.id.radioButtonMakanan -> "Makanan"
                R.id.radioButtonMinuman -> "Minuman"
                else -> "semua"
            }
            viewModel.loadProduk(kategori)
        }

        binding.buttonSimpan.setOnClickListener {
            showKonfirmasiDialog()
        }

        viewModel.penjualanItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        viewModel.totalPenjualan.observe(viewLifecycleOwner) { total ->
            binding.textViewTotal.text = "Total: Rp ${formatPrice(total)}"
            binding.buttonSimpan.isEnabled = total > 0
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showKonfirmasiDialog() {
        val selectedItems = viewModel.getAllCartItems()

        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada item yang dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_konfirmasi_transaksi, null)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewKonfirmasi)
        val textViewTotal = dialogView.findViewById<TextView>(R.id.textViewTotalKonfirmasi)
        val buttonBatal = dialogView.findViewById<Button>(R.id.buttonBatal)
        val buttonLanjutkan = dialogView.findViewById<Button>(R.id.buttonLanjutkan)

        val konfirmasiAdapter = KonfirmasiAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = konfirmasiAdapter

        konfirmasiAdapter.submitList(selectedItems)

        val totalHarga = viewModel.totalPenjualan.value ?: 0.0
        textViewTotal.text = "Total: Rp ${formatPrice(totalHarga)}"

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonBatal.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonLanjutkan.setOnClickListener {
            viewModel.saveTransaksi()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun formatPrice(price: Double): String {
        return String.format("%,.0f", price)
    }
}