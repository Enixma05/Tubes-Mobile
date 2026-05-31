package com.example.rabu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Navigasi ke Profil
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cvGoToProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Pengaturan Tema
        val rgThemeMode = findViewById<RadioGroup>(R.id.rgThemeMode)
        val appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedTheme = appPrefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> rgThemeMode.check(R.id.rbThemeLight)
            AppCompatDelegate.MODE_NIGHT_YES -> rgThemeMode.check(R.id.rbThemeDark)
            else -> rgThemeMode.check(R.id.rbThemeSystem)
        }

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            appPrefs.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Tombol Sign Out (Pindah dari Profile)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                // Hapus sesi login
                getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply()
                
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
