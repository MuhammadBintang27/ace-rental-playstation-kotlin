package com.ace.playstation.repository

import com.ace.playstation.auth.SupabaseClientInstance
import com.ace.playstation.model.FinancialEntry
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

class FinancialRepository {
    private val client = SupabaseClientInstance.getClient()

    /**
     * Gets the current balance from the most recent entry in the database, ordered by created_at
     */
    suspend fun getCurrentBalance(): Double = withContext(Dispatchers.IO) {
        val list = client
            .from("laporan_keuangan")
            .select {
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<SaldoEntry>()

        if (list.isEmpty()) {
            0.0
        } else {
            list.first().saldo
        }
    }

    /**
     * Fetches all financial records between [from] and [to] (inclusive),
     * groups them by date, and returns a list of FinancialEntry(date, income, outcome).
     */
    suspend fun getFinancialTrendData(
        from: String,
        to: String
    ): List<FinancialEntry> = withContext(Dispatchers.IO) {
        val rows = client
            .from("laporan_keuangan")
            .select {
                filter {
                    gte("tanggal", from)
                    lte("tanggal", to)
                }
                order("tanggal", Order.ASCENDING)
            }
            .decodeList<RawEntry>()

        rows
            .groupBy { LocalDate.parse(it.tanggal) }
            .map { (date, group) ->
                val inc = group.filter { it.jenis == "Pemasukan" }.sumOf { it.jumlah }
                val out = group.filter { it.jenis == "Pengeluaran" }.sumOf { it.jumlah }
                FinancialEntry(date.toString(), inc, out)
            }
    }

    /** Total income for a specific month and year */
    suspend fun getMonthlyIncome(year: Int, month: Int): Double = withContext(Dispatchers.IO) {
        val from = String.format("%02d/01/%04d", month, year) // e.g., 04/01/2025
        val to = String.format("%02d/%02d/%04d", month, getLastDayOfMonth(year, month), year) // e.g., 04/30/2025
        client
            .from("laporan_keuangan")
            .select {
                filter {
                    eq("jenis", "Pemasukan")
                    gte("tanggal", from)
                    lte("tanggal", to)
                }
            }
            .decodeList<RawEntry>()
            .sumOf { it.jumlah }
    }

    /** Total expenses for a specific month and year */
    suspend fun getMonthlyOutcome(year: Int, month: Int): Double = withContext(Dispatchers.IO) {
        val from = String.format("%02d/01/%04d", month, year) // e.g., 04/01/2025
        val to = String.format("%02d/%02d/%04d", month, getLastDayOfMonth(year, month), year) // e.g., 04/30/2025
        client
            .from("laporan_keuangan")
            .select {
                filter {
                    eq("jenis", "Pengeluaran")
                    gte("tanggal", from)
                    lte("tanggal", to)
                }
            }
            .decodeList<RawEntry>()
            .sumOf { it.jumlah }
    }

    /** Overall total income (ever) */
    suspend fun getTotalIncome(): Double = withContext(Dispatchers.IO) {
        client
            .from("laporan_keuangan")
            .select {
                filter {
                    eq("jenis", "Pemasukan")
                }
            }
            .decodeList<RawEntry>()
            .sumOf { it.jumlah }
    }

    /** Overall total expenses (ever) */
    suspend fun getTotalOutcome(): Double = withContext(Dispatchers.IO) {
        client
            .from("laporan_keuangan")
            .select {
                filter {
                    eq("jenis", "Pengeluaran")
                }
            }
            .decodeList<RawEntry>()
            .sumOf { it.jumlah }
    }

    /** Helper to get the last day of a month */
    private fun getLastDayOfMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    /** Internal DTO for decoding each row of laporan_keuangan */
    @Serializable
    private data class RawEntry(
        val tanggal: String,
        val jenis: String,
        val jumlah: Double
    )

    /** Internal DTO for decoding saldo entries */
    @Serializable
    private data class SaldoEntry(
        val saldo: Double
    )
}