package com.pnj.vaksin.data.pendaftar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Atur table beserta column data & primary keynya pada room database
@Entity(tableName = "pendaftar")

data class Fajar_Pendaftar(
    @ColumnInfo(name = "foto_pendaftar") var foto_pendaftar: String = "",
    @ColumnInfo(name = "nik_pendaftar") var nik_pendaftar: Int = 0,
    @ColumnInfo(name = "nama_pendaftar") var nama_pendaftar: String = "",
    @ColumnInfo(name = "umur_pendaftar") var umur_pendaftar: Int = 0,
    @ColumnInfo(name = "jenis_kelamin_pendaftar") var jenis_kelamin_pendaftar: String = "",
    @ColumnInfo(name = "penyakit_bawaan_pendaftar") var penyakit_bawaan_pendaftar: String = "",
) : Serializable {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
