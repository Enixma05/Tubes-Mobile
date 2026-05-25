package com.example.rabu

data class Buku(
    val judul: String,
    val deskripsi: String,
    val progress: Int,
    val status: String = "Sedang dibaca"
)