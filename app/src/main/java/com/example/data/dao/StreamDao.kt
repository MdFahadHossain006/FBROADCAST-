package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // --- Video/Livestream Queries ---
    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideosFlow(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getVideoByIdFlow(videoId: String): Flow<Video?>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): Video?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<Video>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)

    @Update
    suspend fun updateVideo(video: Video)

    // --- Post Queries ---
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    // --- Comment/Live Chat Queries ---
    @Query("SELECT * FROM comments WHERE targetId = :targetId AND isLiveChat = :isLiveChat ORDER BY timestamp ASC")
    fun getCommentsFlow(targetId: String, isLiveChat: Boolean): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    // --- Follow Queries ---
    @Query("SELECT * FROM follows")
    fun getAllFollowsFlow(): Flow<List<Follow>>

    @Query("SELECT isFollowing FROM follows WHERE creatorId = :creatorId")
    suspend fun isFollowing(creatorId: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: Follow)

    @Delete
    suspend fun deleteFollow(follow: Follow)
}
