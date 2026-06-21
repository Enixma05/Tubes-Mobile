package com.example.rabu.ui.book

import android.content.Intent
import android.net.Uri
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

        setSupportActionBar(findViewById(R.id.toolbarAddBook))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAddBook).setNavigationOnClickListener { finish() }

        ivBookCover = findViewById(R.id.ivAddBookCover)
        tvClickPrompt = findViewById(R.id.tvClickPrompt)

        val etTitle = findViewById<TextInputEditText>(R.id.etAddTitle)
        val etAuthor = findViewById<TextInputEditText>(R.id.etAddAuthor)
        val etPublisher = findViewById<TextInputEditText>(R.id.etAddPublisher)
        val etYear = findViewById<TextInputEditText>(R.id.etAddYear)
        val etGenre = findViewById<TextInputEditText>(R.id.etAddGenre)
        val etPages = findViewById<TextInputEditText>(R.id.etAddPages)
        val etSynopsis = findViewById<TextInputEditText>(R.id.etAddSynopsis)

        findViewById<CardView>(R.id.cvAddCover).setOnClickListener {
            val options = CropImageOptions().apply { guidelines = CropImageView.Guidelines.ON }
            cropImage.launch(CropImageContractOptions(null, options))
        }

        findViewById<Button>(R.id.btnSubmitAdd).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val author = etAuthor.text.toString().trim()
            val pagesText = etPages.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || pagesText.isEmpty()) {
                Toast.makeText(this, "Judul, Penulis, dan Halaman wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val year = etYear.text.toString().trim()
            val publisher = etPublisher.text.toString().trim()

            // Format Penerbit (Tahun) yang lebih rapi
            val formattedPublisher = when {
                publisher.isNotEmpty() && year.isNotEmpty() -> "$publisher ($year)"
                year.isNotEmpty() -> "($year)"
                else -> publisher.ifEmpty { "-" }
            }

            val newBook = Buku(
                judul = title,
                author = author,
                penerbit = formattedPublisher,
                jumlahHalaman = pagesText.toIntOrNull() ?: 0,
                genre = etGenre.text.toString().trim().ifEmpty { "-" },
                deskripsi = etSynopsis.text.toString().trim().ifEmpty { "Tidak ada deskripsi." },
                progress = 0,
                status = "Belum dibaca",
                halamanTerakhir = "0",
                coverUri = selectedImageUri?.toString()
            )

            setResult(RESULT_OK, Intent().apply { putExtra("NEW_BOOK", newBook) })
            Toast.makeText(this, "Buku berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}