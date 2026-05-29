package com.example.rabu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.textfield.TextInputEditText

class AddBookActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivBookCover: ImageView
    private lateinit var tvClickPrompt: TextView

    // Launcher untuk proses Crop (Ambil gambar -> Crop -> Hasil)
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Jika berhasil, tampilkan hasil crop di ImageView
            selectedImageUri = result.uriContent
            ivBookCover.setImageURI(selectedImageUri)
            ivBookCover.setPadding(0, 0, 0, 0) // Hilangkan padding icon default
            tvClickPrompt.text = "Ubah Cover"
        } else {
            val error = result.error
            if (error != null) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbarAddBook)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Inisialisasi View
        ivBookCover = findViewById(R.id.ivAddBookCover)
        tvClickPrompt = findViewById(R.id.tvClickPrompt)
        val cvAddCover = findViewById<CardView>(R.id.cvAddCover)
        val etTitle = findViewById<TextInputEditText>(R.id.etAddTitle)
        val etAuthor = findViewById<TextInputEditText>(R.id.etAddAuthor)
        val etPublisher = findViewById<TextInputEditText>(R.id.etAddPublisher)
        val etYear = findViewById<TextInputEditText>(R.id.etAddYear)
        val etGenre = findViewById<TextInputEditText>(R.id.etAddGenre)
        val etPages = findViewById<TextInputEditText>(R.id.etAddPages)
        val etSynopsis = findViewById<TextInputEditText>(R.id.etAddSynopsis)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAdd)

        // Ketika cover diklik, mulai proses pilih dan potong
        cvAddCover.setOnClickListener {
            startCropProcess()
        }

        // Simpan Data Buku
        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val author = etAuthor.text.toString().trim()
            val publisher = etPublisher.text.toString().trim()
            val year = etYear.text.toString().trim()
            val pages = etPages.text.toString().trim()
            val genre = etGenre.text.toString().trim()
            val synopsis = etSynopsis.text.toString().trim()


            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(this, "Judul dan Author wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                val newBook = Buku(
                    judul = title,
                    author = author,
                    penerbit = "$publisher ($year)",
                    jumlahHalaman = pages,
                    genre = genre,
                    deskripsi = synopsis,
                    progress = 0,
                    status = "Belum dibaca",
                    terakhirDibaca = "",
                    coverUri = selectedImageUri?.toString()
                )

                val resultIntent = Intent()
                resultIntent.putExtra("NEW_BOOK", newBook)
                setResult(Activity.RESULT_OK, resultIntent)
                
                Toast.makeText(this, "Buku berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCropProcess() {

        val options = CropImageOptions().apply {
            guidelines = CropImageView.Guidelines.ON
            cropMenuCropButtonTitle = "OK"
        }

        cropImage.launch(
            CropImageContractOptions(
                null,
                options
            )
        )
    }
}
