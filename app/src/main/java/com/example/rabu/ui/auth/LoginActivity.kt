package com.example.rabu.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.rabu.R
import com.example.rabu.data.local.PrefManager
import com.example.rabu.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)
        auth = FirebaseAuth.getInstance()

        // Cek sesi login otomatis
        if (prefManager.isLoggedIn()) {
            AppCompatDelegate.setDefaultNightMode(prefManager.getThemeMode())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnToRegister = findViewById<Button>(R.id.btnSignup)

        // Navigasi ke Halaman Register
        btnToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- LOGIKA LOGIN FIREBASE ---
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        prefManager.setLoginStatus(true, user?.uid)

                        // Terapkan tema user
                        AppCompatDelegate.setDefaultNightMode(prefManager.getThemeMode())

                        Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
