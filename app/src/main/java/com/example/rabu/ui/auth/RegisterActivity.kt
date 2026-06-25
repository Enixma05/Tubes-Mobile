package com.example.rabu.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rabu.R
import com.example.rabu.data.local.PrefManager
import com.example.rabu.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        prefManager = PrefManager(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvBackToLogin = findViewById<Button>(R.id.btnBackToLogin)


        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val username = etUsername.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, "Isi email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

                        prefManager.setLoginStatus(false, user?.uid)
                        prefManager.saveJoinDate(currentDate)
                        prefManager.saveProfileInfo(username, email)

                        auth.signOut()

                        Toast.makeText(this, "Akun berhasil dibuat! Silakan login.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}