package com.example.rabu.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class PrefManager(context: Context) {

    private val sessionPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val settingsPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val profilePref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

    // --- LOGIKA SESI (User Session) ---

    /**
     * Menyimpan status login dan UID unik dari Firebase.
     * userId di sini akan berisi UID dari auth.currentUser?.uid
     */
    fun setLoginStatus(isLoggedIn: Boolean, userId: String?) {
        sessionPref.edit().apply {
            putBoolean("is_logged_in", isLoggedIn)
            putString("current_user", userId)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sessionPref.getBoolean("is_logged_in", false)
    }

    /**
     * Mengambil UID user yang sedang aktif.
     * UID ini digunakan sebagai kunci unik untuk data lokal lainnya.
     */
    fun getCurrentUser(): String {
        return sessionPref.getString("current_user", "Guest") ?: "Guest"
    }

    fun logout() {
        sessionPref.edit().clear().apply()
    }

    // --- LOGIKA PENGATURAN (App Settings) ---

    fun saveThemeMode(mode: Int) {
        val uid = getCurrentUser()
        settingsPref.edit().putInt("theme_mode_$uid", mode).apply()
    }

    fun getThemeMode(): Int {
        val uid = getCurrentUser()
        return settingsPref.getInt("theme_mode_$uid", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // --- LOGIKA PROFIL (Local Cache) ---

    fun saveProfileInfo(name: String, email: String) {
        val uid = getCurrentUser()
        profilePref.edit().apply {
            putString("name_$uid", name)
            putString("email_$uid", email)
            apply()
        }
    }

    fun saveProfileImage(uri: String) {
        val uid = getCurrentUser()
        profilePref.edit().putString("image_$uid", uri).apply()
    }

    fun getProfileName(default: String): String =
        profilePref.getString("name_${getCurrentUser()}", default) ?: default

    fun getProfileEmail(): String =
        profilePref.getString("email_${getCurrentUser()}", "-") ?: "-"

    fun getProfileImage(): String? =
        profilePref.getString("image_${getCurrentUser()}", null)

    fun getJoinDate(): String {
        val uid = getCurrentUser()
        return profilePref.getString("join_$uid", "-") ?: "-"
    }

    fun saveJoinDate(date: String) {
        val uid = getCurrentUser()
        profilePref.edit().putString("join_$uid", date).apply()
    }
}