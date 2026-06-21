package com.example.rabu.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataHelper {

    /**
     * Memformat tanggal saat ini ke format Indonesia
     * Contoh: 21 Juni 2024
     */
    fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }

    /**
     * Mengekstrak tahun dari string penerbit format "Penerbit (Tahun)"
     */
    fun extractYear(penerbit: String): Int {
        val regex = "\\((\\d{4})\\)".toRegex()
        val match = regex.find(penerbit)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * Validasi input halaman agar tidak melebihi total halaman dan tidak negatif
     */
    fun cleanPageInput(input: String, totalPage: Int): String {
        val number = input.filter { it.isDigit() }.toIntOrNull() ?: 0
        return when {
            number <= 0 -> "0"
            number > totalPage -> totalPage.toString()
            else -> number.toString()
        }
    }

    /**
     * Menghitung persentase progres membaca
     */
    fun calculateProgress(currentPage: Int, totalPage: Int): Int {
        if (totalPage <= 0) return 0
        return ((currentPage.toFloat() / totalPage) * 100).toInt().coerceIn(0, 100)
    }
}