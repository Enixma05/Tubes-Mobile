package com.example.rabu.data.model

import java.io.Serializable

data class Buku(
    val judul: String,
    val author: String,
    val penerbit: String,
    val jumlahHalaman: Int,
    val genre: String,
    val deskripsi: String,
    val progress: Int,
    val status: String,
    val halamanTerakhir: String,
    val coverUri: String?,
    val rating: Float = 0f,
    val notes: String = ""
) : Serializable