package com.example.rabu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout

class BookDescriptionActivity : AppCompatActivity() {

    private lateinit var etNotes: EditText
    private lateinit var tilNotes: TextInputLayout
    private lateinit var tvNoteStatus: TextView
    private lateinit var btnSaveNotes: Button
    private lateinit var btnDeleteNotes: Button
    private lateinit var llNoteActions: LinearLayout
    private lateinit var tvRatingValue: TextView
    private lateinit var sliderRating: Slider
    private lateinit var spinnerStatus: AutoCompleteTextView

    private lateinit var tvBookTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvPublisherDisplay: TextView
    private lateinit var tvPages: TextView
    private lateinit var etLastRead: EditText
    private lateinit var tvSynopsis: TextView
    private lateinit var ivBookCover: ImageView

    private var currentBuku: Buku? = null
    private var itemPosition: Int = -1

    private val prefsName = "BookPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_description)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finishWithResult() }

        tvBookTitle = findViewById(R.id.tvBookTitle)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvGenre = findViewById(R.id.tvGenre)
        tvPublisherDisplay = findViewById(R.id.tvPublisher)
        tvPages = findViewById(R.id.tvPages)
        etLastRead = findViewById(R.id.etLastRead)
        tvSynopsis = findViewById(R.id.tvSynopsis)
        tvRatingValue = findViewById(R.id.tvRatingValue)
        sliderRating = findViewById(R.id.sliderRating)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etNotes = findViewById(R.id.etNotes)
        tilNotes = findViewById(R.id.tilNotes)
        tvNoteStatus = findViewById(R.id.tvNoteStatus)
        btnSaveNotes = findViewById(R.id.btnSaveNotes)
        btnDeleteNotes = findViewById(R.id.btnDeleteNotes)
        llNoteActions = findViewById(R.id.llNoteActions)
        ivBookCover = findViewById(R.id.ivBookCover)

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
            tvPublisherDisplay.text = "Penerbit: ${buku.penerbit}"
            tvPages.text = "Jumlah Halaman: ${buku.jumlahHalaman}"
            tvSynopsis.text = buku.deskripsi

            if (!buku.coverUri.isNullOrEmpty()) {
                ivBookCover.setImageURI(Uri.parse(buku.coverUri))
                ivBookCover.setPadding(0, 0, 0, 0)
            } else {
                ivBookCover.setImageResource(android.R.drawable.ic_menu_gallery)
                ivBookCover.setPadding(30, 30, 30, 30)
            }

