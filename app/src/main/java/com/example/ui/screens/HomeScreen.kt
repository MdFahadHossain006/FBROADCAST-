package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Video
import com.example.ui.viewmodel.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StreamViewModel,
    onVideoClick: (String) -> Unit
) {
    val videos by viewModel.allVideos.collectAsState()
    val follows by viewModel.allFollows.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Bangladesh", "Sports", "Movie", "Music", "Kids", "Islamic", "News", "Gaming")

    // Highly optimized cache-remembered filtering for maximum smoothness and speed
    val filteredVideos = remember(videos, selectedCategory, searchQuery) {
        videos.filter { video ->
            val matchesCategory = selectedCategory == "All" || video.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                    video.title.contains(searchQuery, ignoreCase = true) || 
                    video.category.contains(searchQuery, ignoreCase = true) ||
                    video.description.contains(searchQuery, ignoreCase = true) ||
                    video.creatorName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Dynamic animation for the Pulse LIVE dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131116)) // Immersive dark cinema canvas
    ) {
        // Modern Premium M3 Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { 
                Text(
                    text = "Search TV channels, categories or shows...", 
                    color = Color(0x66FFFFFF), 
                    fontSize = 13.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = Color(0xFFD0BCFF)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search query",
                            tint = Color.White
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedContainerColor = Color(0x1AFFFFFF),
                unfocusedContainerColor = Color(0x06FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                .testTag("channel_search_bar")
        )

        // Horizontal Topic Category Scroll
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { 
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            letterSpacing = 0.5.sp
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x12FFFFFF),
                        labelColor = Color(0xFFCAC4D0),
                        selectedContainerColor = Color(0xFFD0BCFF),
                        selectedLabelColor = Color(0xFF381E72)
                    ),
                    border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_$category")
                )
            }
        }

        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "No Streams Found",
                        tint = Color(0x22FFFFFF),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No channels available in this section.",
                        color = Color(0x66FFFFFF),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Premium Card Grid showing all TV Channels Vertical Grid Style
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("channels_vertical_grid"),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVideos) { channel ->
                    val isFollowing = follows.any { it.creatorId == channel.creatorId }
                    GridChannelCard(
                        channel = channel,
                        pulseAlpha = livePulseAlpha,
                        isFollowing = isFollowing,
                        onFollowClick = { viewModel.toggleFollowCreator(channel.creatorId) },
                        onCardClick = { onVideoClick(channel.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GridChannelCard(
    channel: Video,
    pulseAlpha: Float,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x33FFFFFF), Color(0x08FFFFFF))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("channel_card_${channel.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)), // Glassy transcluent card
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(channel.thumbnailUrl)
                        .crossfade(true)
                        .fallback(android.R.drawable.stat_notify_error)
                        .build(),
                    contentDescription = "Logo for ${channel.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Breathing neon LIVE badge on upper left
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xFFB3261E), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(Color.White.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Category overlay bottom end
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = channel.category,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Channel label details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = channel.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${formatViewerCount(channel.viewsCount)} viewers",
                        color = Color(0x99FFFFFF),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Minimal follow star/heart action feedback
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onFollowClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Channel",
                            tint = if (isFollowing) Color(0xFFF014E2) else Color(0x66FFFFFF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatViewerCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}
