package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StreamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: StreamViewModel,
    onLoginSuccess: () -> Unit
) {
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAccountChooser by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Observe user session for navigation redirect
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    // Dynamic wave animation behind logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val radiusMultipler by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F)) // Obsidian luxury dark background
            .drawBehind {
                // Background radial glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x30D0BCFF), Color.Transparent),
                        radius = size.minDimension * 0.8f
                    ),
                    center = this.center
                )
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Elegant glowing logo container
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFFD0BCFF),
                            radius = (size.minDimension / 2) * radiusMultipler,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                    .background(Color(0x20D0BCFF), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "FBROADCAST Play Icon",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FBROADCAST",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Live Stream & Connect Effortlessly",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color(0x99FFFFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!isLoggingIn) {
                // Styled Google Login Button
                Button(
                    onClick = { showAccountChooser = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F0F12)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Drawing custom beautiful search G letter representing google logo
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFEA4335), CircleShape)
                                .clip(CircleShape)
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                "G",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.loginWithGoogle(
                            name = "Guest User",
                            email = "guest@fbroadcast.com",
                            avatarUrl = ""
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("direct_access_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0BCFF)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Direct Access (No Log In)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                CircularProgressIndicator(
                    color = Color(0xFFD0BCFF),
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Connecting Google accounts safely...",
                    color = Color(0x90FFFFFF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Beautiful Material 3 Account Chooser Sheet (Simulated)
        if (showAccountChooser) {
            AlertDialog(
                onDismissRequest = { showAccountChooser = false },
                title = {
                    Text(
                        text = "Choose an Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "to continue to FBROADCAST",
                            fontSize = 12.sp,
                            color = Color(0x66FFFFFF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        // 1. Current user custom account from requirements metadata
                        AccountItemRow(
                            name = "Active Member",
                            email = "member@fbroadcast.com",
                            avatarInitials = "AM",
                            avatarColor = Color(0xFFD0BCFF),
                            onClick = {
                                showAccountChooser = false
                                viewModel.loginWithGoogle(
                                    name = "Active Member",
                                    email = "member@fbroadcast.com",
                                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=250&auto=format&fit=crop"
                                )
                            }
                        )

                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 8.dp))

                        // 2. Guest account
                        AccountItemRow(
                            name = "Guest Streamer",
                            email = "guest.streamer@gmail.com",
                            avatarInitials = "GS",
                            avatarColor = Color(0xFF9C27B0),
                            onClick = {
                                showAccountChooser = false
                                viewModel.loginWithGoogle(
                                    name = "Guest Streamer",
                                    email = "guest.streamer@gmail.com",
                                    avatarUrl = ""
                                )
                            }
                        )

                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 8.dp))

                        // 3. Custom entry button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAccountChooser = false
                                    showCustomInput = true
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x33FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Use another account",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                "Use another account",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAccountChooser = false }) {
                        Text("Cancel", color = Color(0xFFD0BCFF))
                    }
                },
                containerColor = Color(0xFF25232A),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Custom account input form
        if (showCustomInput) {
            AlertDialog(
                onDismissRequest = { showCustomInput = false },
                title = {
                    Text(
                        "Google Auth Details",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD0BCFF),
                                focusedLabelColor = Color(0xFFD0BCFF)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customEmail,
                            onValueChange = { customEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD0BCFF),
                                focusedLabelColor = Color(0xFFD0BCFF)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customName.isNotBlank() && customEmail.isNotBlank()) {
                                showCustomInput = false
                                viewModel.loginWithGoogle(
                                    name = customName,
                                    email = customEmail,
                                    avatarUrl = ""
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        )
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomInput = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF25232A),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun AccountItemRow(
    name: String,
    email: String,
    avatarInitials: String,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(avatarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitials,
                color = if (avatarColor == Color(0xFFD0BCFF)) Color(0xFF381E72) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = email,
                fontSize = 11.sp,
                color = Color(0x99FFFFFF)
            )
        }
    }
}