            loadSavedData(buku.judul)
        }

        etLastRead.doAfterTextChanged { text ->
            val totalPage = currentBuku?.jumlahHalaman ?: 0
            val cleanValue = cleanLastRead(text.toString(), totalPage)
            val lastPage = cleanValue.toInt()

            if (etLastRead.text.toString() != cleanValue) {
                etLastRead.setText(cleanValue)
                etLastRead.setSelection(cleanValue.length)
            }

            val progress = if (totalPage > 0) ((lastPage.toFloat() / totalPage) * 100).toInt().coerceIn(0, 100) else 0

            // SINKRONISASI STATUS OTOMATIS BERDASARKAN PROGRES
            val statusBaru = when {
                progress <= 0 -> "Belum dibaca"
                progress >= 100 -> "Sudah dibaca"
                else -> "Sedang dibaca"
            }

            if (spinnerStatus.text.toString() != statusBaru) {
                spinnerStatus.setText(statusBaru, false)
            }

            currentBuku = currentBuku?.copy(
                halamanTerakhir = cleanValue,
                progress = progress,
                status = statusBaru
            )

            val session = getSharedPreferences("UserSession", MODE_PRIVATE)
            val currentUser = session.getString("current_user", "User") ?: "User"

            saveLastRead(currentUser, currentBuku?.judul ?: "", cleanValue)
            saveStatus(currentUser, currentBuku?.judul ?: "", statusBaru)
            getSharedPreferences(prefsName, MODE_PRIVATE).edit {
                putInt("${currentUser}_${currentBuku?.judul}_progress", progress)
            }
        }

        val statusOptions = resources.getStringArray(R.array.status_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusOptions)
        spinnerStatus.setAdapter(adapter)

        spinnerStatus.setOnItemClickListener { parent, _, position, _ ->
            val selectedStatus = parent.getItemAtPosition(position).toString()
            currentBuku = currentBuku?.copy(status = selectedStatus)
            
            val session = getSharedPreferences("UserSession", MODE_PRIVATE)
            val currentUser = session.getString("current_user", "User") ?: "User"
            
            saveStatus(currentUser, currentBuku?.judul ?: "", selectedStatus)
            sliderRating.isEnabled = selectedStatus != "Belum dibaca"
            if (selectedStatus == "Belum dibaca") {
                sliderRating.value = 0f
                tvRatingValue.text = "Rating: 0/10"
                saveRating(currentUser, currentBuku?.judul ?: "", 0f)
            }
        }

        sliderRating.addOnChangeListener { _, value, _ ->
            tvRatingValue.text = "Rating: ${value.toInt()}/10"
            val session = getSharedPreferences("UserSession", MODE_PRIVATE)
            val currentUser = session.getString("current_user", "User") ?: "User"
            saveRating(currentUser, currentBuku?.judul ?: "", value)
        }

        btnSaveNotes.setOnClickListener {
            if (btnSaveNotes.text == "Tambahkan catatan" || btnSaveNotes.text == "Edit") setEditMode(true)
            else { saveNotes(); hideKeyboard() }
        }

        btnDeleteNotes.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun cleanLastRead(input: String, totalPage: Int): String {
        val number = input.filter { it.isDigit() }.toIntOrNull() ?: 0
        return when {
            number <= 0 -> "0"
            number > totalPage -> totalPage.toString()
            else -> number.toString()
        }
    }

    private fun loadSavedData(bookTitle: String) {
        val session = getSharedPreferences("UserSession", MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        val sharedPref = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        
        // Ambil progres per user
        val savedProgress = sharedPref.getInt("${currentUser}_${bookTitle}_progress", 0)
        
        // PAKSA STATUS BERDASARKAN PROGRES SAAT LOAD
        val finalStatus = when {
            savedProgress <= 0 -> "Belum dibaca"
            savedProgress >= 100 -> "Sudah dibaca"
            else -> sharedPref.getString("${currentUser}_${bookTitle}_status", "Sedang dibaca") ?: "Sedang dibaca"
        }

        spinnerStatus.setText(finalStatus, false)
        currentBuku = currentBuku?.copy(status = finalStatus, progress = savedProgress)

        var savedLastRead = sharedPref.getString("${currentUser}_${bookTitle}_last_read", "0") ?: "0"
        savedLastRead = savedLastRead.filter { it.isDigit() }.ifEmpty { "0" }
        etLastRead.setText(savedLastRead)
        currentBuku = currentBuku?.copy(halamanTerakhir = savedLastRead)

        val savedRating = sharedPref.getFloat("${currentUser}_${bookTitle}_rating", 0f)
        sliderRating.value = savedRating
        tvRatingValue.text = "Rating: ${savedRating.toInt()}/10"

        val savedNotes = sharedPref.getString("${currentUser}_${bookTitle}_notes", "") ?: ""
        etNotes.setText(savedNotes)
        updateUiState(savedNotes.isNotEmpty())
    }

    private fun finishWithResult() {
        val intent = Intent()
        intent.putExtra("UPDATED_BUKU", currentBuku)
        intent.putExtra("EXTRA_POSITION", itemPosition)
        setResult(RESULT_OK, intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { finishWithResult() }

    private fun setEditMode(editable: Boolean) {
        tilNotes.isVisible = true
        etNotes.isEnabled = editable
        btnSaveNotes.text = if (editable) "Simpan" else "Edit"
        updateButtonLayout(false)
        if (editable) showKeyboard(etNotes)
    }

    private fun saveRating(user: String, bookTitle: String, rating: Float) {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit { putFloat("${user}_${bookTitle}_rating", rating) }
    }

    private fun saveStatus(user: String, bookTitle: String, status: String) {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit { putString("${user}_${bookTitle}_status", status) }
    }

    private fun saveLastRead(user: String, bookTitle: String, lastRead: String) {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit { putString("${user}_${bookTitle}_last_read", lastRead) }
    }

    private fun saveNotes() {
        val notes = etNotes.text.toString().trim()
        val title = currentBuku?.judul ?: ""
        val session = getSharedPreferences("UserSession", MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit { putString("${currentUser}_${title}_notes", notes) }
        updateUiState(notes.isNotEmpty())
        Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show()
    }

    private fun updateUiState(hasNotes: Boolean) {
        if (hasNotes) {
            tilNotes.isVisible = true
            etNotes.isEnabled = false
            tvNoteStatus.text = "Tersimpan"
            tvNoteStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            btnSaveNotes.text = "Edit"
            btnDeleteNotes.isVisible = true
            updateButtonLayout(false)
        } else {
            tilNotes.isGone = true
            tvNoteStatus.text = "Belum ada catatan"
            btnSaveNotes.text = "Tambahkan catatan"
            btnDeleteNotes.isGone = true
            updateButtonLayout(true)
        }
    }

    private fun updateButtonLayout(isInitialAddMode: Boolean) {
        val params = btnSaveNotes.layoutParams as LinearLayout.LayoutParams
        val density = resources.displayMetrics.density
        params.width = if (isInitialAddMode) LinearLayout.LayoutParams.MATCH_PARENT else (110 * density).toInt()
        btnSaveNotes.layoutParams = params
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Yakin ingin menghapus?")
            .setPositiveButton("Hapus") { _, _ ->
                val title = currentBuku?.judul ?: ""
                val session = getSharedPreferences("UserSession", MODE_PRIVATE)
                val currentUser = session.getString("current_user", "User") ?: "User"
                
                getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit { remove("${currentUser}_${title}_notes") }
                etNotes.setText("")
                updateUiState(false)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
