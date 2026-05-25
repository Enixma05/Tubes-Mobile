package com.example.rabu

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerBuku: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerBuku = findViewById(R.id.recyclerBuku)

        val listBuku = listOf(
            Buku("Atomic Habits", "Buku tentang membangun kebiasaan kecil yang berdampak besar.", 70, "Sedang dibaca"),
            Buku("Laut Bercerita", "Novel karya Leila S. Chudori tentang perjuangan dan kehilangan.", 45, "Sedang dibaca"),
            Buku("Rich Dad Poor Dad", "Buku mengenai pengelolaan keuangan dan pola pikir finansial.", 90, "Sudah dibaca")
        )

        bukuAdapter = BukuAdapter(listBuku)
        recyclerBuku.layoutManager = LinearLayoutManager(this)
        recyclerBuku.adapter = bukuAdapter

        // ← Tambahkan stats di bawah ini
        findViewById<TextView>(R.id.tvTotalBuku).text = listBuku.size.toString()
        findViewById<TextView>(R.id.tvSedangBaca).text = listBuku.count { it.status == "Sedang dibaca" }.toString()
        findViewById<TextView>(R.id.tvSelesai).text = listBuku.count { it.status == "Sudah dibaca" }.toString()

        // Greeting
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 11 -> "Selamat pagi! ☀️"
            hour < 15 -> "Selamat siang! 🌤"
            hour < 18 -> "Selamat sore! 🌥"
            else -> "Selamat malam! 🌙"
        }
        findViewById<TextView>(R.id.txtGreeting).text = greeting
    }
}