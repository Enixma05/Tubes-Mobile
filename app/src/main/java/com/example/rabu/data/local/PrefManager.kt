package com.example.rabu.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class PrefManager(context: Context) {

    // Nama file SharedPreferences (disesuaikan dengan kode lama Anda agar data tidak hilang)
    private val sessionPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val settingsPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val profilePref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

    // --- LOGIKA SESI (User Session) ---

    fun setLoginStatus(isLoggedIn: Boolean, username: String?) {
        sessionPref.edit().apply {
            putBoolean("is_logged_in", isLoggedIn)
            putString("current_user", username)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionPref.getBoolean("is_logged_in", false)
    }

    fun getCurrentUser(): String {
        return sessionPref.getString("current_user", "User") ?: "User"
    }

    fun logout() {
        sessionPref.edit().clear().apply()
    }

    // --- LOGIKA PENGATURAN (App Settings) ---

    fun saveThemeMode(mode: Int) {
        val user = getCurrentUser()
        settingsPref.edit().putInt("theme_mode_$user", mode).apply()
    }

    fun getThemeMode(): Int {
        val user = getCurrentUser()
        return settingsPref.getInt("theme_mode_$user", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // --- LOGIKA PROFIL & REGISTRASI (User Profile) ---

    fun registerUser(username: String, pass: String, joinDate: String) {
        profilePref.edit().apply {
            putString("username_$username", username)
            putString("password_$username", pass)
            putString("join_$username", joinDate)
            apply()
        }
    }

    fun getRegisteredPassword(username: String): String? {
        return profilePref.getString("password_$username", null)
    }

    fun isUsernameExists(username: String): Boolean {
        return profilePref.contains("username_$username")
    }

    fun getJoinDate(username: String): String {
        return profilePref.getString("join_$username", "-") ?: "-"
    }

    // --- LOGIKA PROFIL (Tambahan) ---

    fun saveProfileInfo(name: String, phone: String, email: String) {
        val user = getCurrentUser()
        profilePref.edit().apply {
            putString("name_$user", name)
            putString("phone_$user", phone)
            putString("email_$user", email)
            apply()
        }
    }

    fun saveProfileImage(uri: String) {
        val user = getCurrentUser()
        profilePref.edit().putString("image_$user", uri).apply()
    }

    fun getProfileName(default: String): String =
        profilePref.getString("name_${getCurrentUser()}", default) ?: default

    fun getProfilePhone(): String =
        profilePref.getString("phone_${getCurrentUser()}", "-") ?: "-"

    fun getProfileEmail(): String =
        profilePref.getString("email_${getCurrentUser()}", "-") ?: "-"

    fun getProfileImage(): String? =
        profilePref.getString("image_${getCurrentUser()}", null)
}