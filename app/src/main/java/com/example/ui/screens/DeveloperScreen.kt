package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(viewModel: StreamViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val userState by viewModel.currentUser.collectAsState()

    // Determine lock permissions
    val isGuestUser = userState?.email?.startsWith("guest@") == true

    val facebookUrl = "https://www.facebook.com/md.fahad.hossain.359237"
    val instagramUrl = "https://www.instagram.com/mdfahadhossain006/"
    val githubUrl = "https://github.com/MdFahadHossain006"
    val youtubeUrl = "https://www.youtube.com/@brightnessworld"

    // Backstage user customization values
    var nicknameInput by remember(userState) { mutableStateOf(userState?.name ?: "") }
    var avatarInput by remember(userState) { mutableStateOf(userState?.avatarUrl ?: "") }
    var showProfileSaveAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131116)) // Ultra deep cinema background
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // --- GLASSMORPHISM SECTION A: CLIENT PERSONAL PROFILE ---
        if (!isGuestUser && userState != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x66FFFFFF), Color(0x11FFFFFF))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)), // Glassy translucency
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CUSTOMIZE YOUR BROADCASTER PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD0BCFF),
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current editing Live avatar preview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .border(1.dp, Color(0xFFD0BCFF), CircleShape)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarInput)
                                    .crossfade(true)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .build(),
                                contentDescription = "Live Avatar Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userState?.email ?: "",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Customize how you appear globally during chats, live streams, and uploads.",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        label = { Text("Display Nickname", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_nickname_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = avatarInput,
                        onValueChange = { avatarInput = it },
                        label = { Text("Custom Profile Image URL", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            focusedLabelColor = Color(0xFFD0BCFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_avatar_url_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateUserProfile(name = nicknameInput, avatarUrl = avatarInput)
                            showProfileSaveAlert = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("profile_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE BROADCAST PROFILE", fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    }

                    if (showProfileSaveAlert) {
                        Text(
                            text = "✓ Broadcaster profile updated successfully!",
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            // Elegant placeholder welcome card for Guest visitors
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0x30FFFFFF), Color(0x05FFFFFF))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Guest Lock",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VISITOR GUEST ACCOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You are currently exploring as a guest visitor. Log in with a Google account to unlock live broadcasting, customize your avatar, set a nickname, and post content.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x22FFFFFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Get Started / Login with Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- GLASSMORPHISM SECTION B: CREATOR BIO PROFILE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x66FFFFFF), Color(0x11FFFFFF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0x12FFFFFF)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OFFICIAL APP CREATOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.5.sp
                    )
                }

                // Custom Rounded avatar with updated image
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFD0BCFF), Color(0xFFF014E2))
                                ),
                                radius = (size.minDimension / 2) + 3.dp.toPx()
                            )
                        }
                        .background(Color(0xFF201D24), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://i.postimg.cc/0yKFvBbq/IMG-20250830-121909.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Md Fahad Hossain Avatar",
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MD. Fahad Hossain",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Lead Platform Architect & Developer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // BIO Section exactly matching user description
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1A000000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BIO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = " HELLO EVERYONE 👋,\nI AM MD FAHAD HOSSAIN. I AM FROM BANGLADESH 🇧🇩. I AM A SOFTWARE DEVELOPER, WEB DEVELOPER, ANDROID DEVELOPER, AND CYBERSECURITY EXPERT.I MAKE BROADCASTS FOR YOU SO THAT YOU CAN WATCH TV AND RADIO CHANNELS FROM BANGLADESH AND ALL OVER THE WORLD. IF YOU WANT TO CONTACT ME OR IF YOU HAVE ANY IDEAS TO SHARE, YOU CAN MESSAGE ME. ALL OF MY PROFILES ARE GIVEN BELOW 👇\n\nTHANK YOU FOR VISITING MY APPS AND SHARE IT WITH EACH OTHER SO THAT EVERYONE CAN ENJOY IT .\n\nTHANK YOU 💐",
                            fontSize = 12.sp,
                            color = Color(0xE6FFFFFF),
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Left
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "CONTACT WITH DEVELOPER",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x3FFFFFF), RoundedCornerShape(8.dp))
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Social contacts rows
                DeveloperContactRow(
                    title = "Join Conversation on Facebook",
                    subtitle = "md.fahad.hossain.359237",
                    brandColor = Color(0xFF1877F2),
                    iconContainerColor = Color(0x201877F2),
                    onClick = { launchBrowserIntent(context, facebookUrl) },
                    modifier = Modifier.testTag("dev_link_facebook")
                )

                Spacer(modifier = Modifier.height(12.dp))

                DeveloperContactRow(
                    title = "Follow Aesthetics on Instagram",
                    subtitle = "@mdfahadhossain006",
                    brandColor = Color(0xFFE1306C),
                    iconContainerColor = Color(0x20E1306C),
                    onClick = { launchBrowserIntent(context, instagramUrl) },
                    modifier = Modifier.testTag("dev_link_instagram")
                )

                Spacer(modifier = Modifier.height(12.dp))

                DeveloperContactRow(
                    title = "Inspect Repositories on GitHub",
                    subtitle = "MdFahadHossain006",
                    brandColor = Color.White,
                    iconContainerColor = Color(0x20FFFFFF),
                    onClick = { launchBrowserIntent(context, githubUrl) },
                    modifier = Modifier.testTag("dev_link_github")
                )

                Spacer(modifier = Modifier.height(12.dp))

                DeveloperContactRow(
                    title = "Subscribe to brightnessworld on YouTube",
                    subtitle = "@brightnessworld",
                    brandColor = Color(0xFFFF0000),
                    iconContainerColor = Color(0x20FF0000),
                    onClick = { launchBrowserIntent(context, youtubeUrl) },
                    modifier = Modifier.testTag("dev_link_youtube")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- PREMIUM SECURITY, APP LICENSING & PERFORMANCE CENTER ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.6.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFD0BCFF), Color(0xFFF014E2))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1E1B22)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFF014E2),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LICENSING & SECURITY SHIELD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LICENSE AUTHENTICITY Row
                        val isAuthentic = com.example.data.security.SecurityManager.isAppIdAuthentic(context)
                        SecurityStatusFeatureRow(
                            label = "Package Anti-Clone Shield",
                            value = if (isAuthentic) "OK - ORIGINAL CERTIFIED BUILD" else "WARNING - CLONED INSTANCE DETECTED!",
                            isGood = isAuthentic,
                            icon = if (isAuthentic) Icons.Default.VerifiedUser else Icons.Default.Warning
                        )

                        // REGISTERED LICENSEE Row
                        SecurityStatusFeatureRow(
                            label = "Licensed Application Owner",
                            value = com.example.data.security.SecurityManager.LICENSEE,
                            isGood = true,
                            icon = Icons.Default.Copyright
                        )

                        // SECURITY SCORE Row
                        val score = com.example.data.security.SecurityManager.getSecurityScore(context)
                        SecurityStatusFeatureRow(
                            label = "Integrity Security Level",
                            value = "$score% (Active Protection)",
                            isGood = score >= 90,
                            icon = Icons.Default.GppGood
                        )

                        // PERFORMANCE GRADE Row
                        val grade = com.example.data.security.SecurityManager.getPerformanceGrade()
                        SecurityStatusFeatureRow(
                            label = "ExoPlayer Low Buffer Playback",
                            value = "OPTIMAL (Latency Tuned 1.5s)",
                            isGood = true,
                            icon = Icons.Default.Speed
                        )

                        // SYSTEM HARDWARE GRADE Row
                        SecurityStatusFeatureRow(
                            label = "Processor Core Optimization",
                            value = grade,
                            isGood = true,
                            icon = Icons.Default.Memory
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // OFFICIAL IP / COPYRIGHT STATEMENT
                        Text(
                            text = "OFFICIAL INTELLECTUAL PROPERTY NOTICE\nThis professional streaming platform is custom signed and distributed. By explicit licensing agreement, redistribution, variable spoofing, decompiling, or repackaging as an unlicensed clone is strictly prohibited under international copyright laws, reserved for the official platform licensee.",
                            fontSize = 10.sp,
                            color = Color(0x99FFFFFF),
                            lineHeight = 15.sp,
                            textAlign = TextAlign.Left
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Version 1.1.0 (${com.example.data.security.SecurityManager.BRAND_NAME})",
                    fontSize = 11.sp,
                    color = Color(0x33FFFFFF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DeveloperContactRow(
    title: String,
    subtitle: String,
    brandColor: Color,
    iconContainerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF25232A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = "Link",
                    tint = brandColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0x66FFFFFF),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0x4DFFFFFF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun launchBrowserIntent(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
    }
}

@Composable
fun SecurityStatusFeatureRow(
    label: String,
    value: String,
    isGood: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGood) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = if (isGood) Color.White else Color(0xFFF44336), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
