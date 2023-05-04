package com.pnj.vaksin.pendaftar

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pnj.vaksin.Fajar_HomeActivity
import com.pnj.vaksin.R
import com.pnj.vaksin.data.Fajar_VaksinDatabase
import com.pnj.vaksin.data.pendaftar.Fajar_Pendaftar
import com.pnj.vaksin.databinding.FragmentFajarAddPendaftarBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Fajar_AddPendaftarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Fajar_AddPendaftarFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFajarAddPendaftarBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 100
    private var dataGambar: Bitmap? = null
    private var saved_image_url: String = ""

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFajarAddPendaftarBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun addPendaftar() {
        val nama_pendaftar = binding.TxtNama.text.toString()
        val nik_pendaftar = binding.TxtNIK.text.toString().toInt()
        val umur_pendaftar = binding.TxtUmur.text.toString().toInt()

        val id_kelamin = binding.radioGroupKelamin.checkedRadioButtonId
        val radioButton_kelamin = binding.root.findViewById<RadioButton>(id_kelamin)
        val jenis_kelamin_pendaftar = radioButton_kelamin.text.toString()

        val id_penyakit = binding.radioGroupPenyakit.checkedRadioButtonId
        val radioButton_penyakit = binding.root.findViewById<RadioButton>(id_penyakit)
        val penyakit_pendaftar = radioButton_penyakit.text.toString()

        lifecycleScope.launch {
            // constructor on column info
            val pendaftar = Fajar_Pendaftar(saved_image_url, nik_pendaftar, nama_pendaftar, umur_pendaftar, jenis_kelamin_pendaftar, penyakit_pendaftar)
            Fajar_VaksinDatabase(requireContext()).getPendaftarDao().addPendaftar(pendaftar) // insert database with dao
        }
        dismiss()
    }

    fun saveMediaToStorage(bitmap: Bitmap): String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null
        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            activity?.contentResolver?.also { resolver ->
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
            val permission = ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if(permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
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
        if (requestCode == REQ_CAM && resultCode == AppCompatActivity.RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            binding.BtnImgPendaftar.setImageBitmap(dataGambar)
            saved_image_url = image_save_uri
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.BtnImgPendaftar.setOnClickListener{
            openCamera()
        }

        binding.BtnAddPendaftar.setOnClickListener {
            if(saved_image_url != ""){
                addPendaftar()
            }
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.activity?.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    // Refresh
    override fun onDetach() {
        super.onDetach()
        (activity as Fajar_HomeActivity?)?.loadDataPendaftar()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Fajar_AddPendaftarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Fajar_AddPendaftarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}