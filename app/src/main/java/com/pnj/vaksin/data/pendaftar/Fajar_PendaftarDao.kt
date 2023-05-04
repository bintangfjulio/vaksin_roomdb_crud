package com.pnj.vaksin.data.pendaftar

import androidx.room.*

// buat method via Data Access Object untuk interaksi dengan table pendaftar
@Dao
interface Fajar_PendaftarDao {
    @Query("SELECT * FROM pendaftar WHERE nama_pendaftar LIKE :namaPendaftar")
    suspend fun searchPendaftar(namaPendaftar: String) : List<Fajar_Pendaftar>

    @Insert
    suspend fun addPendaftar(pendaftar: Fajar_Pendaftar)

    @Update(entity = Fajar_Pendaftar::class)
    suspend fun updatePendaftar(pendaftar: Fajar_Pendaftar)

    @Delete
    suspend fun deletePendaftar(pendaftar: Fajar_Pendaftar)

    @Query("SELECT * FROM pendaftar")
    suspend fun getAllPendaftar(): List<Fajar_Pendaftar>
}