package com.example.rabu.ui.book

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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

    // View Variables
    private lateinit var tvBookTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvPages: TextView
    private lateinit var tvSynopsis: TextView
    private lateinit var ivBookCover: ImageView
    private lateinit var etLastRead: EditText
    private lateinit var sliderRating: Slider
    private lateinit var tvRatingValue: TextView
    private lateinit var spinnerStatus: AutoCompleteTextView
    private lateinit var etNotes: EditText
    private lateinit var tilNotes: TextInputLayout
    private lateinit var tvNoteStatus: TextView
    private lateinit var btnSaveNotes: Button
    private lateinit var btnDeleteNotes: Button
    private lateinit var llNoteActions: LinearLayout

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
        tvBookTitle = findViewById(R.id.tvBookTitle)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvGenre = findViewById(R.id.tvGenre)
        tvPublisher = findViewById(R.id.tvPublisher)
        tvPages = findViewById(R.id.tvPages)
        tvSynopsis = findViewById(R.id.tvSynopsis)
        ivBookCover = findViewById(R.id.ivBookCover)
        etLastRead = findViewById(R.id.etLastRead)
        sliderRating = findViewById(R.id.sliderRating)
        tvRatingValue = findViewById(R.id.tvRatingValue)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etNotes = findViewById(R.id.etNotes)
        tilNotes = findViewById(R.id.tilNotes)
        tvNoteStatus = findViewById(R.id.tvNoteStatus)
        btnSaveNotes = findViewById(R.id.btnSaveNotes)
        btnDeleteNotes = findViewById(R.id.btnDeleteNotes)
        llNoteActions = findViewById(R.id.llNoteActions)
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

        currentBuku?.let { buku ->
            tvBookTitle.text = buku.judul
            tvAuthor.text = "Penulis: ${buku.author}"
            tvGenre.text = "Genre: ${buku.genre}"
            tvPublisher.text = "Penerbit: ${buku.penerbit}"
            tvPages.text = "Jumlah Halaman: ${buku.jumlahHalaman}"
            tvSynopsis.text = buku.deskripsi

            if (!buku.coverUri.isNullOrEmpty()) {
                ivBookCover.setImageURI(Uri.parse(buku.coverUri))
                ivBookCover.setPadding(0, 0, 0, 0)
            }

            loadSavedExtras(buku.judul)
        }
    }

    private fun setupListeners() {
        // Dropdown Status
        val statusOptions = resources.getStringArray(R.array.status_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusOptions)
        spinnerStatus.setAdapter(adapter)

        etLastRead.doAfterTextChanged { text ->
            val totalPage = currentBuku?.jumlahHalaman ?: 0
            val cleanValue = DataHelper.cleanPageInput(text.toString(), totalPage)

            if (etLastRead.text.toString() != cleanValue) {
                etLastRead.setText(cleanValue)
                etLastRead.setSelection(cleanValue.length)
            }

            val progress = DataHelper.calculateProgress(cleanValue.toInt(), totalPage)
            updateStatusByProgress(progress)

            currentBuku = currentBuku?.copy(halamanTerakhir = cleanValue, progress = progress)
            saveToPrefs("last_read", cleanValue)
            saveToPrefs("progress", progress)
        }

        spinnerStatus.setOnItemClickListener { parent, _, position, _ ->
            val selectedStatus = parent.getItemAtPosition(position).toString()
            currentBuku = currentBuku?.copy(status = selectedStatus)
            saveToPrefs("status", selectedStatus)

            sliderRating.isEnabled = selectedStatus != "Belum dibaca"
        }

        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = "Rating: ${value.toInt()}/10"
            saveToPrefs("rating", value)
        }

        btnSaveNotes.setOnClickListener {
            if (btnSaveNotes.text == "Tambahkan catatan" || btnSaveNotes.text == "Edit") {
                setEditMode(true)
            } else {
                saveNotes()
                hideKeyboard()
            }
        }

        btnDeleteNotes.setOnClickListener { showDeleteConfirmationDialog() }
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
            saveToPrefs("status", statusBaru)
        }
    }

    private fun loadSavedExtras(bookTitle: String) {
        val user = prefManager.getCurrentUser()
        val prefs = getSharedPreferences("BookPrefs", MODE_PRIVATE)

        val savedLastRead = prefs.getString("${user}_${bookTitle}_last_read", "0") ?: "0"
        etLastRead.setText(savedLastRead)

        val savedStatus = prefs.getString("${user}_${bookTitle}_status", "Belum dibaca") ?: "Belum dibaca"
        spinnerStatus.setText(savedStatus, false)

        val savedRating = prefs.getFloat("${user}_${bookTitle}_rating", 0f)
        sliderRating.value = savedRating
        tvRatingValue.text = "Rating: ${savedRating.toInt()}/10"
        sliderRating.isEnabled = savedStatus != "Belum dibaca"

        val savedNotes = prefs.getString("${user}_${bookTitle}_notes", "") ?: ""
        etNotes.setText(savedNotes)
        updateNoteUiState(savedNotes.isNotEmpty())
    }

    private fun saveToPrefs(key: String, value: Any) {
        val user = prefManager.getCurrentUser()
        val title = currentBuku?.judul ?: ""
        val prefs = getSharedPreferences("BookPrefs", MODE_PRIVATE).edit()

        when (value) {
            is String -> prefs.putString("${user}_${title}_$key", value)
            is Int -> prefs.putInt("${user}_${title}_$key", value)
            is Float -> prefs.putFloat("${user}_${title}_$key", value)
        }
        prefs.apply()
    }

    private fun saveNotes() {
        val notes = etNotes.text.toString().trim()
        saveToPrefs("notes", notes)
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

    private fun setEditMode(editable: Boolean) {
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
                saveToPrefs("notes", "")
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

    // GANTI BAGIAN @Deprecated onBackPressed dengan ini:
    override fun onSupportNavigateUp(): Boolean {
        finishWithResult()
        return true
    }
}