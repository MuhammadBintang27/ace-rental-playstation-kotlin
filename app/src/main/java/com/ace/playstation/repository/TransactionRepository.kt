package com.ace.playstation.repository

import android.util.Log
import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.model.Produk
import com.ace.playstation.model.TransactionItem
import com.ace.playstation.model.TransaksiPenjualan
import com.ace.playstation.model.TransaksiRental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TransactionRepository {
    // Define category constants
    companion object {
        const val CATEGORY_ALL = "ALL"
        const val CATEGORY_RENTAL = "RENTAL"
        const val CATEGORY_MAKANAN = "MAKANAN"
        const val CATEGORY_MINUMAN = "MINUMAN"
    }

    suspend fun getAllTransactions(): Result<List<TransactionItem>> = withContext(Dispatchers.IO) {
        try {
            val transactionItems = mutableListOf<TransactionItem>()

            // Fetch all necessary data
            val produkList = fetchProduk()
            val rentalList = fetchRental()
            val penjualanList = fetchPenjualan()

            // Map rental transactions
            rentalList.forEach { rental ->
                Log.d("TransactionRepo", "Processing rental: ${rental.waktu_mulai} - ${rental.waktu_selesai}")
                transactionItems.add(
                    TransactionItem(
                        id = rental.unit_id,
                        type = "RENTAL",
                        category = rental.kategori_main,
                        name = "PlayStation Unit #${rental.unit_id}",
                        details = formatRentalDetails(rental),
                        amount = rental.harga,
                        datetime = formatDateTime(rental.waktu_mulai)
                    )
                )
            }

            // Map penjualan transactions with produk info
            penjualanList.forEach { penjualan ->
                val produk = produkList.find { it.produk_id == penjualan.produk_id }
                produk?.let {
                    Log.d("TransactionRepo", "Processing penjualan: ${penjualan.created_at}, Category: ${produk.kategori}")
                    transactionItems.add(
                        TransactionItem(
                            id = penjualan.penjualan_id,
                            type = "PENJUALAN",
                            category = produk.kategori,
                            name = produk.nama_produk,
                            details = "Jumlah: ${penjualan.jumlah}",
                            amount = penjualan.total_harga,
                            datetime = formatDateTime(penjualan.created_at.toString())
                        )
                    )
                }
            }

            // Sort transactions by datetime (newest first)
            val sortedItems = transactionItems.sortedByDescending {
                try {
                    val inputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    inputFormat.parse(it.datetime) ?: Date(0)
                } catch (e: Exception) {
                    Log.e("TransactionRepo", "Error parsing date for sorting: ${it.datetime}", e)
                    Date(0)
                }
            }

            Result.success(sortedItems)
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error in getAllTransactions", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchProduk(): List<Produk> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClientInstance.getClient().from("produk").select { Columns.ALL}.decodeList<Produk>()
            response
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching produk", e)
            emptyList()
        }
    }

    private suspend fun fetchRental(): List<TransaksiRental> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClientInstance.getClient().from("transaksi_rental").select { Columns.ALL }.decodeList<TransaksiRental>()
            response
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching rental", e)
            emptyList()
        }
    }

    private suspend fun fetchPenjualan(): List<TransaksiPenjualan> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClientInstance.getClient().from("transaksi_penjualan").select { Columns.ALL }.decodeList<TransaksiPenjualan>()
            response
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error fetching penjualan", e)
            emptyList()
        }
    }

    private fun formatRentalDetails(rental: TransaksiRental): String {
        try {
            // Handle ISO-8601 format with T separator (2025-03-28T15:09:55)
            val startTimeStr = extractTimeFromISO(rental.waktu_mulai)
            val endTimeStr = extractTimeFromISO(rental.waktu_selesai)

            // Calculate duration in hours
            val durasiJam = if (rental.durasi > 0) {
                rental.durasi / 60
            } else {
                // Try to calculate from start and end times
                val durationHours = calculateDurationHours(rental.waktu_mulai, rental.waktu_selesai)
                if (durationHours > 0) durationHours else 0
            }

            return "$startTimeStr - $endTimeStr, Durasi: $durasiJam jam"
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error formatting rental details", e)
            return "N/A - N/A, Durasi: 0 jam"
        }
    }

    // Helper to extract time from ISO format (2025-03-28T15:09:55)
    private fun extractTimeFromISO(isoDatetime: String): String {
        return try {
            if (isoDatetime.contains("T")) {
                val parts = isoDatetime.split("T")
                if (parts.size == 2) {
                    val timePart = parts[1]
                    timePart.substring(0, minOf(5, timePart.length)) // Extract HH:MM
                } else {
                    "N/A"
                }
            } else if (isoDatetime.contains(" ")) {
                val parts = isoDatetime.split(" ")
                if (parts.size == 2) {
                    val timePart = parts[1]
                    timePart.substring(0, minOf(5, timePart.length)) // Extract HH:MM
                } else {
                    "N/A"
                }
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error extracting time", e)
            "N/A"
        }
    }

    // Calculate duration hours between two ISO timestamps
    private fun calculateDurationHours(start: String, end: String): Long {
        try {
            val parseFormat = if (start.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }

            val startDate = parseFormat.parse(start)
            val endDate = parseFormat.parse(end)

            if (startDate != null && endDate != null) {
                val diffMs = endDate.time - startDate.time
                return diffMs / (1000 * 60 * 60)
            }
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error calculating duration", e)
        }
        return 0
    }

    // Format datetime to "02 Mar 2023 13:21" format
    private fun formatDateTime(dateString: String): String {
        try {
            if (dateString.isBlank()) return ""

            // Handle both ISO-8601 format with T separator (2025-03-28T15:09:55)
            // and standard format with space separator (2025-03-30 10:23:51)
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

            val date = inputFormat.parse(dateString.trim())
            if (date != null) {
                val formatted = outputFormat.format(date)
                Log.d("TransactionRepo", "Formatted date: $dateString -> $formatted")
                return formatted
            }
            return dateString
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error formatting date: $dateString", e)
            return dateString
        }
    }

    // Combined filtering method for both time and category
    fun filterTransactions(
        transactions: List<TransactionItem>,
        timeFilter: String,
        fromDate: String = "",
        toDate: String = "",
        categoryFilter: String = CATEGORY_ALL
    ): List<TransactionItem> {

        Log.d("TransactionRepo", "Filtering: timeFilter=$timeFilter, fromDate=$fromDate, toDate=$toDate, categoryFilter=$categoryFilter")

        // First apply time filter
        val timeFiltered = when (timeFilter) {
            "today" -> filterByToday(transactions)
            "week" -> filterByWeek(transactions)
            "month" -> filterByMonth(transactions)
            "year" -> filterByYear(transactions)
            "custom" -> if (fromDate.isNotEmpty() && toDate.isNotEmpty()) {
                filterByDateRange(transactions, fromDate, toDate)
            } else {
                transactions
            }
            else -> transactions
        }

        // Then apply category filter
        return if (categoryFilter == CATEGORY_ALL) {
            timeFiltered
        } else {
            filterByCategory(timeFiltered, categoryFilter)
        }
    }

    // Filter transactions by category
    fun filterByCategory(transactions: List<TransactionItem>, category: String): List<TransactionItem> {
        Log.d("TransactionRepo", "Filtering by category: $category")

        return when (category) {
            CATEGORY_RENTAL -> transactions.filter { it.type == "RENTAL" }
            CATEGORY_MAKANAN -> transactions.filter { it.category.equals("MAKANAN", ignoreCase = true) }
            CATEGORY_MINUMAN -> transactions.filter { it.category.equals("MINUMAN", ignoreCase = true) }
            else -> transactions
        }
    }

    // Time filter methods
    fun filterByDateRange(transactions: List<TransactionItem>, fromDate: String, toDate: String): List<TransactionItem> {
        try {
            Log.d("TransactionRepo", "Filtering date range: $fromDate to $toDate")

            // Convert UI format dates to comparable Date objects
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val fromDateObj = dateFormat.parse(fromDate)
            val toDateObj = dateFormat.parse(toDate)

            if (fromDateObj == null || toDateObj == null) {
                Log.e("TransactionRepo", "Could not parse date range: $fromDate - $toDate")
                return transactions
            }

            // Add one day to toDate to make it inclusive
            val calendar = Calendar.getInstance()
            calendar.time = toDateObj
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val adjustedToDate = calendar.time

            return transactions.filter { transaction ->
                try {
                    val parts = transaction.datetime.split(" ")
                    if (parts.size >= 3) {
                        val dateStr = "${parts[0]} ${parts[1]} ${parts[2]}"
                        val transDate = dateFormat.parse(dateStr)

                        val result = transDate != null &&
                                !transDate.before(fromDateObj) &&
                                transDate.before(adjustedToDate)

                        Log.d("TransactionRepo", "Transaction date: $dateStr, include: $result")
                        result
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    Log.e("TransactionRepo", "Error parsing transaction date: ${transaction.datetime}", e)
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error in filterByDateRange", e)
            return transactions
        }
    }

    fun filterByToday(transactions: List<TransactionItem>): List<TransactionItem> {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        Log.d("TransactionRepo", "Filtering by today: $today")

        return transactions.filter { transaction ->
            val datePrefix = transaction.datetime.split(" ").take(3).joinToString(" ")
            val result = datePrefix == today
            Log.d("TransactionRepo", "Transaction date: $datePrefix, matches today: $result")
            result
        }
    }

    fun filterByWeek(transactions: List<TransactionItem>): List<TransactionItem> {
        val calendar = Calendar.getInstance()
        // Set to first day of current week
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val weekStartDate = calendar.time
        val weekStartStr = dateFormat.format(weekStartDate)

        Log.d("TransactionRepo", "Filtering by week starting: $weekStartStr")

        return transactions.filter { transaction ->
            try {
                val parts = transaction.datetime.split(" ")
                if (parts.size >= 3) {
                    val dateStr = "${parts[0]} ${parts[1]} ${parts[2]}"
                    val transDate = dateFormat.parse(dateStr)

                    val result = transDate != null && !transDate.before(weekStartDate)
                    Log.d("TransactionRepo", "Transaction date: $dateStr, in current week: $result")
                    result
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("TransactionRepo", "Error in filterByWeek for date: ${transaction.datetime}", e)
                false
            }
        }
    }

    fun filterByMonth(transactions: List<TransactionItem>): List<TransactionItem> {
        val currentMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(Date())
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

        Log.d("TransactionRepo", "Filtering by month: $currentMonth $currentYear")

        return transactions.filter { transaction ->
            val parts = transaction.datetime.split(" ")
            val result = parts.size >= 3 && parts[1] == currentMonth && parts[2] == currentYear

            Log.d("TransactionRepo", "Transaction date: ${transaction.datetime}, in current month: $result")
            result
        }
    }

    fun filterByYear(transactions: List<TransactionItem>): List<TransactionItem> {
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

        Log.d("TransactionRepo", "Filtering by year: $currentYear")

        return transactions.filter { transaction ->
            val parts = transaction.datetime.split(" ")
            val result = parts.size >= 3 && parts[2] == currentYear

            Log.d("TransactionRepo", "Transaction date: ${transaction.datetime}, in current year: $result")
            result
        }
    }
}