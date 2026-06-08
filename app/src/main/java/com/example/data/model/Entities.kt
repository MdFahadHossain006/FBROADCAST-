package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String,
    val bio: String = "Content Creator & Streamer"
)

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val likesCount: Int,
    val viewsCount: Int,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isLive: Boolean = false,
    val category: String = "Gaming"
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val content: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetId: String, // References videoId or postId
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveChat: Boolean = false
)

@Entity(tableName = "follows")
data class Follow(
    @PrimaryKey val creatorId: String,
    val isFollowing: Boolean = true
)
