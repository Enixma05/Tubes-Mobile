package com.example.rabu

import android.os.Bundle
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
            Buku(
                "Atomic Habits",
                "Buku tentang membangun kebiasaan kecil yang berdampak besar.",
                70
            ),
            Buku(
                "Laut Bercerita",
                "Novel karya Leila S. Chudori tentang perjuangan dan kehilangan.",
                45
            ),
            Buku(
                "Rich Dad Poor Dad",
                "Buku mengenai pengelolaan keuangan dan pola pikir finansial.",
                90
            )
        )

        bukuAdapter = BukuAdapter(listBuku)

        recyclerBuku.layoutManager = LinearLayoutManager(this)
        recyclerBuku.adapter = bukuAdapter
    }
}