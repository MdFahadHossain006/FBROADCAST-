package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StreamViewModel
import com.example.ui.viewmodel.StreamViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely extract Application Context and populate ViewModel Provider
        val app = application as StreamApplication
        val viewModel = ViewModelProvider(
            this,
            StreamViewModelFactory(app, app.repository)
        )[StreamViewModel::class.java]

        setContent {
            MyApplicationTheme(darkTheme = true) { // Immersive cinematic dark theme default
                val userState by viewModel.currentUser.collectAsState()
                var currentTabInLayout by remember { mutableStateOf(0) } // 0=Explore, 1=Community, 2=Publish, 3=DevContact
                
                // Track active video selected for the detail player screen overlay
                var selectedVideoIdInPlayer by remember { mutableStateOf<String?>(null) }

                if (userState == null) {
                    // Show Auth/Google Sign-In page first
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            currentTabInLayout = 0 // Default to Home Explorer on entry
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1B1F))) {
                        
                        // If a video/live is selected, display the immersive player (which covers content & starts simulator)
                        selectedVideoIdInPlayer?.let { videoId ->
                            StreamPlayerScreen(
                                videoId = videoId,
                                viewModel = viewModel,
                                onBackClick = {
                                    viewModel.stopChatSimulation()
                                    selectedVideoIdInPlayer = null
                                }
                            )
                        } ?: run {
                            // Primary scaffold with top-bar, main page screen switcher, and bottom bar
                            Scaffold(
                                topBar = {
                                    Surface(
                                        color = Color(0xFF25232A),
                                        tonalElevation = 4.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Glowing Brand label
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD0BCFF),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "FBROADCAST",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    letterSpacing = 1.sp
                                                )
                                            }

                                            // Logged-in Google profile actions (avatar and power button)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(userState?.avatarUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "My profile image",
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x33FFFFFF)),
                                                    contentScale = ContentScale.Crop
                                                )

                                                IconButton(
                                                    onClick = { viewModel.logout() },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("logout_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PowerSettingsNew,
                                                        contentDescription = "Google Logout button",
                                                        tint = Color(0xFFD0BCFF),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = Color(0xFF25232A),
                                        tonalElevation = 8.dp,
                                        modifier = Modifier.navigationBarsPadding()
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTabInLayout == 0,
                                            onClick = { currentTabInLayout = 0 },
                                            icon = { 
                                                Icon(
                                                    imageVector = if (currentTabInLayout == 0) Icons.Default.Explore else Icons.Default.Explore,
                                                    contentDescription = "Explore Streams Tab"
                                                )
                                            },
                                            label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFD0BCFF),
                                                selectedTextColor = Color(0xFFD0BCFF),
                                                indicatorColor = Color(0xFF49454F),
                                                unselectedIconColor = Color(0xFFCAC4D0),
                                                unselectedTextColor = Color(0xFFCAC4D0)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_explore")
                                        )

                                        NavigationBarItem(
                                            selected = currentTabInLayout == 1,
                                            onClick = { currentTabInLayout = 1 },
                                            icon = { 
                                                Icon(
                                                    imageVector = Icons.Default.Forum,
                                                    contentDescription = "Community Feed Tab"
                                                )
                                            },
                                            label = { Text("Community", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFD0BCFF),
                                                selectedTextColor = Color(0xFFD0BCFF),
                                                indicatorColor = Color(0xFF49454F),
                                                unselectedIconColor = Color(0xFFCAC4D0),
                                                unselectedTextColor = Color(0xFFCAC4D0)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_community")
                                        )

                                        NavigationBarItem(
                                            selected = currentTabInLayout == 2,
                                            onClick = { currentTabInLayout = 2 },
                                            icon = { 
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    contentDescription = "Publish Content Tab"
                                                )
                                            },
                                            label = { Text("Publish", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFD0BCFF),
                                                selectedTextColor = Color(0xFFD0BCFF),
                                                indicatorColor = Color(0xFF49454F),
                                                unselectedIconColor = Color(0xFFCAC4D0),
                                                unselectedTextColor = Color(0xFFCAC4D0)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_publish")
                                        )

                                        NavigationBarItem(
                                            selected = currentTabInLayout == 3,
                                            onClick = { currentTabInLayout = 3 },
                                            icon = { 
                                                Icon(
                                                    imageVector = Icons.Default.Radio,
                                                    contentDescription = "Radio Station Tab"
                                                )
                                            },
                                            label = { Text("Radio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFD0BCFF),
                                                selectedTextColor = Color(0xFFD0BCFF),
                                                indicatorColor = Color(0xFF49454F),
                                                unselectedIconColor = Color(0xFFCAC4D0),
                                                unselectedTextColor = Color(0xFFCAC4D0)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_radio")
                                         )

                                         NavigationBarItem(
                                             selected = currentTabInLayout == 4,
                                             onClick = { currentTabInLayout = 4 },
                                             icon = { 
                                                 Icon(
                                                     imageVector = Icons.Default.AccountCircle,
                                                     contentDescription = "Profile Tab"
                                                 )
                                             },
                                             label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                             colors = NavigationBarItemDefaults.colors(
                                                 selectedIconColor = Color(0xFFD0BCFF),
                                                 selectedTextColor = Color(0xFFD0BCFF),
                                                 indicatorColor = Color(0xFF49454F),
                                                 unselectedIconColor = Color(0xFFCAC4D0),
                                                 unselectedTextColor = Color(0xFFCAC4D0)
                                             ),
                                             modifier = Modifier.testTag("nav_tab_profile")
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    // Cross-fade animations for elegant page switching
                                    AnimatedContent(
                                        targetState = currentTabInLayout,
                                        transitionSpec = {
                                            fadeIn() togetherWith fadeOut()
                                        },
                                        label = "page_transition"
                                    ) { tabIndex ->
                                        when (tabIndex) {
                                            0 -> HomeScreen(
                                                viewModel = viewModel,
                                                onVideoClick = { videoId ->
                                                    selectedVideoIdInPlayer = videoId
                                                }
                                            )
                                            1 -> CommunityScreen(viewModel = viewModel)
                                            2 -> PublishScreen(viewModel = viewModel)
                                            3 -> RadioScreen()
                                             else -> DeveloperScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
