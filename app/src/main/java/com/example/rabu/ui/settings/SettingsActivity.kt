package com.example.rabu.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.rabu.R
import com.example.rabu.data.local.PrefManager
import com.example.rabu.ui.auth.LoginActivity
import com.example.rabu.ui.profile.ProfileActivity
import com.google.android.material.card.MaterialCardView

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        prefManager = PrefManager(this)

        val toolbar: Toolbar = findViewById(R.id.toolbarSettings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        findViewById<MaterialCardView>(R.id.cvGoToProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Pengaturan Tema
        val rgThemeMode = findViewById<RadioGroup>(R.id.rgThemeMode)
        val savedTheme = prefManager.getThemeMode()

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
            prefManager.saveThemeMode(mode)
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                prefManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}