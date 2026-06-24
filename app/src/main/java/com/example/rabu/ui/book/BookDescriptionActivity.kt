package com.example.rabu.ui.book

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.rabu.R
import com.example.rabu.data.local.PrefManager
import com.example.rabu.data.model.Buku
import com.example.rabu.utils.DataHelper
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout

class BookDescriptionActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private var currentBuku: Buku? = null
    private var itemPosition: Int = -1

    // View Variables - Informasi Utama
    private lateinit var etBookTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etGenre: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etTotalPages: EditText
    private lateinit var etSynopsis: EditText
    private lateinit var ivBookCover: ImageView
    private lateinit var btnEditBookInfo: Button
    
    // View Variables - Progres & Catatan
    private lateinit var etLastRead: EditText
    private lateinit var sliderRating: Slider
    private lateinit var tvRatingValue: TextView
    private lateinit var spinnerStatus: AutoCompleteTextView
    private lateinit var etNotes: EditText
    private lateinit var tilNotes: TextInputLayout
    private lateinit var tvNoteStatus: TextView
    private lateinit var btnSaveNotes: Button
    private lateinit var btnDeleteNotes: Button

    private var isEditInfoMode = false

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            ivBookCover.setImageURI(uri)
            ivBookCover.setPadding(0, 0, 0, 0)
            currentBuku = currentBuku?.copy(coverUri = uri?.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_description)
        prefManager = PrefManager(this)

        initViews()
        setupToolbar()
        setupData()
        setupListeners()
    }

    private fun initViews() {
        etBookTitle = findViewById(R.id.etBookTitle)
        etAuthor = findViewById(R.id.etAuthor)
        etGenre = findViewById(R.id.etGenre)
        etPublisher = findViewById(R.id.etPublisher)
        etTotalPages = findViewById(R.id.etTotalPages)
        etSynopsis = findViewById(R.id.etSynopsis)
        ivBookCover = findViewById(R.id.ivBookCover)
        btnEditBookInfo = findViewById(R.id.btnEditBookInfo)

        etLastRead = findViewById(R.id.etLastRead)
        sliderRating = findViewById(R.id.sliderRating)
        tvRatingValue = findViewById(R.id.tvRatingValue)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etNotes = findViewById(R.id.etNotes)
        tilNotes = findViewById(R.id.tilNotes)
        tvNoteStatus = findViewById(R.id.tvNoteStatus)
        btnSaveNotes = findViewById(R.id.btnSaveNotes)
        btnDeleteNotes = findViewById(R.id.btnDeleteNotes)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finishWithResult() }
    }

    private fun setupData() {
        itemPosition = intent.getIntExtra("EXTRA_POSITION", -1)
        currentBuku = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_BUKU", Buku::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_BUKU") as? Buku
        }

        currentBuku?.let { updateUiWithBook(it) }
    }

    private fun updateUiWithBook(buku: Buku) {
        etBookTitle.setText(buku.judul)
        etAuthor.setText(buku.author)
        etGenre.setText(buku.genre)
        etPublisher.setText(buku.penerbit)
        etTotalPages.setText(buku.jumlahHalaman.toString())
        etSynopsis.setText(buku.deskripsi)

        if (!buku.coverUri.isNullOrEmpty()) {
            ivBookCover.setImageURI(Uri.parse(buku.coverUri))
            ivBookCover.setPadding(0, 0, 0, 0)
        } else {
            ivBookCover.setImageResource(android.R.drawable.ic_menu_gallery)
            ivBookCover.setPadding(30, 30, 30, 30)
        }

        // Load Progress & Rating
        etLastRead.setText(buku.halamanTerakhir)
        spinnerStatus.setText(buku.status, false)
        sliderRating.value = buku.rating
        tvRatingValue.text = "Rating: ${buku.rating.toInt()}/10"
        
        updateInteractionByStatus(buku.status)

        etNotes.setText(buku.notes)
        updateNoteUiState(buku.notes.isNotEmpty())
    }

    private fun setupListeners() {
        btnEditBookInfo.setOnClickListener { toggleEditInfoMode() }

        findViewById<CardView>(R.id.cvBookCover).setOnClickListener {
            if (isEditInfoMode) {
                val options = CropImageOptions().apply { guidelines = CropImageView.Guidelines.ON }
                cropImage.launch(CropImageContractOptions(null, options))
            }
        }

        val statusOptions = arrayOf("Belum dibaca", "Sedang dibaca", "Sudah dibaca")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusOptions)
        spinnerStatus.setAdapter(adapter)

        etLastRead.doAfterTextChanged { text ->
            if (spinnerStatus.text.toString() == "Belum dibaca") return@doAfterTextChanged
            
            val totalPage = currentBuku?.jumlahHalaman ?: 0
            val cleanValue = DataHelper.cleanPageInput(text.toString(), totalPage)

            if (etLastRead.text.toString() != cleanValue) {
                etLastRead.setText(cleanValue)
                etLastRead.setSelection(cleanValue.length)
            }

            val progress = DataHelper.calculateProgress(cleanValue.toInt(), totalPage)
            updateStatusByProgress(progress)

            currentBuku = currentBuku?.copy(halamanTerakhir = cleanValue, progress = progress)
        }

        spinnerStatus.setOnItemClickListener { parent, _, position, _ ->
            val selectedStatus = parent.getItemAtPosition(position).toString()
            updateInteractionByStatus(selectedStatus)
            
            if (selectedStatus == "Belum dibaca") {
                etLastRead.setText("0")
                sliderRating.value = 0f
                tvRatingValue.text = "Rating: 0/10"
                currentBuku = currentBuku?.copy(status = selectedStatus, halamanTerakhir = "0", progress = 0, rating = 0f)
            } else {
                currentBuku = currentBuku?.copy(status = selectedStatus)
            }
        }

        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = "Rating: ${value.toInt()}/10"
            currentBuku = currentBuku?.copy(rating = value)
        }

        btnSaveNotes.setOnClickListener {
            if (btnSaveNotes.text == "Tambahkan catatan" || btnSaveNotes.text == "Edit") {
                setEditNoteMode(true)
            } else {
                saveNotes()
                hideKeyboard()
            }
        }

        btnDeleteNotes.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun toggleEditInfoMode() {
        isEditInfoMode = !isEditInfoMode
        
        etBookTitle.isEnabled = isEditInfoMode
        etAuthor.isEnabled = isEditInfoMode
        etGenre.isEnabled = isEditInfoMode
        etPublisher.isEnabled = isEditInfoMode
        etTotalPages.isEnabled = isEditInfoMode
        etSynopsis.isEnabled = isEditInfoMode

        if (isEditInfoMode) {
            btnEditBookInfo.text = "Simpan Perubahan"
            etBookTitle.requestFocus()
        } else {
            btnEditBookInfo.text = "Edit Informasi Buku"
            saveBookInfo()
            Toast.makeText(this, "Informasi buku diperbarui!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBookInfo() {
        val newTotalPages = etTotalPages.text.toString().toIntOrNull() ?: 0
        val lastRead = etLastRead.text.toString().toIntOrNull() ?: 0
        val newProgress = DataHelper.calculateProgress(lastRead, newTotalPages)

        currentBuku = currentBuku?.copy(
            judul = etBookTitle.text.toString(),
            author = etAuthor.text.toString(),
            genre = etGenre.text.toString(),
            penerbit = etPublisher.text.toString(),
            jumlahHalaman = newTotalPages,
            deskripsi = etSynopsis.text.toString(),
            progress = newProgress
        )
        updateStatusByProgress(newProgress)
    }

    private fun updateInteractionByStatus(status: String) {
        val isNotBelumDibaca = status != "Belum dibaca"
        sliderRating.isEnabled = isNotBelumDibaca
        etLastRead.isEnabled = isNotBelumDibaca
    }

    private fun updateStatusByProgress(progress: Int) {
        val statusBaru = when {
            progress <= 0 -> "Belum dibaca"
            progress >= 100 -> "Sudah dibaca"
            else -> "Sedang dibaca"
        }
        if (spinnerStatus.text.toString() != statusBaru) {
            spinnerStatus.setText(statusBaru, false)
            currentBuku = currentBuku?.copy(status = statusBaru)
            updateInteractionByStatus(statusBaru)
        }
    }

    private fun saveNotes() {
        val notes = etNotes.text.toString().trim()
        currentBuku = currentBuku?.copy(notes = notes)
        updateNoteUiState(notes.isNotEmpty())
        Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show()
    }

    private fun updateNoteUiState(hasNotes: Boolean) {
        if (hasNotes) {
            tilNotes.isVisible = true
            etNotes.isEnabled = false
            tvNoteStatus.text = "Tersimpan"
            btnSaveNotes.text = "Edit"
            btnDeleteNotes.isVisible = true
        } else {
            tilNotes.isGone = true
            tvNoteStatus.text = "Belum ada catatan"
            btnSaveNotes.text = "Tambahkan catatan"
            btnDeleteNotes.isGone = true
        }
    }

    private fun setEditNoteMode(editable: Boolean) {
        tilNotes.isVisible = true
        etNotes.isEnabled = editable
        btnSaveNotes.text = if (editable) "Simpan" else "Edit"
        if (editable) {
            etNotes.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etNotes, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Yakin ingin menghapus catatan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                currentBuku = currentBuku?.copy(notes = "")
                etNotes.setText("")
                updateNoteUiState(false)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun finishWithResult() {
        val intent = Intent().apply {
            putExtra("UPDATED_BUKU", currentBuku)
            putExtra("EXTRA_POSITION", itemPosition)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finishWithResult()
        return true
    }
}
