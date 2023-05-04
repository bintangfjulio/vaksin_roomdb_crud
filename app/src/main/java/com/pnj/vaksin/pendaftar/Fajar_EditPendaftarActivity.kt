package com.pnj.vaksin.pendaftar

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.pnj.vaksin.data.Fajar_VaksinDatabase
import com.pnj.vaksin.databinding.ActivityFajarEditPendaftarBinding
import java.io.File
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.pnj.vaksin.Fajar_HomeActivity
import com.pnj.vaksin.R
import com.pnj.vaksin.data.pendaftar.Fajar_Pendaftar
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.OutputStream

class Fajar_EditPendaftarActivity : AppCompatActivity() {
    private var _binding: ActivityFajarEditPendaftarBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var dataGambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""

    private var id_pendaftar: Int = 0

    lateinit var vaksinDB: Fajar_VaksinDatabase
    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityFajarEditPendaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaksinDB = Fajar_VaksinDatabase(this@Fajar_EditPendaftarActivity)

        val intent = intent
        binding.TxtEditNama.setText(intent.getStringExtra("nama_pendaftar").toString())
        binding.TxtEditNIK.setText(intent.getStringExtra("nik_pendaftar").toString())
        binding.TxtEditUmur.setText(intent.getStringExtra("umur_pendaftar").toString())

        id_pendaftar = intent.getStringExtra("id").toString().toInt()

        old_foto_dir = intent.getStringExtra("foto_pendaftar").toString()

        val imgFile = File("${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

        if(!imgFile.exists()) {
            binding.BtnImgPendaftar.setImageResource(R.drawable.pendaftar_default)
        }

        else {
            binding.BtnImgPendaftar.setImageBitmap(myBitmap)
        }

        val kelamin = intent.getStringExtra("jenis_kelamin_pendaftar").toString()
        val radioButton_kelamin = binding.root.findViewWithTag<RadioButton>(kelamin)
        radioButton_kelamin.isChecked = true

        val penyakit = intent.getStringExtra("penyakit_bawaan_pendaftar").toString()
        val radioButton_penyakit = binding.root.findViewWithTag<RadioButton>(penyakit)
        radioButton_penyakit.isChecked = true

        if(!checkPermission()) {
            requestPermission()
        }

        binding.BtnImgPendaftar.setOnClickListener{
            openCamera()
        }

        binding.BtnEditPendaftar.setOnClickListener {
            editPendaftar()
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

    fun saveMediaToStorage(bitmap: Bitmap): String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null
        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply{
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        }

        // DCIM
        else{
            val permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if(permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE)
            }

            val imageDir : String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera/"
            Log.e("Image Directory", imageDir)
            Log.e("File Name", filename)

            val image = File(imageDir, filename)
            fos = FileOutputStream(image)

            image_save = imageDir + filename
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQ_CAM && resultCode == RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            new_foto_dir = image_save_uri
            binding.BtnImgPendaftar.setImageBitmap(dataGambar)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    private fun editPendaftar() {
        val nama_pendaftar = binding.TxtEditNama.text.toString()
        val nik_pendaftar = binding.TxtEditNIK.text.toString().toInt()
        val umur_pendaftar = binding.TxtEditUmur.text.toString().toInt()

        val id_kelamin = binding.radioGroupEditKelamin.checkedRadioButtonId
        val radioButton_kelamin = binding.root.findViewById<RadioButton>(id_kelamin)
        val jenis_kelamin_pendaftar = radioButton_kelamin.text.toString()

        val id_penyakit = binding.radioGroupEditPenyakit.checkedRadioButtonId
        val radioButton_penyakit = binding.root.findViewById<RadioButton>(id_penyakit)
        val penyakit_pendaftar = radioButton_penyakit.text.toString()

        var foto_final_dir : String = old_foto_dir

        if(old_foto_dir != new_foto_dir){
            if(new_foto_dir != "") {
                foto_final_dir = new_foto_dir

                val old_foto_delete = File(old_foto_dir)

                if (old_foto_delete.exists()) {
                    if (old_foto_delete.delete()) {
                        Log.e("foto lama dihapus, diganti dengan", new_foto_dir)
                    }
                }
            }

            else{
                foto_final_dir = old_foto_dir
                Log.e("foto tidak diganti, masih", old_foto_dir)
            }

            lifecycleScope.launch{
                val pendaftar = Fajar_Pendaftar(foto_final_dir, nik_pendaftar, nama_pendaftar, umur_pendaftar, jenis_kelamin_pendaftar, penyakit_pendaftar)
                pendaftar.id = id_pendaftar
                vaksinDB.getPendaftarDao().updatePendaftar(pendaftar)
            }

            val intentPendaftar = Intent(this, Fajar_HomeActivity::class.java)
            startActivity(intentPendaftar)
        }
    }
}