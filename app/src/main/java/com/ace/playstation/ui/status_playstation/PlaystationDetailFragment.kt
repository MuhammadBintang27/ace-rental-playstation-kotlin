package com.ace.playstation.ui.status_playstation

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ace.playstation.R
import com.ace.playstation.data.model.TransaksiRental
import com.ace.playstation.data.repository.PlayStationRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlaystationDetailFragment : Fragment() {
    private val playStationRepository = PlayStationRepository()

    private lateinit var tvStatus: TextView
    private lateinit var tvNomorUnit: TextView
    private lateinit var btnMulaiPermainan: Button
    private lateinit var btnAkhiriPermainan: Button
    private lateinit var spinnerTipeMain: Spinner
    private lateinit var etJumlahMenit: EditText
    private lateinit var tvWaktuMulai: TextView
    private lateinit var tvWaktuSelesai: TextView

    private var unitId: Int = 0
    private var nomorUnit: String = ""
    private var statusPlaystation: String = "Available"
    private var waktuMulai: String = ""
    private var waktuSelesai: String = ""
    private var tipePermainan: String = ""
    private var jumlahMenit: Int = 0

    private val tipePermainanArray = arrayOf("Tetap", "Personal")

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    private val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_playstation_detail, container, false)

        arguments?.let {
            unitId = it.getInt("unit_id", 0)
        }

        // Inisialisasi view
        tvStatus = view.findViewById(R.id.tvStatus)
        tvNomorUnit = view.findViewById(R.id.tvNomorUnit)
        btnMulaiPermainan = view.findViewById(R.id.btnMulaiPermainan)
        btnAkhiriPermainan = view.findViewById(R.id.btnAkhiriPermainan)
        spinnerTipeMain = view.findViewById(R.id.spinnerTipeMain)
        etJumlahMenit = view.findViewById(R.id.etJumlahMenit)
        tvWaktuMulai = view.findViewById(R.id.tvWaktuMulai)
        tvWaktuSelesai = view.findViewById(R.id.tvWaktuSelesai)

        fetchPlayStationDetail(unitId)
        setupSpinners()
        updateUI()

        btnMulaiPermainan.setOnClickListener { startGame() }
        btnAkhiriPermainan.setOnClickListener { endGame() }

        spinnerTipeMain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etJumlahMenit.visibility = if (spinnerTipeMain.selectedItem.toString() == "Tetap") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private fun fetchPlayStationDetail(unitId: Int) {
        lifecycleScope.launch {
            try {
                val playStation = playStationRepository.getPlaystationById(unitId)

                if (playStation != null) {
                    nomorUnit = playStation.nomorUnit
                    statusPlaystation = playStation.status
                    tipePermainan = playStation.tipeMain

                    waktuMulai = parseTimeString(playStation.waktuMulai ?: "")
                    waktuSelesai = parseTimeString(playStation.waktuSelesai ?: "")

                    updateUI()
                } else {
                    Log.e("PlaystationDetail", "PlayStation dengan ID $unitId tidak ditemukan.")
                    Toast.makeText(requireContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PlaystationDetail", "Gagal mengambil data", e)
                Toast.makeText(requireContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseTimeString(timeString: String): String {
        if (timeString.isBlank()) return ""

        return try {
            val parsedDate = try {
                isoDateFormat.parse(timeString)
            } catch (e: ParseException) {
                try {
                    sqlDateFormat.parse(timeString)
                } catch (e: ParseException) {
                    null
                }
            }

            parsedDate?.let { isoDateFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipePermainanArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipeMain.adapter = adapter

        val currentTipeIndex = tipePermainanArray.indexOfFirst { it == tipePermainan }
        if (currentTipeIndex != -1) {
            spinnerTipeMain.setSelection(currentTipeIndex)
        }
    }

    private fun updateUI() {
        tvStatus.text = "Status: $statusPlaystation"
        tvNomorUnit.text = "Nomor Unit: $nomorUnit"

        when (statusPlaystation) {
            "Available" -> {
                btnMulaiPermainan.visibility = View.VISIBLE
                btnAkhiriPermainan.visibility = View.GONE
                spinnerTipeMain.isEnabled = true
                etJumlahMenit.visibility = if (spinnerTipeMain.selectedItem.toString() == "Tetap") View.VISIBLE else View.GONE

                tvWaktuMulai.text = "Mulai: -"
                tvWaktuSelesai.text = "Selesai: -"
            }
            "Rented" -> {
                btnMulaiPermainan.visibility = View.GONE
                btnAkhiriPermainan.visibility = View.VISIBLE
                spinnerTipeMain.isEnabled = false
                etJumlahMenit.visibility = View.GONE

                when (tipePermainan) {
                    "Tetap" -> {
                        if (waktuMulai.isNotEmpty()) {
                            tvWaktuMulai.text = "Mulai: ${formatTimeString(waktuMulai)}"
                        }
                        if (waktuSelesai.isNotEmpty()) {
                            tvWaktuSelesai.text = "Selesai: ${formatTimeString(waktuSelesai)}"
                        } else if (waktuMulai.isNotEmpty() && jumlahMenit > 0) {
                            val waktuMulaiDate = isoDateFormat.parse(waktuMulai)
                            val waktuSelesaiCalc = waktuMulaiDate?.time?.plus(jumlahMenit * 60 * 1000)
                            if (waktuSelesaiCalc != null) {
                                val calculatedWaktuSelesai = isoDateFormat.format(Date(waktuSelesaiCalc))
                                tvWaktuSelesai.text = "Selesai: ${formatTimeString(calculatedWaktuSelesai)}"
                            }
                        }
                    }
                    "Personal" -> {
                        if (waktuMulai.isNotEmpty()) {
                            tvWaktuMulai.text = "Mulai: ${formatTimeString(waktuMulai)}"
                        }
                        tvWaktuSelesai.text = "Selesai: -"
                    }
                }
            }
        }
    }

    private fun startGame() {
        if (spinnerTipeMain.selectedItem.toString() == "Tetap") {
            val menitInput = etJumlahMenit.text.toString().toIntOrNull()
            if (menitInput == null || menitInput <= 0) {
                Toast.makeText(requireContext(), "Masukkan jumlah menit yang valid", Toast.LENGTH_SHORT).show()
                return
            }
            jumlahMenit = menitInput
        }

        waktuMulai = isoDateFormat.format(Date())

        val waktuSelesaiFormatted = if (spinnerTipeMain.selectedItem.toString() == "Tetap") {
            val waktuMulaiDate = isoDateFormat.parse(waktuMulai)
            val waktuSelesaiCalc = waktuMulaiDate?.time?.plus(jumlahMenit * 60 * 1000)
            if (waktuSelesaiCalc != null) isoDateFormat.format(Date(waktuSelesaiCalc)) else null
        } else {
            null
        }

        lifecycleScope.launch {
            val success = playStationRepository.updatePlayStation(
                unitId,
                "Rented",
                spinnerTipeMain.selectedItem.toString(),
                waktuMulai,
                waktuSelesaiFormatted
            )
            if (success) {
                Log.d("Playstation", "Data berhasil diperbarui")
                statusPlaystation = "Rented"
                arguments = Bundle().apply {
                    putInt("unit_id", unitId)
                    putString("nomor_unit", nomorUnit)
                    putString("status", "Rented")
                    putString("tipe_main", spinnerTipeMain.selectedItem.toString())
                    putString("waktu_mulai", waktuMulai)
                    putString("waktu_selesai", waktuSelesaiFormatted ?: "")
                }
                updateUI()
            } else {
                Log.e("Playstation", "Gagal memperbarui data")
                Toast.makeText(requireContext(), "Gagal memulai permainan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun endGame() {
        try {
            val waktuPerhitungan: Date? = when {
                tipePermainan == "Tetap" && waktuSelesai.isNotEmpty() -> isoDateFormat.parse(waktuSelesai)
                else -> Date()
            }

            val waktuMulaiDate: Date? = try {
                when {
                    waktuMulai.isNotEmpty() && waktuMulai.contains('T') -> isoDateFormat.parse(waktuMulai)
                    waktuMulai.isNotEmpty() -> sqlDateFormat.parse(waktuMulai)
                    else -> null
                }
            } catch (e: ParseException) {
                null
            }

            val durasiMenit = if (waktuMulaiDate != null && waktuPerhitungan != null) {
                ((waktuPerhitungan.time - waktuMulaiDate.time) / (1000 * 60)).toInt()
            } else 0

            val hargaPerMenit = 9000.0 / 60
            val totalHarga = (durasiMenit * hargaPerMenit).toInt()

            showHargaDialog(durasiMenit, totalHarga, waktuMulaiDate, waktuPerhitungan) {
                lifecycleScope.launch {
                    val success = playStationRepository.updatePlayStation(
                        unitId, "Available", tipePermainan, null, null
                    )

                    if (success) {
                        Log.d("Playstation", "Data berhasil diperbarui")
                        statusPlaystation = "Available"
                        this@PlaystationDetailFragment.waktuMulai = ""
                        this@PlaystationDetailFragment.waktuSelesai = ""
                        jumlahMenit = 0
                        updateUI()
                    } else {
                        Log.e("Playstation", "Gagal memperbarui data")
                        Toast.makeText(requireContext(), "Gagal mengakhiri permainan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Playstation", "Error parsing waktu", e)
            Toast.makeText(requireContext(), "Gagal memproses waktu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTimeString(timeString: String): String {
        return try {
            val parsedDate = try {
                isoDateFormat.parse(timeString)
            } catch (e: ParseException) {
                sqlDateFormat.parse(timeString)
            }

            displayFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            timeString
        }
    }

    private fun showHargaDialog(durasiMenit: Int, totalHarga: Int, waktuMulai: Date?, waktuSelesai: Date?, onDismiss: () -> Unit = {}) {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedHarga = currencyFormatter.format(totalHarga)
        val waktuMulaiStr = waktuMulai?.let { isoDateFormat.format(it) } ?: "-"
        val waktuSelesaiStr = waktuSelesai?.let { isoDateFormat.format(it) } ?: "-"

        AlertDialog.Builder(requireContext())
            .setTitle("Perhitungan Harga")
            .setMessage("Durasi Bermain: $durasiMenit menit\nTotal Harga: $formattedHarga")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()

                lifecycleScope.launch {
                    val transaksi = TransaksiRental(
                        unit_id = unitId,
                        shift_id = 1,
                        kategori_main = tipePermainan,
                        waktu_mulai = waktuMulaiStr,
                        waktu_selesai = waktuSelesaiStr,
                        durasi = durasiMenit,
                        harga = totalHarga.toDouble()
                    )

                    val success = playStationRepository.simpanTransaksi(transaksi)

                    if (success) {
                        Log.d("Transaksi", "Transaksi berhasil disimpan ke Supabase")
                    } else {
                        Log.e("Transaksi", "Gagal menyimpan transaksi ke Supabase")
                        Toast.makeText(requireContext(), "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                    }
                }

                onDismiss()
            }
            .create()
            .show()
    }

}