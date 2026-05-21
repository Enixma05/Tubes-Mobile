package com.example.rabu

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
    private lateinit var tvPublisherDisplay: TextView
    
    private val prefsName = "BookPrefs"
    private val keyNotes = "notes_content"
    private val keyRating = "book_rating"
    private val keyStatus = "book_status"
    private val keyPublisher = "book_publisher"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_description)

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup Slider Rating
        tvRatingValue = findViewById(R.id.tvRatingValue)
        sliderRating = findViewById(R.id.sliderRating)

        // Setup Dropdown Status
        val statusOptions = resources.getStringArray(R.array.status_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusOptions)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerStatus.setAdapter(adapter)

        spinnerStatus.setOnItemClickListener { parent, _, position, _ ->
            val selectedStatus = parent.getItemAtPosition(position).toString()
            saveStatus(selectedStatus)
            
            if (selectedStatus == "Belum dibaca") {
                sliderRating.value = 0f
                sliderRating.isEnabled = false
                tvRatingValue.text = "Rating: 0/10"
                saveRating(0f)
            } else {
                sliderRating.isEnabled = true
            }
        }

        sliderRating.addOnChangeListener { _, value, _ ->
            val rating = value.toInt()
            tvRatingValue.text = "Rating: $rating/10"
            saveRating(value)
        }

        // Setup Views
        etNotes = findViewById(R.id.etNotes)
        tilNotes = findViewById(R.id.tilNotes)
        tvNoteStatus = findViewById(R.id.tvNoteStatus)
        btnSaveNotes = findViewById(R.id.btnSaveNotes)
        btnDeleteNotes = findViewById(R.id.btnDeleteNotes)
        llNoteActions = findViewById(R.id.llNoteActions)
        tvPublisherDisplay = findViewById(R.id.tvPublisher)

        // Load existing data
        loadSavedData()

        etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                handleDraftStatus()
            }
        })

        btnSaveNotes.setOnClickListener {
            val currentStatus = btnSaveNotes.text.toString()
            when (currentStatus) {
                "Edit" -> {
                    setEditMode(true)
                    tvNoteStatus.text = "Mengedit..."
                    etNotes.requestFocus()
                    etNotes.setSelection(etNotes.text.length)
                }
                "Tambahkan catatan" -> {
                    setEditMode(true)
                    tvNoteStatus.text = "Menulis..."
                    etNotes.requestFocus()
                }
                "Simpan" -> {
                    saveNotes()
                    hideKeyboard()
                }
            }
        }

        btnDeleteNotes.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Penerbit dibuat tidak bisa diklik/diedit sesuai permintaan
        tvPublisherDisplay.isClickable = false
        tvPublisherDisplay.isFocusable = false
    }

    private fun handleDraftStatus() {
        val currentNotes = etNotes.text.toString().trim()
        val sharedPref = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedNotes = sharedPref.getString(keyNotes, "") ?: ""
        
        if (tilNotes.isVisible && etNotes.isEnabled) {
            if (currentNotes != savedNotes) {
                tvNoteStatus.text = "Draft (Belum disimpan)"
                tvNoteStatus.setTextColor(Color.RED)
                btnSaveNotes.text = "Simpan"
                updateButtonLayout(false)
            } else if (savedNotes.isNotEmpty()) {
                tvNoteStatus.text = "Tersimpan"
                tvNoteStatus.setTextColor(Color.parseColor("#4CAF50"))
                btnSaveNotes.text = "Edit"
                updateButtonLayout(false)
            }
        }
    }

    private fun setEditMode(editable: Boolean) {
        tilNotes.isVisible = true
        etNotes.isEnabled = editable
        btnSaveNotes.text = if (editable) "Simpan" else "Edit"
        updateButtonLayout(false)
        if (editable) showKeyboard(etNotes)
    }

    private fun saveRating(rating: Float) {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
            putFloat(keyRating, rating)
        }
    }

    private fun saveStatus(status: String) {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
            putString(keyStatus, status)
        }
    }

    private fun updateButtonLayout(isInitialAddMode: Boolean) {
        val params = btnSaveNotes.layoutParams as LinearLayout.LayoutParams
        if (isInitialAddMode) {
            llNoteActions.gravity = Gravity.START
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            btnSaveNotes.minWidth = 0
        } else {
            llNoteActions.gravity = Gravity.END
            params.width = (110 * resources.displayMetrics.density).toInt()
            btnSaveNotes.minWidth = params.width
        }
        btnSaveNotes.layoutParams = params
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteNotes()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun getSavedNotesContent(): String? {
        val sharedPref = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return sharedPref.getString(keyNotes, null)
    }

    private fun saveNotes() {
        val notes = etNotes.text.toString().trim()
        if (notes.isNotEmpty()) {
            getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
                putString(keyNotes, notes)
            }
            updateUiState(true)
            Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNotes() {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit {
            remove(keyNotes)
        }
        etNotes.setText("")
        updateUiState(false)
        Toast.makeText(this, "Catatan dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedData() {
        val sharedPref = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        
        val savedStatus = sharedPref.getString(keyStatus, "Belum dibaca")
        spinnerStatus.setText(savedStatus, false)
        sliderRating.isEnabled = (savedStatus != "Belum dibaca")

        val savedRating = sharedPref.getFloat(keyRating, 0f)
        sliderRating.value = if (savedStatus == "Belum dibaca") 0f else savedRating
        tvRatingValue.text = "Rating: ${sliderRating.value.toInt()}/10"

        val publisherValue = sharedPref.getString(keyPublisher, "Nama Penerbit (Tahun)")
        tvPublisherDisplay.text = "Penerbit: $publisherValue"

        val savedNotes = sharedPref.getString(keyNotes, null)
        etNotes.setText(savedNotes)

        if (!savedNotes.isNullOrEmpty()) {
            updateUiState(true)
        } else {
            updateUiState(false)
        }
    }

    private fun updateUiState(hasNotes: Boolean) {
        if (hasNotes) {
            tilNotes.isVisible = true
            etNotes.isEnabled = false
            tvNoteStatus.text = "Tersimpan"
            tvNoteStatus.setTextColor(Color.parseColor("#4CAF50"))
            btnSaveNotes.text = "Edit"
            btnDeleteNotes.isVisible = true
            updateButtonLayout(false)
        } else {
            tilNotes.isGone = true
            etNotes.isEnabled = true
            tvNoteStatus.text = "Belum ada catatan"
            tvNoteStatus.setTextColor(Color.GRAY)
            btnSaveNotes.text = "Tambahkan catatan"
            btnDeleteNotes.isGone = true
            updateButtonLayout(true)
        }
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
