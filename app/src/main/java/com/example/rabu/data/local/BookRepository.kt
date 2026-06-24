package com.example.rabu.data.local

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.rabu.data.model.Buku
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookRepository(context: Context) {

    private val sharedPref = context.getSharedPreferences("BookPrefs", Context.MODE_PRIVATE)
    private val prefManager = PrefManager(context)
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun loadBooks(): MutableList<Buku> = withContext(Dispatchers.IO) {
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
                        if (jsonObject.isNull("coverUri")) null else jsonObject.getString("coverUri"),
                        jsonObject.optDouble("rating", 0.0).toFloat(),
                        jsonObject.optString("notes", "")
                    )
                    listBuku.add(buku)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Jika lokal kosong, coba ambil dari Firestore (Cloud Backup)
            val cloudBooks = fetchBooksFromFirestore()
            if (cloudBooks.isNotEmpty()) {
                listBuku.addAll(cloudBooks)
                saveToLocal(listBuku)
            } else {
                val initialData = loadInitialData()
                saveBooks(initialData)
                listBuku.addAll(initialData)
            }
        }
        listBuku
    }

    suspend fun saveBooks(listBuku: List<Buku>) = withContext(Dispatchers.IO) {
        saveToLocal(listBuku)
        saveToFirestore(listBuku)
    }

    private fun saveToLocal(listBuku: List<Buku>) {
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
            jsonObject.put("rating", buku.rating)
            jsonObject.put("notes", buku.notes)
            jsonArray.put(jsonObject)
        }
        sharedPref.edit {
            putString("books_$currentUser", jsonArray.toString())
        }
    }

    private suspend fun saveToFirestore(listBuku: List<Buku>) {
        val currentUser = prefManager.getCurrentUser()
        if (currentUser == "Guest") return

        val data = hashMapOf("bookList" to listBuku)

        try {
            firestore.collection("users")
                .document(currentUser)
                .set(data)
                .await()
            Log.d("Firestore", "Data berhasil disinkronkan ke Cloud")
        } catch (e: Exception) {
            Log.e("Firestore", "Gagal sinkronisasi: ${e.message}")
        }
    }

    private suspend fun fetchBooksFromFirestore(): List<Buku> {
        val currentUser = prefManager.getCurrentUser()
        if (currentUser == "Guest") return emptyList()

        return try {
            val document = firestore.collection("users")
                .document(currentUser)
                .get()
                .await()

            val list = mutableListOf<Buku>()
            @Suppress("UNCHECKED_CAST")
            val data = document.get("bookList") as? List<Map<String, Any>>

            data?.forEach { map ->
                list.add(Buku(
                    map["judul"] as String,
                    map["author"] as String,
                    map["penerbit"] as String,
                    (map["jumlahHalaman"] as Long).toInt(),
                    map["genre"] as String,
                    map["deskripsi"] as String,
                    (map["progress"] as Long).toInt(),
                    map["status"] as String,
                    map["halamanTerakhir"] as String,
                    map["coverUri"] as? String,
                    (map["rating"] as? Double)?.toFloat() ?: 0f,
                    map["notes"] as? String ?: ""
                ))
            }
            list
        } catch (e: Exception) {
            Log.e("Firestore", "Gagal mengambil data: ${e.message}")
            emptyList()
        }
    }

    private fun loadInitialData(): List<Buku> {
        return listOf(
            Buku("Atomic Habits", "James Clear", "Penguin (2018)", 320, "Self-Help", "Buku tentang membangun kebiasaan kecil.", 0, "Belum dibaca", "0", null),
            Buku("Laut Bercerita", "Leila S. Chudori", "KPG (2017)", 379, "Historical Fiction", "Novel tentang perjuangan dan kehilangan.", 0, "Belum dibaca", "0", null),
            Buku("Rich Dad Poor Dad", "Robert Kiyosaki", "Warner Books (1997)", 336, "Finance", "Buku mengenai pengelolaan keuangan.", 0, "Belum dibaca", "0", null)
        )
    }
}
