package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.StreamApplication
import com.example.data.model.*
import com.example.data.repository.StreamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class StreamViewModel(
    application: Application,
    private val repository: StreamRepository
) : AndroidViewModel(application) {

    // Current signed-in user session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Login progress state
    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Repository flows exposed to UI
    val allVideos: StateFlow<List<Video>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<Post>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFollows: StateFlow<List<Follow>> = repository.allFollows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live chat simulator job list
    private var chatSimulationJob: Job? = null

    // Pre-allocated mock chat comments pool to simulate realistic live streaming
    private val mockViewerNames = listOf(
        "GamerPro_44", "HyperNinja", "TwitchViewer", "LofiPanda", 
        "CodeAndroid", "SpeedRunner", "ApexPredator", "FahadFan", 
        "BrightStar", "NeonRacer", "PixelArtist", "SleekCoder"
    )
    private val mockViewerAvatars = listOf(
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=100&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=100&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=100&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=100&auto=format&fit=crop"
    )
    private val mockGamingComments = listOf(
        "OMG THIS MATCH IS SO CLOSE!", "WOW, clutch play right there!", 
        "Can we get some 'W's in the chat?", "Nice headshot!! 🔥", 
        "unreal reflexes holy cow", "Where can I buy the streamer merchandise?",
        "Subscribe to BrightnessWorld on YT, guys!", "FBRODCUST performance is super smooth",
        "Apex Esports is unmatched this season!", "Fahad is cooking with these compose animations"
    )
    private val mockMusicComments = listOf(
        "This retro bass is absolutely slapping.", "Lofi synthwave = perfect coding ambiance.",
        "Beautiful aesthetics in the background stream art.", "Who is the artist for this beat?",
        "Coding is 10x faster with this live broadcast.", "Calm, composed, and cozy. Love it",
        "greetings from Bangladesh! 🇧🇩", "Drop the SoundCloud album link!",
        "Perfect stream playlist, literally never skipping."
    )
    private val mockBDTVComments = listOf(
        "যমুনা টিভির রিপোর্টিং দারুণ! 🇧🇩",
        "Somoy TV has the fastest breaking news updates.",
        "চ্যানেল আই-এর নাটকগুলো আসলেই অসাধারণ।",
        "BTV brings back so many childhood memories! 📺",
        "FBRODCUST app streaming speed is fantastic!",
        "সরাসরি খবর দেখার সুযোগ দেওয়ার জন্য ধন্যবাদ ফাহাদ ভাই",
        "Deepto TV's dubbed cartoons are legendary haha",
        "ATN Bangla brings World Class programs",
        "I am watching this live from Sylhet! 💚",
        "Greetings from Dhaka! Excellent streaming clarity."
    )
    private val mockIntlTVComments = listOf(
        "Al Jazeera coverage is brilliant and in-depth. 🌍",
        "NASA Live Stream is literally out of this world! 🚀",
        "France 24 delivers great insights on European politics.",
        "DW News has incredibly futuristic science segments.",
        "Sky News coverage of raw breaking news is unmatched.",
        "FBRODCUST app is so smooth, absolutely zero buffering!",
        "Watching NASA live rocket prep from London! 🛰️",
        "This live news broadcast is high quality.",
        "World news keeps me updated on everything!"
    )

    init {
        viewModelScope.launch {
            // Ensure first-launch seed mock data exists
            repository.seedMockDataIfEmpty()
        }
    }

    // Google Sign-In action simulator
    fun loginWithGoogle(name: String, email: String, avatarUrl: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            delay(1500) // Realistic loading delay for authenticator dialog
            val user = User(
                id = "google_user_${System.currentTimeMillis()}",
                name = name,
                email = email,
                avatarUrl = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150&auto=format&fit=crop" }
            )
            _currentUser.value = user
            _isLoggingIn.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        stopChatSimulation()
    }

    fun updateUserProfile(name: String, avatarUrl: String) {
        _currentUser.value = _currentUser.value?.copy(
            name = name,
            avatarUrl = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150&auto=format&fit=crop" }
        )
    }

    // Like buttons
    fun toggleLikeVideo(videoId: String) {
        viewModelScope.launch {
            repository.toggleLikeVideo(videoId)
        }
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    // Follower list tracker
    fun toggleFollowCreator(creatorId: String) {
        viewModelScope.launch {
            repository.toggleFollowCreator(creatorId)
        }
    }

    // Comment lists helper flows
    fun getCommentsFlow(targetId: String, isLiveChat: Boolean): Flow<List<Comment>> {
        return repository.getCommentsFlow(targetId, isLiveChat)
    }

    // Send chat (live) or comment (post/video)
    fun sendComment(targetId: String, content: String, isLiveChat: Boolean) {
        if (content.isBlank()) return
        val user = _currentUser.value ?: User("guest", "Anonymous Guest", "guest@streamcast.com", "")
        viewModelScope.launch {
            repository.addComment(
                targetId = targetId,
                authorName = user.name,
                authorAvatar = user.avatarUrl,
                content = content,
                isLiveChat = isLiveChat
            )
        }
    }

    // Publish section
    fun publishPost(content: String, imageUrl: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.publishUserPost(
                content = content,
                authorName = user.name,
                authorAvatar = user.avatarUrl,
                imageUrl = imageUrl
            )
        }
    }

    fun publishVideo(
        title: String,
        description: String,
        isLive: Boolean,
        videoUrl: String,
        thumbnailUrl: String,
        category: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.publishUserVideo(
                title = title,
                description = description,
                isLive = isLive,
                authorName = user.name,
                authorAvatar = user.avatarUrl,
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                category = category
            )
        }
    }

    // Live chat generator loop whenever user is on the Live Stream view
    fun startChatSimulation(videoId: String, categoryName: String) {
        stopChatSimulation()
        chatSimulationJob = viewModelScope.launch {
            while (true) {
                delay(3000L + Random.nextLong(2000L, 5000L)) // Message every 5-10 seconds
                val commentPool = when {
                    categoryName.contains("Bangladesh", ignoreCase = true) || categoryName.contains("BD TV", ignoreCase = true) -> mockBDTVComments
                    categoryName.contains("Music", ignoreCase = true) -> mockMusicComments
                    else -> mockIntlTVComments
                }
                val mockAuthor = mockViewerNames.random()
                val mockAvatar = mockViewerAvatars.random()
                val mockContent = commentPool.random()

                repository.addComment(
                    targetId = videoId,
                    authorName = mockAuthor,
                    authorAvatar = mockAvatar,
                    content = mockContent,
                    isLiveChat = true
                )
            }
        }
    }

    fun stopChatSimulation() {
        chatSimulationJob?.cancel()
        chatSimulationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopChatSimulation()
    }
}

// ViewModel Factory Provider to correctly supply Repository context
class StreamViewModelFactory(
    private val application: Application,
    private val repository: StreamRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StreamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StreamViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
