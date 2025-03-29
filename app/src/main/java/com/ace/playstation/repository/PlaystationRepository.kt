package com.ace.playstation.data.repository

import PlayStation
import android.util.Log
import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.data.model.TransaksiRental
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class PlayStationRepository {

    // Konversi timestamp Unix ke format ISO8601
    private fun convertToISO8601(timestamp: String?): String {
        return try {
            timestamp?.toLongOrNull()?.let { seconds ->
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.format(Date(seconds * 1000))
            } ?: ""
        } catch (e: Exception) {
            Log.e("Supabase", "Error converting timestamp: ${e.message}")
            ""
        }
    }

    // Ambil semua data PlayStation dari Supabase
    suspend fun getAllPlaystations(): List<PlayStation> {
        return try {
            val response = SupabaseClientInstance.getClient()
                .from("playstation_unit")
                .select()

            if (response.data == null) {
                Log.e("Supabase", "Response data is NULL: ${response.headers}")
                return emptyList()
            }

            Log.d("Supabase", "Raw response: ${response.data}")

            val jsonArray = JSONArray(response.data.toString())
            val playstationList = mutableListOf<PlayStation>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val unitId = jsonObject.optInt("unit_id", -1)
                val nomorUnit = jsonObject.optString("nomor_unit", "")
                val status = jsonObject.optString("status", "")
                val tipeMain = jsonObject.optString("tipe_main", "")

                val waktuMulai = jsonObject.optString("waktu_mulai")?.takeIf { it.isNotEmpty() }?.let {
                    parseTimestamp(it)
                }
                val waktuSelesai = jsonObject.optString("waktu_selesai")?.takeIf { it.isNotEmpty() }?.let {
                    parseTimestamp(it)
                }

                if (unitId != -1) {
                    playstationList.add(PlayStation(unitId, nomorUnit, status, tipeMain, waktuMulai, waktuSelesai))
                }
            }

            return playstationList

        } catch (e: Exception) {
            Log.e("Supabase", "Error parsing JSON: ${e.message}")
            emptyList()
        }
    }

    // Parsing timestamp ke format yang sesuai
    private fun parseTimestamp(timestamp: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())

            val date = inputFormat.parse(timestamp)
            outputFormat.format(date ?: return null)
        } catch (e: Exception) {
            Log.e("Supabase", "Error parsing timestamp: $timestamp - ${e.message}")
            null
        }
    }

    // Ambil PlayStation berdasarkan unit_id
    suspend fun getPlaystationById(id: Int): PlayStation? {
        return try {
            val response = SupabaseClientInstance.getClient()
                .from("playstation_unit")
                .select {
                    filter { eq("unit_id", id) }
                }

            if (response.data == null || response.data.toString() == "[]") {
                Log.e("Supabase", "PlayStation dengan ID $id tidak ditemukan")
                return null
            }

            Log.d("Supabase", "Raw response (by ID): ${response.data}")

            val jsonArray = JSONArray(response.data.toString())
            if (jsonArray.length() == 0) return null

            val jsonObject = jsonArray.getJSONObject(0)

            val unitId = jsonObject.optInt("unit_id", -1)
            val nomorUnit = jsonObject.optString("nomor_unit", "")
            val status = jsonObject.optString("status", "")
            val tipeMain = jsonObject.optString("tipe_main", "")

            val waktuMulai = jsonObject.optString("waktu_mulai")?.takeIf { it.isNotEmpty() }?.let {
                parseTimestamp(it)
            }
            val waktuSelesai = jsonObject.optString("waktu_selesai")?.takeIf { it.isNotEmpty() }?.let {
                parseTimestamp(it)
            }

            return PlayStation(unitId, nomorUnit, status, tipeMain, waktuMulai, waktuSelesai)

        } catch (e: Exception) {
            Log.e("Supabase", "Error fetching PlayStation by ID: ${e.message}")
            null
        }
    }

    // Update data PlayStation berdasarkan unit_id
    suspend fun updatePlayStation(
        unitId: Int,
        status: String,
        tipeMain: String,
        waktuMulai: String?,
        waktuSelesai: String?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updateData = buildJsonObject {
                    put("status", status)
                    put("tipe_main", tipeMain)
                    waktuMulai?.let { put("waktu_mulai", it) }
                    waktuSelesai?.let { put("waktu_selesai", it) }
                }

                val response = SupabaseClientInstance.getClient()
                    .from("playstation_unit")
                    .update(updateData) {
                        filter { eq("unit_id", unitId) }
                    }

                Log.d("Supabase", "Update Response: ${response.data}")
                return@withContext response.data != null
            } catch (e: Exception) {
                Log.e("Supabase", "Error updating PlayStation: ${e.message}")
                return@withContext false
            }
        }
    }

    suspend fun simpanTransaksi(transaksi: TransaksiRental): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClientInstance.getClient()
                    .from("transaksi_rental")
                    .insert(transaksi)

                Log.d("Supabase", "Transaksi berhasil disimpan")
                true
            } catch (e: Exception) {
                Log.e("Supabase", "Error saat menyimpan transaksi: ${e.message}")
                false
            }
        }
    }

}
