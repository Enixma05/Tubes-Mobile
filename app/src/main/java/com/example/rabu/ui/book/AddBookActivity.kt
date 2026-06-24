package com.example.rabu.ui.book

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.rabu.R
import com.example.rabu.data.model.Buku
import com.google.android.material.textfield.TextInputEditText

class AddBookActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivBookCover: ImageView
    private lateinit var tvClickPrompt: TextView
    private var bookToEdit: Buku? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            selectedImageUri = result.uriContent
            ivBookCover.setImageURI(selectedImageUri)
            ivBookCover.setPadding(0, 0, 0, 0)
            tvClickPrompt.text = "Ubah Cover"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAddBook)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ivBookCover = findViewById(R.id.ivAddBookCover)
        tvClickPrompt = findViewById(R.id.tvClickPrompt)

        val etTitle = findViewById<TextInputEditText>(R.id.etAddTitle)
        val etAuthor = findViewById<TextInputEditText>(R.id.etAddAuthor)
        val etPublisher = findViewById<TextInputEditText>(R.id.etAddPublisher)
        val etYear = findViewById<TextInputEditText>(R.id.etAddYear)
        val etGenre = findViewById<TextInputEditText>(R.id.etAddGenre)
        val etPages = findViewById<TextInputEditText>(R.id.etAddPages)
        val etAddSynopsis = findViewById<TextInputEditText>(R.id.etAddSynopsis)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAdd)

        // Cek apakah mode Edit
        bookToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_BUKU", Buku::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_BUKU") as? Buku
        }

        bookToEdit?.let { buku ->
            toolbar.title = "Edit Informasi Buku"
            etTitle.setText(buku.judul)
            etAuthor.setText(buku.author)
            etGenre.setText(buku.genre)
            etPages.setText(buku.jumlahHalaman.toString())
            etAddSynopsis.setText(buku.deskripsi)
            
            // Parsing Penerbit & Tahun jika memungkinkan
            if (buku.penerbit.contains(" (") && buku.penerbit.endsWith(")")) {
                val parts = buku.penerbit.split(" (")
                etPublisher.setText(parts[0])
                etYear.setText(parts[1].replace(")", ""))
            } else {
                etPublisher.setText(buku.penerbit)
            }

            if (!buku.coverUri.isNullOrEmpty()) {
                selectedImageUri = Uri.parse(buku.coverUri)
                ivBookCover.setImageURI(selectedImageUri)
                ivBookCover.setPadding(0, 0, 0, 0)
                tvClickPrompt.text = "Ubah Cover"
            }
            btnSubmit.text = "Simpan Perubahan"
        }

        findViewById<CardView>(R.id.cvAddCover).setOnClickListener {
            val options = CropImageOptions().apply { guidelines = CropImageView.Guidelines.ON }
            cropImage.launch(CropImageContractOptions(null, options))
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val author = etAuthor.text.toString().trim()
            val pagesText = etPages.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || pagesText.isEmpty()) {
                Toast.makeText(this, "Judul, Penulis, dan Halaman wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val year = etYear.text.toString().trim()
            val publisher = etPublisher.text.toString().trim()

            val formattedPublisher = when {
                publisher.isNotEmpty() && year.isNotEmpty() -> "$publisher ($year)"
                year.isNotEmpty() -> "($year)"
                else -> publisher.ifEmpty { "-" }
            }

            val updatedBook = bookToEdit?.copy(
                judul = title,
                author = author,
                penerbit = formattedPublisher,
                jumlahHalaman = pagesText.toIntOrNull() ?: 0,
                genre = etGenre.text.toString().trim().ifEmpty { "-" },
                deskripsi = etAddSynopsis.text.toString().trim().ifEmpty { "Tidak ada deskripsi." },
                coverUri = selectedImageUri?.toString()
            ) ?: Buku(
                judul = title,
                author = author,
                penerbit = formattedPublisher,
                jumlahHalaman = pagesText.toIntOrNull() ?: 0,
                genre = etGenre.text.toString().trim().ifEmpty { "-" },
                deskripsi = etAddSynopsis.text.toString().trim().ifEmpty { "Tidak ada deskripsi." },
                progress = 0,
                status = "Belum dibaca",
                halamanTerakhir = "0",
                coverUri = selectedImageUri?.toString()
            )

            val resultIntent = Intent().apply {
                putExtra("NEW_BOOK", updatedBook)
            }
            setResult(RESULT_OK, resultIntent)
            Toast.makeText(this, if (bookToEdit != null) "Perubahan disimpan!" else "Buku ditambahkan!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
