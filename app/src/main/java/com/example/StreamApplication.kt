package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.database.StreamDatabase
import com.example.data.repository.StreamRepository

class StreamApplication : Application() {

    lateinit var database: StreamDatabase
        private set

    lateinit var repository: StreamRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            StreamDatabase::class.java,
            "stream_cast_database"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = StreamRepository(database.streamDao())
    }
}
