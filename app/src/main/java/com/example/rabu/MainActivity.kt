package com.example.rabu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.PopupMenu
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerBuku: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter
    private val listBuku = mutableListOf<Buku>()
    private val displayList = mutableListOf<Buku>()
    
    private var currentSearchQuery = ""
    private var currentSortMode = "AZ" // Default sort AZ

    private val addBookLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val newBook = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getSerializableExtra("NEW_BOOK", Buku::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getSerializableExtra("NEW_BOOK") as? Buku
                }
                newBook?.let {
                    listBuku.add(0, it)
                    filterAndSort()
                    updateStats()
                    saveBooks()
                }
            }
        }

    private val descriptionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val updatedBuku = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getSerializableExtra("UPDATED_BUKU", Buku::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getSerializableExtra("UPDATED_BUKU") as? Buku
                }

                val position = data?.getIntExtra("EXTRA_POSITION", -1) ?: -1

                if (updatedBuku != null && position != -1) {
                    listBuku[position] = updatedBuku
                    filterAndSort()
                    updateStats()
                    saveBooks()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerBuku = findViewById(R.id.recyclerBuku)
        val btnTambah = findViewById<Button>(R.id.btnTambah)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        val btnSort = findViewById<androidx.cardview.widget.CardView>(R.id.btnSort)

        bukuAdapter = BukuAdapter(
            displayList,
            onItemClick = { buku ->
                val intent = Intent(this, BookDescriptionActivity::class.java)
                intent.putExtra("EXTRA_BUKU", buku)
                intent.putExtra("EXTRA_POSITION", listBuku.indexOf(buku))
                descriptionLauncher.launch(intent)
            },
            onDeleteClick = { position ->
                val actualPosition = listBuku.indexOf(displayList[position])
                showDeleteDialog(actualPosition)
            }
        )

        recyclerBuku.layoutManager = LinearLayoutManager(this)
        recyclerBuku.adapter = bukuAdapter

        loadBooks()

        etSearch.doAfterTextChanged { text ->
            currentSearchQuery = text.toString().lowercase(Locale.getDefault())
            filterAndSort()
        }

        btnSort.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("A-Z")
            popup.menu.add("Z-A")
            popup.menu.add("Tahun Terbit")
            
            popup.setOnMenuItemClickListener { item ->
                currentSortMode = when (item.title) {
                    "A-Z" -> "AZ"
                    "Z-A" -> "ZA"
                    "Tahun Terbit" -> "YEAR"
                    else -> "AZ"
                }
                filterAndSort()
                true
            }
            popup.show()
        }

        btnTambah.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            addBookLauncher.launch(intent)
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        updateStats()
    }

    private fun loadInitialData() {
        val session = getSharedPreferences("UserSession", MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        val sharedPref = getSharedPreferences("BookPrefs", MODE_PRIVATE)
        
        // Data sampel awal untuk pengguna baru: Semuanya bernilai 0
        val initialBooks = listOf(
            Buku("Atomic Habits", "James Clear", "Penguin (2018)", 320, "Self-Help", "Buku tentang membangun kebiasaan kecil.", 0, "Belum dibaca", "0", null),
            Buku("Laut Bercerita", "Leila S. Chudori", "KPG (2017)", 379, "Historical Fiction", "Novel tentang perjuangan dan kehilangan.", 0, "Belum dibaca", "0", null),
            Buku("Rich Dad Poor Dad", "Robert Kiyosaki", "Warner Books (1997)", 336, "Finance", "Buku mengenai pengelolaan keuangan.", 0, "Belum dibaca", "0", null)
        )
        
        listBuku.clear()
        for (book in initialBooks) {
            // Ambil progres per user
            val savedProgress = sharedPref.getInt("${currentUser}_${book.judul}_progress", 0)
            
            // Ambil halaman terakhir per user
            val savedLastRead = sharedPref.getString("${currentUser}_${book.judul}_last_read", "0") ?: "0"
            
            // PAKSA STATUS SINKRON DENGAN PROGRES
            val finalStatus = when {
                savedProgress <= 0 -> "Belum dibaca"
                savedProgress >= 100 -> "Sudah dibaca"
                else -> "Sedang dibaca"
            }

            listBuku.add(
                book.copy(
                    status = finalStatus,
                    progress = savedProgress,
                    halamanTerakhir = savedLastRead
                )
            )
        }
    }

    private fun showDeleteDialog(position: Int) {
        val buku = listBuku[position]
        AlertDialog.Builder(this)
            .setTitle("Hapus Buku")
            .setMessage("Apakah Anda yakin ingin menghapus '${buku.judul}'?")
            .setPositiveButton("Hapus") { _, _ ->
                listBuku.removeAt(position)
                filterAndSort()
                updateStats()
                saveBooks()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStats() {
        findViewById<TextView>(R.id.tvTotalBuku).text = listBuku.size.toString()
        findViewById<TextView>(R.id.tvSedangBaca).text = listBuku.count { it.status == "Sedang dibaca" }.toString()
        findViewById<TextView>(R.id.tvSelesai).text = listBuku.count { it.status == "Sudah dibaca" }.toString()
    }

    private fun filterAndSort() {
        val filtered = if (currentSearchQuery.isEmpty()) {
            listBuku.toList()
        } else {
            listBuku.filter { 
                it.judul.lowercase(Locale.getDefault()).contains(currentSearchQuery) ||
                it.author.lowercase(Locale.getDefault()).contains(currentSearchQuery) ||
                it.genre.lowercase(Locale.getDefault()).contains(currentSearchQuery) ||
                it.penerbit.lowercase(Locale.getDefault()).contains(currentSearchQuery)
            }
        }

        val sorted = when (currentSortMode) {
            "AZ" -> filtered.sortedBy { it.judul.lowercase(Locale.getDefault()) }
            "ZA" -> filtered.sortedByDescending { it.judul.lowercase(Locale.getDefault()) }
            "YEAR" -> filtered.sortedByDescending { 
                // Ekstrak tahun dari string penerbit "Penerbit (Tahun)"
                val regex = "\\((\\d{4})\\)".toRegex()
                val match = regex.find(it.penerbit)
                match?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }
            else -> filtered
        }

        displayList.clear()
        displayList.addAll(sorted)
        bukuAdapter.notifyDataSetChanged()
    }

    private fun saveBooks() {
        val session = getSharedPreferences("UserSession", MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        val sharedPref = getSharedPreferences("BookPrefs", MODE_PRIVATE)
        
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

    private fun loadBooks() {
        val session = getSharedPreferences("UserSession", MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        val sharedPref = getSharedPreferences("BookPrefs", MODE_PRIVATE)
        
        val jsonString = sharedPref.getString("books_$currentUser", null)
        if (jsonString != null) {
            listBuku.clear()
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
            filterAndSort()
        } else {
            loadInitialData()
            saveBooks()
        }
    }
}
