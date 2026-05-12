package com.example.rabu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BukuAdapter(private val listBuku: List<Buku>) :
    RecyclerView.Adapter<BukuAdapter.BukuViewHolder>() {

    class BukuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtJudul: TextView = itemView.findViewById(R.id.txtJudul)
        val txtDeskripsi: TextView = itemView.findViewById(R.id.txtDeskripsi)
        val progressBaca: ProgressBar = itemView.findViewById(R.id.progressBaca)
        val txtProgress: TextView = itemView.findViewById(R.id.txtProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BukuViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_buku, parent, false)

        return BukuViewHolder(view)
    }

    override fun onBindViewHolder(holder: BukuViewHolder, position: Int) {

        val buku = listBuku[position]

        holder.txtJudul.text = buku.judul
        holder.txtDeskripsi.text = buku.deskripsi
        holder.progressBaca.progress = buku.progress
        holder.txtProgress.text = "${buku.progress}% selesai"
    }

    override fun getItemCount(): Int {
        return listBuku.size
    }
}