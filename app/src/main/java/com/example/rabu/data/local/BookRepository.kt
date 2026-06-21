package com.example.rabu.data.local

import android.content.Context
import com.example.rabu.data.model.Buku
import org.json.JSONArray
import org.json.JSONObject

class BookRepository(context: Context) {

    private val sharedPref = context.getSharedPreferences("BookPrefs", Context.MODE_PRIVATE)
    private val prefManager = PrefManager(context)

    fun loadBooks(): MutableList<Buku> {
        val currentUser = prefManager.getCurrentUser()
        val jsonString = sharedPref.getString("books_$currentUser", null)
        val listBuku = mutableListOf<Buku>()

        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val buku = Buku(
                        jsonObject.getString("judul"),
                        jsonObject.getString("author"),
                        jsonObject.getString("penerbit"),
                        jsonObject.getInt("jumlahHalaman"),
                        jsonObject.getString("genre"),
                        jsonObject.getString("deskripsi"),
                        jsonObject.getInt("progress"),
                        jsonObject.getString("status"),
                        jsonObject.getString("halamanTerakhir"),
                        if (jsonObject.isNull("coverUri")) null else jsonObject.getString("coverUri")
                    )
                    listBuku.add(buku)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val initialData = loadInitialData()
            saveBooks(initialData)
            listBuku.addAll(initialData)
        }
        return listBuku
    }

    fun saveBooks(listBuku: List<Buku>) {
        val currentUser = prefManager.getCurrentUser()
        val jsonArray = JSONArray()
        for (buku in listBuku) {
            val jsonObject = JSONObject()
            jsonObject.put("judul", buku.judul)
            jsonObject.put("author", buku.author)
            jsonObject.put("penerbit", buku.penerbit)
            jsonObject.put("jumlahHalaman", buku.jumlahHalaman)
            jsonObject.put("genre", buku.genre)
            jsonObject.put("deskripsi", buku.deskripsi)
            jsonObject.put("progress", buku.progress)
            jsonObject.put("status", buku.status)
            jsonObject.put("halamanTerakhir", buku.halamanTerakhir)
            jsonObject.put("coverUri", buku.coverUri)
            jsonArray.put(jsonObject)
        }
        sharedPref.edit().putString("books_$currentUser", jsonArray.toString()).apply()
    }

    private fun loadInitialData(): List<Buku> {
        val currentUser = prefManager.getCurrentUser()
        val initialBooks = listOf(
            Buku("Atomic Habits", "James Clear", "Penguin (2018)", 320, "Self-Help", "Buku tentang membangun kebiasaan kecil.", 0, "Belum dibaca", "0", null),
            Buku("Laut Bercerita", "Leila S. Chudori", "KPG (2017)", 379, "Historical Fiction", "Novel tentang perjuangan dan kehilangan.", 0, "Belum dibaca", "0", null),
            Buku("Rich Dad Poor Dad", "Robert Kiyosaki", "Warner Books (1997)", 336, "Finance", "Buku mengenai pengelolaan keuangan.", 0, "Belum dibaca", "0", null)
        )

        return initialBooks.map { book ->
            val savedProgress = sharedPref.getInt("${currentUser}_${book.judul}_progress", 0)
            val savedLastRead = sharedPref.getString("${currentUser}_${book.judul}_last_read", "0") ?: "0"
            val finalStatus = when {
                savedProgress <= 0 -> "Belum dibaca"
                savedProgress >= 100 -> "Sudah dibaca"
                else -> "Sedang dibaca"
            }
            book.copy(status = finalStatus, progress = savedProgress, halamanTerakhir = savedLastRead)
        }
    }
}