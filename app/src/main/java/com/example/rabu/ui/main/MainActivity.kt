package com.example.rabu.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.*
import com.example.rabu.R
import com.example.rabu.data.local.BookRepository
import com.example.rabu.data.model.Buku
import com.example.rabu.ui.adapter.BukuAdapter
import com.example.rabu.ui.book.AddBookActivity
import com.example.rabu.ui.book.BookDescriptionActivity
import com.example.rabu.ui.settings.SettingsActivity
import com.example.rabu.utils.DataHelper
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerBuku: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter
    private lateinit var repository: BookRepository

    private val listBuku = mutableListOf<Buku>()
    private val displayList = mutableListOf<Buku>()

    private var currentSearchQuery = ""
    private var currentSortMode = "AZ"

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
                    repository.saveBooks(listBuku)
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
                    repository.saveBooks(listBuku)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Repository
        repository = BookRepository(this)

        recyclerBuku = findViewById(R.id.recyclerBuku)
        val btnTambah = findViewById<Button>(R.id.btnTambah)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnSort = findViewById<CardView>(R.id.btnSort)

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

        // Load data dari repository
        listBuku.clear()
        listBuku.addAll(repository.loadBooks())
        filterAndSort()

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

    private fun showDeleteDialog(position: Int) {
        val buku = listBuku[position]
        AlertDialog.Builder(this)
            .setTitle("Hapus Buku")
            .setMessage("Apakah Anda yakin ingin menghapus '${buku.judul}'?")
            .setPositiveButton("Hapus") { _, _ ->
                listBuku.removeAt(position)
                filterAndSort()
                updateStats()
                repository.saveBooks(listBuku)
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
                // Menggunakan DataHelper untuk mengekstrak tahun
                DataHelper.extractYear(it.penerbit)
            }
            else -> filtered
        }

        displayList.clear()
        displayList.addAll(sorted)
        bukuAdapter.notifyDataSetChanged()
    }
}