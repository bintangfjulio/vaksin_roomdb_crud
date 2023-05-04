package com.pnj.vaksin.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.pnj.vaksin.R
import com.pnj.vaksin.data.pendaftar.Fajar_Pendaftar
import com.pnj.vaksin.pendaftar.Fajar_EditPendaftarActivity
import java.io.File

// Adaptasi view dengan recycler pada adapter & holder position
class Fajar_PendaftarAdapter(private val pendaftarList: ArrayList<Fajar_Pendaftar>) :
    RecyclerView.Adapter<Fajar_PendaftarAdapter.Fajar_PendaftarViewHolder>() {
    private lateinit var activity: AppCompatActivity

    class Fajar_PendaftarViewHolder(pendaftarItemView: View) :
        RecyclerView.ViewHolder(pendaftarItemView) {
        val nama_pendaftar: TextView = pendaftarItemView.findViewById(R.id.TVLNamaPendaftar)
        val jeniskelamin_pendaftar: TextView =
            pendaftarItemView.findViewById(R.id.TVLJenisKelaminPendaftar)
        val umur_pendaftar: TextView = pendaftarItemView.findViewById(R.id.TVLUmurPendaftar)
        val img_pendaftar: ImageView = itemView.findViewById(R.id.IMLGambarPendaftar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fajar_PendaftarViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.pendaftar_list_layout, parent, false)
        return Fajar_PendaftarViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return pendaftarList.size
    }

    override fun onBindViewHolder(holder: Fajar_PendaftarViewHolder, position: Int) {
        val currentItem = pendaftarList[position]
        val foto_dir = currentItem.foto_pendaftar.toString()
        val imgFile = File("${foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

        if(!imgFile.exists()) {
            holder.img_pendaftar.setImageResource(R.drawable.pendaftar_default)
        }

        else {
            holder.img_pendaftar.setImageBitmap(myBitmap)
        }

        holder.nama_pendaftar.text = "Nama: " + currentItem.nama_pendaftar.toString()
        holder.jeniskelamin_pendaftar.text = "Gender: " + currentItem.jenis_kelamin_pendaftar.toString()
        holder.umur_pendaftar.text = "Umur: " + currentItem.umur_pendaftar.toString() + " tahun"

        holder.itemView.setOnClickListener {
            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, Fajar_EditPendaftarActivity::class.java).apply{
                putExtra("nama_pendaftar", currentItem.nama_pendaftar.toString())
                putExtra("foto_pendaftar", currentItem.foto_pendaftar.toString())
                putExtra("nik_pendaftar", currentItem.nik_pendaftar.toString())
                putExtra("umur_pendaftar", currentItem.umur_pendaftar.toString())
                putExtra("jenis_kelamin_pendaftar", currentItem.jenis_kelamin_pendaftar.toString())
                putExtra("penyakit_bawaan_pendaftar", currentItem.penyakit_bawaan_pendaftar.toString())
                putExtra("id", currentItem.id.toString())
            })
        }
    }
}