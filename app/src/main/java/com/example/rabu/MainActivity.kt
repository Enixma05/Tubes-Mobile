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
                "Buku tentang membangun kebiasaan kecil.",
                70
            ),
            Buku(
                "Laut Bercerita",
                "Novel karya Leila S. Chudori.",
                45
            ):wq
        )

        bukuAdapter = BukuAdapter(listBuku)

        recyclerBuku.layoutManager = LinearLayoutManager(this)
        recyclerBuku.adapter = bukuAdapter
    }
}