package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.dao.StreamDao
import com.example.data.model.*

@Database(
    entities = [User::class, Video::class, Post::class, Comment::class, Follow::class],
    version = 2,
    exportSchema = false
)
abstract class StreamDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao
}
