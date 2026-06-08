package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StreamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    viewModel: StreamViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    
    // Auth gate check: Only non-guest users contain actual Google profiles
    val isGoogleUser = user != null && user?.email?.startsWith("guest@") == false

    var selectedTab by remember { mutableStateOf(0) } // 0 = Live, 1 = Video, 2 = Post

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Gaming") }
    var contentText by remember { mutableStateOf("") }

    // Solution inputs: Custom URLs to fix upload issues
    var customVideoUrl by remember { mutableStateOf("") }
    var customThumbnailUrl by remember { mutableStateOf("") }
    var customPostImageUrl by remember { mutableStateOf("") }

    var isPublishingSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Default High Quality Unsplash seeds
    val defaultThumbnails = listOf(
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=300&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=300&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1498050108023-c5249f4df085?q=80&w=300&auto=format&fit=crop",
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=300&auto=format&fit=crop"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131116)) // Immersive unified theme color
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (!isGoogleUser) {
            // --- GLASSMORPHIC DECORATIVE AUTHENTICATION LOCKOUT GATE ---
            Spacer(modifier = Modifier.height(40.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x55FFFFFF), Color(0x11FFFFFF))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)), // Glassmorphism translucent look
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0x1AD0BCFF), CircleShape)
                            .border(1.dp, Color(0xFFD0BCFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Access Locked",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "GOOGLE AUTHENTICATION REQUIRED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Only creators authenticated via Google are authorized to broadcast Live TV streams, upload custom Video packages, or share Community posts.\n\nPlease log out and re-login with your Google account to unlock full broadcasting privileges.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = "Log out guest", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOGIN WITH GOOGLE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // --- FULL ACCESS BROADCASTER INTERFACE ---
            AnimatedVisibility(
                visible = isPublishingSuccess,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "Your Live Stream was launched successfully! 🎙️"
                                1 -> "Your Video masterclass was uploaded successfully! 📹"
                                else -> "Your community post was shared successfully! 💬"
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Create & Publish Content",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Broadcast live feeds, upload TV assets, or sync community notices with custom urls.",
                fontSize = 12.sp,
                color = Color(0x66FFFFFF),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF25232A),
                contentColor = Color(0xFFD0BCFF),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .padding(bottom = 20.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFD0BCFF),
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("LIVESTREAM", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Podcasts, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("publish_tab_stream")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("VIDEO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.VideoCameraBack, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("publish_tab_video")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("POST", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Feed, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("publish_tab_post")
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: LIVESTREAM FORM
                    Text(text = "Go Live Broadcast Feed Settings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Stream Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("stream_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description Outline") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().height(90.dp).padding(bottom = 12.dp).testTag("stream_description_input")
                    )

                    // NEW INPUTS: Custom Stream URLs
                    OutlinedTextField(
                        value = customVideoUrl,
                        onValueChange = { customVideoUrl = it },
                        label = { Text("Custom Video URL (HLS .m3u8 or MP4)") },
                        placeholder = { Text("e.g. http://server.com/live/playlist.m3u8") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("stream_url_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customThumbnailUrl,
                        onValueChange = { customThumbnailUrl = it },
                        label = { Text("Custom Poster/Thumbnail image URL (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("stream_thumb_input"),
                        singleLine = true
                    )

                    Text("Select Stream Category", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bangladesh", "Sports", "Music", "Kids", "Islamic", "News", "Gaming").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF25232A),
                                    labelColor = Color(0xFFCAC4D0),
                                    selectedContainerColor = Color(0xFFD0BCFF),
                                    selectedLabelColor = Color(0xFF381E72)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val resolvedVideo = customVideoUrl.ifBlank { "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" }
                                val resolvedThumb = customThumbnailUrl.ifBlank {
                                    defaultThumbnails[when (category) {
                                        "Gaming" -> 0
                                        "Music" -> 1
                                        else -> 3
                                    }]
                                }

                                viewModel.publishVideo(
                                    title = "🔴 LIVE: $title",
                                    description = description,
                                    isLive = true,
                                    videoUrl = resolvedVideo,
                                    thumbnailUrl = resolvedThumb,
                                    category = category
                                )
                                title = ""
                                description = ""
                                customVideoUrl = ""
                                customThumbnailUrl = ""
                                scope.launch {
                                    isPublishingSuccess = true
                                    delay(4000)
                                    isPublishingSuccess = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_stream_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Podcasts, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START LIVE BROADCAST", fontWeight = FontWeight.Bold)
                    }
                }

                1 -> {
                    // TAB 1: RECORDED VIDEO FORM
                    Text(text = "Upload Video Package", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("video_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description Details") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().height(90.dp).padding(bottom = 12.dp)
                    )

                    // SOLUTIONS FIELDS: Custom Upload URLs for Videos
                    OutlinedTextField(
                        value = customVideoUrl,
                        onValueChange = { customVideoUrl = it },
                        label = { Text("Direct Link to Video File (.mp4 / .mkv / .m3u8)") },
                        placeholder = { Text("e.g. https://domain.com/assets/video.mp4") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("video_url_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customThumbnailUrl,
                        onValueChange = { customThumbnailUrl = it },
                        label = { Text("Direct Link to Cover Image/Thumbnail (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("video_thumb_input"),
                        singleLine = true
                    )

                    Text("Select Channel Category", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bangladesh", "Sports", "Music", "Kids", "Islamic", "News", "Gaming").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF25232A),
                                    labelColor = Color(0xFFCAC4D0),
                                    selectedContainerColor = Color(0xFFD0BCFF),
                                    selectedLabelColor = Color(0xFF381E72)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val resolvedVideo = customVideoUrl.ifBlank { "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4" }
                                val resolvedThumb = customThumbnailUrl.ifBlank {
                                    defaultThumbnails[when (category) {
                                        "Music" -> 1
                                        "Sports" -> 2
                                        else -> 3
                                    }]
                                }

                                viewModel.publishVideo(
                                    title = title,
                                    description = description,
                                    isLive = false,
                                    videoUrl = resolvedVideo,
                                    thumbnailUrl = resolvedThumb,
                                    category = category
                                )
                                title = ""
                                description = ""
                                customVideoUrl = ""
                                customThumbnailUrl = ""
                                scope.launch {
                                    isPublishingSuccess = true
                                    delay(4000)
                                    isPublishingSuccess = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_video_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PUBLISH VIDEO CHANNEL", fontWeight = FontWeight.Bold)
                    }
                }

                2 -> {
                    // TAB 2: COMMUNITY POST FORM
                    Text(text = "Publish Social community Post", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text("Describe what is on your mind...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 12.dp).testTag("post_body_input")
                    )

                    // SOLUTION FIELD: Custom Post Image attachment
                    OutlinedTextField(
                        value = customPostImageUrl,
                        onValueChange = { customPostImageUrl = it },
                        label = { Text("Direct Link to Post Image Attachment (Optional)") },
                        placeholder = { Text("e.g. https://images.unsplash.com/photo-example") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).testTag("post_image_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (contentText.isNotBlank()) {
                                // Default back to clean nature/sky view if they don't upload a picture
                                val resolvedImage = customPostImageUrl.ifBlank { "https://images.unsplash.com/photo-1518495973542-4542c06a5843?q=80&w=400&auto=format&fit=crop" }

                                viewModel.publishPost(
                                    content = contentText,
                                    imageUrl = resolvedImage
                                )
                                contentText = ""
                                customPostImageUrl = ""
                                scope.launch {
                                    isPublishingSuccess = true
                                    delay(4000)
                                    isPublishingSuccess = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("publish_post_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHARE COMMUNITY POST", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
