package com.example.rabu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cek sesi login otomatis
        val session = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        if (session.getBoolean("is_logged_in", false)) {
            val user = session.getString("current_user", "User") ?: "User"
            val themeMode = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                .getInt("theme_mode_$user", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(themeMode)
            
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnAction = findViewById<Button>(R.id.btnLogin)
        val btnToggle = findViewById<Button>(R.id.btnSignup)
        val tvTitleSub = findViewById<TextView>(R.id.tvTitleSub)

        val userPrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        btnToggle.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                btnAction.text = "Login"
                btnToggle.text = "Belum punya akun? Daftar sekarang"
                tvTitleSub.text = "Silakan masuk ke akun Anda"
            } else {
                btnAction.text = "Sign In"
                btnToggle.text = "Login sekarang"
                tvTitleSub.text = "Buat akun baru Anda"
            }
        }

        btnAction.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi username dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                // Logika LOGIN
                val registeredPass = userPrefs.getString("password_$user", null)

                if (registeredPass == null) {
                    Toast.makeText(
                        this,
                        "Akun tidak ditemukan. Silakan daftar dulu.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (pass == registeredPass) {

                    session.edit {
                        putBoolean("is_logged_in", true)
                        putString("current_user", user)
                    }

                    // Terapkan tema user
                    val themeMode = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                        .getInt("theme_mode_$user", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    AppCompatDelegate.setDefaultNightMode(themeMode)

                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Password salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                // Logika DAFTAR (Sign In)
                if (userPrefs.contains("username_$user")) {
                    Toast.makeText(
                        this,
                        "Username sudah digunakan.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Validasi tambahan: Password tidak boleh sama dengan username (opsional tapi disarankan)
                if (pass == user) {
                    Toast.makeText(this, "Password tidak boleh sama dengan username.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())

                userPrefs.edit {
                    putString("username_$user", user)
                    putString("password_$user", pass)
                    putString("join_$user", currentDate)
                }
                
                Toast.makeText(this, "Akun berhasil dibuat! Silakan Login", Toast.LENGTH_SHORT).show()
                
                // Kembalikan ke mode login otomatis
                isLoginMode = true
                btnAction.text = "Login"
                btnToggle.text = "Belum punya akun? Daftar sekarang"
                tvTitleSub.text = "Silakan masuk ke akun Anda"
                etPassword.setText("")
            }
        }
    }
}
