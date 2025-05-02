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
     * Gets the current balance from the most recent entry in the database
     */
    suspend fun getCurrentBalance(): Double = withContext(Dispatchers.IO) {
        val list = client
            .from("laporan_keuangan")
            .select {
                order("tanggal", Order.DESCENDING)
                limit(1)
            }
            .decodeList<SaldoEntry>()

        list.firstOrNull()?.saldo ?: 0.0
    }

    /**
     * Fetches all financial records between [from] and [to] (inclusive),
     * groups them by date, and returns a list of FinancialEntry(date, income, outcome).
     */
    suspend fun getFinancialTrendData(
        from: String,
        to: String
    ): List<FinancialEntry> = withContext(Dispatchers.IO) {
        // 1. Query Supabase
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

        // 2. Group by parsed LocalDate and sum
        rows
            .groupBy { LocalDate.parse(it.tanggal) }
            .map { (date, group) ->
                val inc = group.filter { it.jenis == "Pemasukan" }.sumOf { it.jumlah }
                val out = group.filter { it.jenis == "Pengeluaran" }.sumOf { it.jumlah }
                FinancialEntry(date.toString(), inc, out)
            }
    }

    /** Total income between [from] and [to], inclusive. */
    suspend fun getIncomeInRange(from: String, to: String): Double = withContext(Dispatchers.IO) {
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

    /** Total expenses between [from] and [to], inclusive. */
    suspend fun getOutcomeInRange(from: String, to: String): Double = withContext(Dispatchers.IO) {
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

    /** Overall total income (ever). */
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

    /** Overall total expenses (ever). */
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

    /** Internal DTO for decoding each row of laporan_keuangan. */
    @Serializable
    private data class RawEntry(
        val tanggal: String,
        val jenis: String,
        val jumlah: Double
    )

    /** Internal DTO for decoding saldo entries. */
    @Serializable
    private data class SaldoEntry(
        val saldo: Double
    )
}