package com.example.rabu

import android.content.Context
import android.content.Intent
import android.graphics.Color
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

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finishWithResult()
        }

        // Inisialisasi View
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

        // Ambil data buku
        itemPosition = intent.getIntExtra("EXTRA_POSITION", -1)

        currentBuku =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(
                    "EXTRA_BUKU",
                    Buku::class.java
                )
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

            // Cover
            if (!buku.coverUri.isNullOrEmpty()) {
                ivBookCover.setImageURI(Uri.parse(buku.coverUri))
                ivBookCover.setPadding(0, 0, 0, 0)
            } else {
                ivBookCover.setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
                ivBookCover.setPadding(
                    30,
                    30,
                    30,
                    30
                )
            }

            loadSavedData(buku.judul)
        }

        // Auto save terakhir dibaca
        etLastRead.doAfterTextChanged { text ->

            val lastRead = text.toString()

            currentBuku = currentBuku?.copy(
                terakhirDibaca = lastRead
            )

            saveLastRead(
                currentBuku?.judul ?: "",
                lastRead
            )
        }

        // Dropdown status
        val statusOptions = arrayOf(
            "Belum dibaca",
            "Sedang dibaca",
            "Sudah dibaca"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusOptions
        )

        spinnerStatus.setAdapter(adapter)

        spinnerStatus.setOnItemClickListener { parent, _, position, _ ->

            val selectedStatus =
                parent.getItemAtPosition(position).toString()

            currentBuku = currentBuku?.copy(
                status = selectedStatus
            )

            saveStatus(
                currentBuku?.judul ?: "",
                selectedStatus
            )

            sliderRating.isEnabled =
                selectedStatus != "Belum dibaca"

            if (selectedStatus == "Belum dibaca") {
                sliderRating.value = 0f
                tvRatingValue.text = "Rating: 0/10"

                saveRating(
                    currentBuku?.judul ?: "",
                    0f
                )
            }
        }

        // Rating
        sliderRating.addOnChangeListener { _, value, _ ->

            tvRatingValue.text =
                "Rating: ${value.toInt()}/10"

            saveRating(
                currentBuku?.judul ?: "",
                value
            )
        }

        // Catatan
        btnSaveNotes.setOnClickListener {

            if (
                btnSaveNotes.text == "Tambahkan catatan"
                || btnSaveNotes.text == "Edit"
            ) {
                setEditMode(true)
            } else {
                saveNotes()
                hideKeyboard()
            }
        }

        btnDeleteNotes.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        tvPublisherDisplay.isClickable = false
        tvPublisherDisplay.isFocusable = false
    }

    private fun finishWithResult() {
        val intent = Intent()
        intent.putExtra(
            "UPDATED_BUKU",
            currentBuku
        )
        intent.putExtra(
            "EXTRA_POSITION",
            itemPosition
        )
        setResult(RESULT_OK, intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishWithResult()
    }

    private fun setEditMode(editable: Boolean) {
        tilNotes.isVisible = true
        etNotes.isEnabled = editable
        btnSaveNotes.text =
            if (editable) "Simpan" else "Edit"

        updateButtonLayout(false)

        if (editable) {
            showKeyboard(etNotes)
        }
    }

    private fun saveRating(
        bookTitle: String,
        rating: Float
    ) {
        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).edit {
            putFloat(
                "${bookTitle}_rating",
                rating
            )
        }
    }

    private fun saveStatus(
        bookTitle: String,
        status: String
    ) {
        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).edit {
            putString(
                "${bookTitle}_status",
                status
            )
        }
    }

    private fun saveLastRead(
        bookTitle: String,
        lastRead: String
    ) {
        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).edit {
            putString(
                "${bookTitle}_last_read",
                lastRead
            )
        }
    }

    private fun saveNotes() {

        val notes =
            etNotes.text.toString().trim()

        val title =
            currentBuku?.judul ?: ""

        getSharedPreferences(
            prefsName,
            Context.MODE_PRIVATE
        ).edit {
            putString(
                "${title}_notes",
                notes
            )
        }

        updateUiState(
            notes.isNotEmpty()
        )

        Toast.makeText(
            this,
            "Catatan disimpan!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun loadSavedData(
        bookTitle: String
    ) {

        val sharedPref =
            getSharedPreferences(
                prefsName,
                Context.MODE_PRIVATE
            )

        // Status
        val savedStatus =
            sharedPref.getString(
                "${bookTitle}_status",
                currentBuku?.status
                    ?: "Belum dibaca"
            )

        spinnerStatus.setText(
            savedStatus,
            false
        )

        currentBuku =
            currentBuku?.copy(
                status = savedStatus!!
            )

        // Rating
        sliderRating.isEnabled =
            savedStatus != "Belum dibaca"

        val savedRating =
            sharedPref.getFloat(
                "${bookTitle}_rating",
                0f
            )

        sliderRating.value =
            savedRating

        tvRatingValue.text =
            "Rating: ${savedRating.toInt()}/10"

        // Last Read
        val savedLastRead =
            sharedPref.getString(
                "${bookTitle}_last_read",
                currentBuku?.terakhirDibaca ?: ""
            ) ?: ""

        etLastRead.setText(
            savedLastRead
        )

        currentBuku =
            currentBuku?.copy(
                terakhirDibaca =
                    savedLastRead
            )

        // Notes
        val savedNotes =
            sharedPref.getString(
                "${bookTitle}_notes",
                ""
            ) ?: ""

        etNotes.setText(
            savedNotes
        )

        updateUiState(
            savedNotes.isNotEmpty()
        )
    }

    private fun updateUiState(
        hasNotes: Boolean
    ) {

        if (hasNotes) {

            tilNotes.isVisible = true
            etNotes.isEnabled = false

            tvNoteStatus.text =
                "Tersimpan"

            tvNoteStatus.setTextColor(
                Color.parseColor(
                    "#4CAF50"
                )
            )

            btnSaveNotes.text =
                "Edit"

            btnDeleteNotes.isVisible =
                true

            updateButtonLayout(false)

        } else {

            tilNotes.isGone = true

            tvNoteStatus.text =
                "Belum ada catatan"

            btnSaveNotes.text =
                "Tambahkan catatan"

            btnDeleteNotes.isGone =
                true

            updateButtonLayout(true)
        }
    }

    private fun updateButtonLayout(
        isInitialAddMode: Boolean
    ) {

        val params =
            btnSaveNotes.layoutParams
                    as LinearLayout.LayoutParams

        val density =
            resources.displayMetrics.density

        if (isInitialAddMode) {
            params.width =
                LinearLayout.LayoutParams.MATCH_PARENT
        } else {
            params.width =
                (110 * density).toInt()
        }

        btnSaveNotes.layoutParams =
            params
    }

    private fun showDeleteConfirmationDialog() {

        AlertDialog.Builder(this)
            .setTitle(
                "Hapus Catatan"
            )
            .setMessage(
                "Yakin ingin menghapus?"
            )
            .setPositiveButton(
                "Hapus"
            ) { _, _ ->

                val title =
                    currentBuku?.judul ?: ""

                getSharedPreferences(
                    prefsName,
                    Context.MODE_PRIVATE
                ).edit {
                    remove(
                        "${title}_notes"
                    )
                }

                etNotes.setText("")

                updateUiState(false)
            }
            .setNegativeButton(
                "Batal",
                null
            )
            .show()
    }

    private fun showKeyboard(
        view: View
    ) {

        view.requestFocus()

        val imm =
            getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

        imm.showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    private fun hideKeyboard() {

        val view =
            currentFocus

        if (view != null) {

            val imm =
                getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

            imm.hideSoftInputFromWindow(
                view.windowToken,
                0
            )
        }
    }
}