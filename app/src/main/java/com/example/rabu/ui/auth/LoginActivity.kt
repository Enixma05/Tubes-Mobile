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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private var isLoginMode = true
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)

        // Cek sesi login otomatis
        if (prefManager.isLoggedIn()) {
            AppCompatDelegate.setDefaultNightMode(prefManager.getThemeMode())
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
                val registeredPass = prefManager.getRegisteredPassword(user)

                if (registeredPass == null) {
                    Toast.makeText(this, "Akun tidak ditemukan. Silakan daftar dulu.", Toast.LENGTH_SHORT).show()
                } else if (pass == registeredPass) {
                    prefManager.setLoginStatus(true, user)

                    // Terapkan tema user
                    AppCompatDelegate.setDefaultNightMode(prefManager.getThemeMode())

                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Logika DAFTAR (Sign In)
                if (prefManager.isUsernameExists(user)) {
                    Toast.makeText(this, "Username sudah digunakan.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (pass == user) {
                    Toast.makeText(this, "Password tidak boleh sama dengan username.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
                prefManager.registerUser(user, pass, currentDate)

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