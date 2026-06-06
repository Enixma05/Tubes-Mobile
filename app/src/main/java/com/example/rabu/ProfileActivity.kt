package com.example.rabu

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etJoinDate: TextInputEditText
    private lateinit var btnEditProfile: Button
    private var isEditMode = false

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            ivProfile.setImageURI(uri)
            ivProfile.setPadding(0, 0, 0, 0)

            val session = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val currentUser = session.getString("current_user", "User") ?: "User"

            getSharedPreferences("UserProfile", MODE_PRIVATE).edit {
                putString("image_$currentUser", uri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbarProfile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        ivProfile = findViewById(R.id.ivProfile)
        etName = findViewById(R.id.etProfileName)
        etPhone = findViewById(R.id.etProfilePhone)
        etEmail = findViewById(R.id.etProfileEmail)
        etJoinDate = findViewById(R.id.etProfileJoinDate)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        loadProfileData()

        findViewById<androidx.cardview.widget.CardView>(R.id.cvProfileImage).setOnClickListener {
            val options = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
                cropShape = CropImageView.CropShape.OVAL
                fixAspectRatio = true
                aspectRatioX = 1
                aspectRatioY = 1
                activityTitle = "Sesuaikan Foto Profil"
            }
            cropImage.launch(CropImageContractOptions(null, options))
        }

        btnEditProfile.setOnClickListener {
            toggleEditMode()
        }
    }

    private fun toggleEditMode() {

        isEditMode = !isEditMode

        etName.isEnabled = isEditMode
        etPhone.isEnabled = isEditMode
        etEmail.isEnabled = isEditMode

        etJoinDate.isEnabled = false

        if (isEditMode) {

            btnEditProfile.text = "Simpan Perubahan"

            etName.requestFocus()

            val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

            imm.showSoftInput(
                etName,
                InputMethodManager.SHOW_IMPLICIT
            )

        } else {

            if (saveProfileInfo()) {

                btnEditProfile.text = "Edit Profil"

                etName.isEnabled = false
                etPhone.isEnabled = false
                etEmail.isEnabled = false

                Toast.makeText(
                    this,
                    "Profil diperbarui!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                isEditMode = true

                etName.isEnabled = true
                etPhone.isEnabled = true
                etEmail.isEnabled = true

                btnEditProfile.text = "Simpan Perubahan"
            }
        }
    }

    private fun saveProfileInfo(): Boolean {
        etPhone.error = null
        etEmail.error = null

        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return false
        }

        if (phone.isNotEmpty() && !phone.matches(Regex("\\d+"))) {
            etPhone.error = "Nomor telepon hanya boleh berisi angka"
            return false
        }
        if (email.isNotEmpty() && !email.endsWith("@gmail.com")) {
            etEmail.error = "Email harus menggunakan @gmail.com"
            return false
        }

        val session = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"

        getSharedPreferences("UserProfile", MODE_PRIVATE).edit {
            putString("name_$currentUser", name)
            putString("phone_$currentUser", phone)
            putString("email_$currentUser", email)
        }

        return true
    }

    private fun loadProfileData() {
        val session = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUser = session.getString("current_user", "User") ?: "User"
        val prefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        
        etName.setText(prefs.getString("name_$currentUser", currentUser))
        etPhone.setText(prefs.getString("phone_$currentUser", "-"))
        etEmail.setText(prefs.getString("email_$currentUser", "-"))
        etJoinDate.setText(prefs.getString("join_$currentUser", "-")
        )
        
        val img = prefs.getString("image_$currentUser", null)
        if (img != null) {
            try {
                ivProfile.setImageURI(Uri.parse(img))
            } catch (e: Exception) {
                ivProfile.setImageResource(R.drawable.profile)
            }
            ivProfile.setPadding(0, 0, 0, 0)
        }
    }
}
