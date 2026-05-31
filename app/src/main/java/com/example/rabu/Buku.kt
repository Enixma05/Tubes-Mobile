package com.example.rabu

import java.io.Serializable

data class Buku(
    val judul: String,
    val author: String,
    val penerbit: String,
    val jumlahHalaman: Int = 0,
    val genre: String,
    val deskripsi: String,
    val progress: Int,
    val status: String,
    val terakhirDibaca: String = "",
    val coverUri: String? = null
) : Serializable