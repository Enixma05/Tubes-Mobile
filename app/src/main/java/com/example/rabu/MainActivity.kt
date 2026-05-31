package com.example.rabu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerBuku: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter
    private val listBuku = mutableListOf<Buku>()

    private val addBookLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val newBook = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    data?.getSerializableExtra("NEW_BOOK", Buku::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getSerializableExtra("NEW_BOOK") as? Buku
                }
                newBook?.let {
                    listBuku.add(0, it)
                    bukuAdapter.notifyItemInserted(0)
                    recyclerBuku.scrollToPosition(0)
                    updateStats()
                }
            }
        }

    private val descriptionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedBuku = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    data?.getSerializableExtra("UPDATED_BUKU", Buku::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getSerializableExtra("UPDATED_BUKU") as? Buku
                }
                val position = data?.getIntExtra("EXTRA_POSITION", -1) ?: -1
                if (updatedBuku != null && position != -1) {
                    listBuku[position] = updatedBuku
                    bukuAdapter.notifyItemChanged(position)
                    updateStats()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerBuku = findViewById(R.id.recyclerBuku)
        val btnTambah = findViewById<Button>(R.id.btnTambah)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        loadInitialData()

        bukuAdapter = BukuAdapter(
            listBuku,
            onItemClick = { buku ->
                val intent = Intent(this, BookDescriptionActivity::class.java)
                intent.putExtra("EXTRA_BUKU", buku)
                intent.putExtra("EXTRA_POSITION", listBuku.indexOf(buku))
                descriptionLauncher.launch(intent)
            },
            onDeleteClick = { position ->
                showDeleteDialog(position)
            }
        )

        recyclerBuku.layoutManager = LinearLayoutManager(this)
        recyclerBuku.adapter = bukuAdapter

        btnTambah.setOnClickListener {
            val intent = Intent(this, AddBookActivity::class.java)
            addBookLauncher.launch(intent)
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        updateStats()
        setupGreeting()
    }

    private fun loadInitialData() {
        val sharedPref = getSharedPreferences("BookPrefs", Context.MODE_PRIVATE)
        val initialBooks = listOf(
            Buku("Atomic Habits", "James Clear", "Penguin (2018)", 320, "Self-Help", "Buku tentang membangun kebiasaan kecil.", 70, "Sedang dibaca", "Hal. 120", null),
            Buku("Laut Bercerita", "Leila S. Chudori", "KPG (2017)", 379, "Historical Fiction", "Novel tentang perjuangan dan kehilangan.", 45, "Sedang dibaca", "Hal. 75", null),
            Buku("Rich Dad Poor Dad", "Robert Kiyosaki", "Warner Books (1997)", 336, "Finance", "Buku mengenai pengelolaan keuangan.", 90, "Sudah dibaca", "Selesai", null)
        )
        listBuku.clear()
        for (book in initialBooks) {
            val savedStatus = sharedPref.getString("${book.judul}_status", book.status)
            listBuku.add(book.copy(status = savedStatus ?: book.status))
        }
    }

    private fun showDeleteDialog(position: Int) {
        val buku = listBuku[position]
        AlertDialog.Builder(this)
            .setTitle("Hapus Buku")
            .setMessage("Apakah Anda yakin ingin menghapus '${buku.judul}'?")
            .setPositiveButton("Hapus") { _, _ ->
                listBuku.removeAt(position)
                bukuAdapter.notifyItemRemoved(position)
                updateStats()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStats() {
        findViewById<TextView>(R.id.tvTotalBuku).text = listBuku.size.toString()
        findViewById<TextView>(R.id.tvSedangBaca).text = listBuku.count { it.status == "Sedang dibaca" }.toString()
        findViewById<TextView>(R.id.tvSelesai).text = listBuku.count { it.status == "Sudah dibaca" }.toString()
    }

    private fun setupGreeting() {
        // Logika salam dihapus dari UI Homepage sesuai instruksi, tapi fungsi ini bisa tetap ada jika diperlukan di masa depan.
    }
}
