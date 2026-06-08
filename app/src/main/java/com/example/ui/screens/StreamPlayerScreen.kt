package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Comment
import com.example.data.model.Video
import com.example.ui.viewmodel.StreamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class, UnstableApi::class)
@Composable
fun StreamPlayerScreen(
    videoId: String,
    viewModel: StreamViewModel,
    onBackClick: () -> Unit
) {
    val videos by viewModel.allVideos.collectAsState()
    val follows by viewModel.allFollows.collectAsState()
    
    // Find matching video/stream in list
    val video = videos.find { it.id == videoId }
    val isFollowing = video?.let { v -> follows.any { it.creatorId == v.creatorId } } ?: false

    // Chat comments list Flow
    val chatComments by viewModel.getCommentsFlow(videoId, isLiveChat = true).collectAsState(initial = emptyList())
    val chatListState = rememberLazyListState()

    var isPlaying by remember { mutableStateOf(true) }
    var isFullScreen by remember { mutableStateOf(false) } // Fullscreen state toggler
    var showControls by remember { mutableStateOf(true) } // Premium Auto-fade/Tap toggle controls
    var chatMessageText by remember { mutableStateOf("") }
    var descriptionExpanded by remember { mutableStateOf(false) }

    // Start Chat Simulation on launch, stop on exit
    LaunchedEffect(videoId, video) {
        if (video != null && video.isLive) {
            viewModel.startChatSimulation(videoId, video.category)
        }
    }

    // Auto-scroll chat to latest message whenever comment list changes size
    LaunchedEffect(chatComments.size) {
        if (chatComments.isNotEmpty()) {
            chatListState.animateScrollToItem(chatComments.size - 1)
        }
    }

    // Premium Player Auto-fade system in Fullscreen
    LaunchedEffect(showControls, isPlaying, isFullScreen) {
        if (isFullScreen && showControls && isPlaying) {
            delay(4000L)
            showControls = false
        }
    }

    // Reset controls to visible when exiting fullscreen
    LaunchedEffect(isFullScreen) {
        if (!isFullScreen) {
            showControls = true
        }
    }

    if (video == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131116)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFD0BCFF))
        }
    } else {
        val context = LocalContext.current
        var isPlayerLoading by remember { mutableStateOf(true) }

        // Keep Screen On during playback
        DisposableEffect(Unit) {
            var currentContext = context
            var activity: android.app.Activity? = null
            while (currentContext is android.content.ContextWrapper) {
                if (currentContext is android.app.Activity) {
                    activity = currentContext
                    break
                }
                currentContext = currentContext.baseContext
            }
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        // Share single player across transitions fluidly with elite buffer tuning
        val exoPlayer = remember(video.videoUrl) {
            val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    1500, // minBufferMs
                    5000, // maxBufferMs
                    1000, // bufferForPlaybackMs
                    1500  // bufferForPlaybackAfterRebufferMs
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build().apply {
                    val mediaItem = MediaItem.fromUri(video.videoUrl)
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = isPlaying
                    addListener(object : androidx.media3.common.Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            isPlayerLoading = (state == androidx.media3.common.Player.STATE_BUFFERING)
                        }
                    })
                }
        }

        DisposableEffect(video.videoUrl) {
            val mediaItem = MediaItem.fromUri(video.videoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
            onDispose { }
        }

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        LaunchedEffect(isPlaying) {
            exoPlayer.playWhenReady = isPlaying
        }

        if (isFullScreen) {
            // ==========================================
            // 🎬 IMMERSIVE PREMIUM FULLSCREEN VIEW
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showControls = !showControls }
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            setBackgroundColor(0xFF000000.toInt())
                            keepScreenOn = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isPlayerLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                    }
                }

                // Dark vignetting protective layers for controls (only when controls are showable)
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x66000000), Color(0x00000000), Color(0x99000000)),
                                    startY = 0f
                                )
                            )
                    )
                }

                // 🔙 Floating Back Navigation Button on top corner (doesn't interfere with video)
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(animationSpec = tween(250)) + slideInVertically(animationSpec = tween(250), initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = tween(250)) + slideOutVertically(animationSpec = tween(250), targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp)
                ) {
                    IconButton(
                        onClick = { isFullScreen = false },
                        modifier = Modifier
                            .background(Color(0x99000000), CircleShape)
                            .size(44.dp)
                            .testTag("fullscreen_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Immersive Bottom Floating Control Pane
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(animationSpec = tween(250)) + slideInVertically(animationSpec = tween(250), initialOffsetY = { it }),
                    exit = fadeOut(animationSpec = tween(250)) + slideOutVertically(animationSpec = tween(250), targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xE60D0B0F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clickable(enabled = true, onClick = {}), // Consumes tap to avoid toggling controls off on click
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle play pause",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Perfect cinematic negative space (replaces the seek progress line bar)
                        Spacer(modifier = Modifier.weight(1f))

                        // We have completely hidden the "LIVE STREAM" time badge here as requested!
                        
                        // Back to window mode button
                        IconButton(
                            onClick = { isFullScreen = false },
                            modifier = Modifier.size(32.dp).testTag("fullscreen_exit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // 📺 STANDARD SPLIT VIEW LAYOUT
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF131116))
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (video.isLive) "LIVESTREAM PLAYER" else "VIDEO STATION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.5.sp
                    )
                }

                // ExoPlayer Display Container block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                setBackgroundColor(0xFF000000.toInt())
                                keepScreenOn = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isPlayerLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0x80000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFD0BCFF))
                        }
                    }

                    // Shadow Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x22000000), Color(0xAA000000)),
                                    startY = 140f
                                )
                            )
                    )

                    // Control Row (Play, Pause, Progress Seek, Fullscreen toggle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle play pause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (isPlaying) 0.65f else 0.3f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFD0BCFF), RoundedCornerShape(2.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (video.isLive) "LIVE" else "12:44 / 24:32",
                            color = if (video.isLive) Color(0xFFB3261E) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Full Screen Toggler Button
                        IconButton(
                            onClick = { isFullScreen = true },
                            modifier = Modifier.size(24.dp).testTag("toggle_fullscreen_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Launch Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Content Metadata Pane
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatViewerCount(video.viewsCount)} viewers  •  Category: ${video.category}",
                            color = Color(0x66FFFFFF),
                            fontSize = 11.sp
                        )

                        Button(
                            onClick = { viewModel.toggleLikeVideo(video.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (video.isLiked) Color(0x30D0BCFF) else Color(0x11FFFFFF),
                                contentColor = if (video.isLiked) Color(0xFFD0BCFF) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (video.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like stream",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = video.likesCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1D23), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(video.creatorAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Publisher",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x11FFFFFF)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.creatorName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isFollowing) "Subscribed ✅" else "12.4K followers",
                                color = if (isFollowing) Color(0xFFF014E2) else Color(0x4DFFFFFF),
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleFollowCreator(video.creatorId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color(0xFF49454F) else Color(0xFFD0BCFF),
                                contentColor = if (isFollowing) Color(0xFFE6E1E5) else Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.height(28.dp).testTag("player_subscribe_btn")
                        ) {
                            Text(
                                text = if (isFollowing) "Subbed" else "Subscribe",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D23)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { descriptionExpanded = !descriptionExpanded }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Stream Broadcast Info",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (descriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (descriptionExpanded) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = video.description,
                                    color = Color(0x99FFFFFF),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0x14FFFFFF), thickness = 1.dp)

                // Comments feed section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF131116))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1D23))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFB3261E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (video.isLive) "LIVE CHAT FEED" else "STREAM DISCUSSION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("stream_chat_list"),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatComments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(comment.authorAvatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "User",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x11FFFFFF)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.authorName,
                                            color = if (comment.authorName.contains("Fahad")) Color(0xFFD0BCFF) else Color(0xFFCAC4D0),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "1:02 PM",
                                            color = Color(0x33FFFFFF),
                                            fontSize = 8.sp
                                        )
                                    }
                                    Text(
                                        text = comment.content,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .navigationBarsPadding()
                            .background(Color(0xFF1F1D23))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { 
                                Text(
                                    text = if (video.isLive) "Send a live chat..." else "Add a comment...",
                                    fontSize = 12.sp
                                ) 
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF131116),
                                unfocusedContainerColor = Color(0xFF131116),
                                focusedIndicatorColor = Color(0xFFD0BCFF)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    viewModel.sendComment(videoId, chatMessageText, isLiveChat = true)
                                    chatMessageText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Publish Chat Comment",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
