package com.pnj.vaksin

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pnj.vaksin.data.Fajar_VaksinDatabase
import com.pnj.vaksin.data.pendaftar.Fajar_Pendaftar
import com.pnj.vaksin.databinding.ActivityFajarHomeBinding
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pnj.vaksin.adapter.Fajar_PendaftarAdapter
import com.pnj.vaksin.pendaftar.Fajar_AddPendaftarFragment
import kotlinx.coroutines.launch
import java.io.File
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.core.widget.addTextChangedListener

class Fajar_HomeActivity : AppCompatActivity() {
    private var _binding: ActivityFajarHomeBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    lateinit var pendaftarRecyclerView: RecyclerView
    lateinit var vaksinDB: Fajar_VaksinDatabase

    lateinit var pendaftarList: ArrayList<Fajar_Pendaftar>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityFajarHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!checkPermission()) {
            requestPermission()
        }

        vaksinDB = Fajar_VaksinDatabase(this@Fajar_HomeActivity)

        loadDataPendaftar()

        binding.btnAddPendaftar.setOnClickListener {
            Fajar_AddPendaftarFragment().show(supportFragmentManager, "newPendaftarTag")
        }

        swipeDelete()

        binding.txtSearchPendaftar.addTextChangedListener {
            val keyword: String = "%${binding.txtSearchPendaftar.text.toString()}%"
            if (keyword.count() > 2){
                searchDataPendaftar(keyword)
            }
            else{
                loadDataPendaftar()
            }
        }
    }

    private fun checkPermission() : Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else{
            val write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try{
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            }
            catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    fun loadDataPendaftar() {
        var layoutManager = LinearLayoutManager(this)
        pendaftarRecyclerView = binding.pendaftarListView
        pendaftarRecyclerView.layoutManager = layoutManager
        pendaftarRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            pendaftarList = vaksinDB.getPendaftarDao().getAllPendaftar() as ArrayList<Fajar_Pendaftar>
            Log.e("List pendaftar", pendaftarList.toString())
            pendaftarRecyclerView.adapter = Fajar_PendaftarAdapter(pendaftarList)
        }
    }

    fun deletePendaftar(pendaftar: Fajar_Pendaftar, foto_delete: File) {
        val builder = AlertDialog.Builder(this@Fajar_HomeActivity)
        builder.setMessage("Apakah ${pendaftar.nama_pendaftar} ingin dihapus?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                lifecycleScope.launch {
                    vaksinDB.getPendaftarDao().deletePendaftar(pendaftar)

                    if(foto_delete.exists()){
                        if(foto_delete.delete()){
                            val toastDelete = Toast.makeText(applicationContext,
                                "data & foto deleted", Toast.LENGTH_LONG)
                            toastDelete.show()
                        }
                    }

                    loadDataPendaftar()
                }
            }
            .setNegativeButton("No"){ dialog, id ->
                dialog.dismiss()
                loadDataPendaftar()
            }
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete(){
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch{
                    pendaftarList = vaksinDB.getPendaftarDao().getAllPendaftar() as ArrayList<Fajar_Pendaftar>
                    Log.e("position swiped", pendaftarList[position].toString())
                    Log.e("position swiped", pendaftarList.size.toString())

                    val foto_delete = File(pendaftarList[position].foto_pendaftar)

                    deletePendaftar(pendaftarList[position], foto_delete)
                }
            }
        }).attachToRecyclerView(pendaftarRecyclerView)
    }

    fun searchDataPendaftar(keyword: String){
        var layoutManager = LinearLayoutManager(this)
        pendaftarRecyclerView = binding.pendaftarListView
        pendaftarRecyclerView.layoutManager = layoutManager
        pendaftarRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            pendaftarList = vaksinDB.getPendaftarDao().searchPendaftar(keyword) as ArrayList<Fajar_Pendaftar>
            Log.e("list pendaftar", pendaftarList.toString())
            pendaftarRecyclerView.adapter = Fajar_PendaftarAdapter(pendaftarList)
        }
    }
}
