package com.example.rabu

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

class BukuAdapter(
    private val listBuku: MutableList<Buku>,
    private val onItemClick: (Buku) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<BukuAdapter.BukuViewHolder>() {

    class BukuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtJudul: TextView = itemView.findViewById(R.id.txtJudul)
        val txtDeskripsi: TextView = itemView.findViewById(R.id.txtDeskripsi)
        val progressBaca: ProgressBar = itemView.findViewById(R.id.progressBaca)
        val txtProgress: TextView = itemView.findViewById(R.id.txtProgress)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val btnMenu: ImageButton = itemView.findViewById(R.id.btnMenu)
        val ivBuku: ImageView = itemView.findViewById(R.id.ivBuku)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BukuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_buku, parent, false)
        return BukuViewHolder(view)
    }

    override fun onBindViewHolder(holder: BukuViewHolder, position: Int) {
        val buku = listBuku[position]

        holder.txtJudul.text = buku.judul
        holder.txtDeskripsi.text = buku.deskripsi
        holder.progressBaca.progress = buku.progress

        // ✅ SET TEKS STATUS (Agar sinkron dengan data)
        holder.txtStatus.text = buku.status

        val halaman = buku.halamanTerakhir.filter { it.isDigit() }.ifEmpty { "0" }
        holder.txtProgress.text = "$halaman / ${buku.jumlahHalaman} (${buku.progress}%)"

        if (!buku.coverUri.isNullOrEmpty()) {
            holder.ivBuku.setImageURI(Uri.parse(buku.coverUri))
            holder.ivBuku.setPadding(0, 0, 0, 0)
        } else {
            holder.ivBuku.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.ivBuku.setPadding(30, 30, 30, 30)
        }

        // Sinkronisasi warna badge status
        when (buku.status) {
            "Sudah dibaca" -> holder.txtStatus.setBackgroundResource(R.drawable.bg_badge_selesai)
            "Sedang dibaca" -> holder.txtStatus.setBackgroundResource(R.drawable.bg_badge_sedang)
            else -> holder.txtStatus.setBackgroundResource(R.drawable.bg_badge_belum)
        }

        holder.itemView.setOnClickListener { onItemClick(buku) }

        holder.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Hapus")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Hapus") onDeleteClick(holder.bindingAdapterPosition)
                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = listBuku.size
}
