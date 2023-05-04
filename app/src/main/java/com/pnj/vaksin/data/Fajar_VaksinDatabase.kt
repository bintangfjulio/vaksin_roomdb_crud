package com.pnj.vaksin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pnj.vaksin.data.pendaftar.Fajar_Pendaftar
import com.pnj.vaksin.data.pendaftar.Fajar_PendaftarDao

// Setup & Build Room Database
@Database(entities = [Fajar_Pendaftar::class], version = 1)
abstract class Fajar_VaksinDatabase : RoomDatabase() {

    abstract fun getPendaftarDao(): Fajar_PendaftarDao // Bridge access dao

    companion object{
        @Volatile
        private var instance: Fajar_VaksinDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            Fajar_VaksinDatabase::class.java,
            "vaksin-db"
        ).build()
    }
}
