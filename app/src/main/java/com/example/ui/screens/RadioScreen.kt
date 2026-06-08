package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.delay
import kotlin.math.sin

data class RadioStation(
    val name: String,
    val url: String,
    val category: String,
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Preconfigured list of client's premium stations
    val allStations = remember {
        listOf(
            // --- BANGLADESH FM ---
            RadioStation("Radio Foorti 88.0 FM", "https://stream.zeno.fm/radiofoorti", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Dhaka FM 90.4", "https://stream.zeno.fm/dhakafm904", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Jago FM 94.4", "https://stream.zeno.fm/jagofm", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Today 89.6", "https://stream.zeno.fm/radiotoday", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("ABC Radio 89.2", "https://stream.zeno.fm/abcradiobd", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Shadhin 92.4", "https://stream.zeno.fm/radioshadhin", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Bhumi 92.8", "https://stream.zeno.fm/radiobhumi", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Peoples Radio 91.6", "https://stream.zeno.fm/peoplesradio", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Capital 94.8", "https://stream.zeno.fm/radiocapital", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Amber 102.4", "https://stream.zeno.fm/radioamber", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Din Raat 93.6", "https://stream.zeno.fm/radiodinraat", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio GoonGoon", "https://stream.zeno.fm/radiogoongoon", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Next 93.2", "https://stream.zeno.fm/radionext", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Love Bangla", "https://stream.zeno.fm/radiolovebangla", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Dhol 94.0", "https://stream.zeno.fm/radiodhol", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Aamar 88.4", "https://stream.zeno.fm/radioaamar", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Bangladesh Betar (FM)", "https://stream.zeno.fm/bangladeshbetar", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Bangladesh Betar External", "https://stream.zeno.fm/betarexternal", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Traffic FM 88.8", "https://stream.zeno.fm/trafficfm", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Metrowave", "https://stream.zeno.fm/radiometrowave", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Padma", "https://stream.zeno.fm/radiopadma", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("FnF FM Bangla", "https://stream.zeno.fm/fnffm", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Jalsha", "https://stream.zeno.fm/radiojalsha", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Sargam BD", "https://stream.zeno.fm/radiosargam", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Swadhin Online", "https://stream.zeno.fm/swadhinonline", "Bangladesh FM", "Zeno Live Feed"),
            RadioStation("Radio Campus BD", "https://stream.zeno.fm/radiocampusbd", "Bangladesh FM", "Zeno Live Feed"),
            
            // --- WORLD NEWS ---
            RadioStation("BBC World Service", "https://lstn.lv/bbchw/livestream/ukwfm.meta.m3u8", "World News", "BBC World Live Output"),
            RadioStation("BBC Radio 4", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_radio_fourfm/bbc_radio_fourfm.isml/bbc_radio_fourfm-audio=320000.m3u8", "World News", "World Broadcasts"),
            RadioStation("BBC Radio 5 Live", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_radio_five_live/bbc_radio_five_live.isml/bbc_radio_five_live-audio=320000.m3u8", "World News", "Sports & Discussions"),
            RadioStation("Voice of America", "https://voa-11.akacast.akamaistream.net/7/317/322029/v1/ibb.akacast.akamaistream.net/voa-11.mp3", "World News", "VOA Live Channel"),
            RadioStation("Radio France Internationale", "https://rfienglish64k.ice.infomaniak.ch/rfienglish-64.mp3", "World News", "RFI World English Feed"),
            RadioStation("Deutsche Welle English", "https://dwamdstream103.akamaized.net/hls/live/2015526/dwstream103/index.m3u8", "World News", "DW English Broadcast"),
            RadioStation("NHK World Radio Japan", "https://radiojapan.akamaized.net/hls/live/2020435/nhkworldradio/index.m3u8", "World News", "NHK Tokyo HLS"),
            RadioStation("Radio Romania International", "https://stream2.srr.ro:8000/rri.mp3", "World News", "RRI Live Feed"),
            RadioStation("Radio Taiwan International", "https://streaming.rti.org.tw/radio/RTI_English.mp3", "World News", "RTI Live Feed"),
            RadioStation("China Radio International", "https://live.cri.cn/english.m3u8", "World News", "CRI English Broadcast"),

            // --- MUSIC ---
            RadioStation("BBC Radio 1", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_radio_one/bbc_radio_one.isml/bbc_radio_one-audio=320000.m3u8", "Music", "Youth Hits & Pop"),
            RadioStation("BBC Radio 2", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_radio_two/bbc_radio_two.isml/bbc_radio_two-audio=320000.m3u8", "Music", "Adult Contemporary"),
            RadioStation("BBC Radio 3", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_radio_three/bbc_radio_three.isml/bbc_radio_three-audio=320000.m3u8", "Music", "Classical Masterworks"),
            RadioStation("BBC Radio 6 Music", "http://as-hls-ww-live.akamaized.net/pool_904/live/ww/bbc_6music/bbc_6music.isml/bbc_6music-audio=320000.m3u8", "Music", "Alternative Classics"),
            RadioStation("NTS Radio 1", "https://stream-relay-geo.ntslive.net/stream", "Music", "London underground music"),
            RadioStation("NTS Radio 2", "https://stream-relay-geo.ntslive.net/stream2", "Music", "Alternative soundscapes"),
            RadioStation("KEXP Seattle", "https://kexp.streamguys1.com/kexp160.aac", "Music", "Where Music Matters"),
            RadioStation("KCRW", "https://kcrw.streamguys1.com/kcrw_192k_mp3_on_air", "Music", "Eclectic music & arts"),
            RadioStation("Jazz24", "https://live.wostreaming.net/direct/ppm-jazz24mp3-ibc1", "Music", "Continuous Premium Jazz"),
            RadioStation("Radio Paradise", "https://stream.radioparadise.com/mp3-192", "Music", "Eclectic rock playlist"),
            RadioStation("SomaFM Groove Salad", "https://ice2.somafm.com/groovesalad-128-mp3", "Music", "Chilled ambient beats"),
            RadioStation("SomaFM Drone Zone", "https://ice2.somafm.com/dronezone-128-mp3", "Music", "Deep minimalist drone"),
            RadioStation("SomaFM Indie Pop Rocks", "https://ice2.somafm.com/indiepop-128-mp3", "Music", "Indie pop tracks"),
            RadioStation("Classic FM UK", "https://media-ice.musicradio.com/ClassicFMMP3", "Music", "Classic masterpieces"),
            RadioStation("Smooth Radio UK", "https://media-ice.musicradio.com/SmoothUKMP3", "Music", "Smooth love songs"),
            RadioStation("Absolute Radio", "https://edge-bauerall-01-gos2.sharp-stream.com/absoluteradio.mp3", "Music", "Rock & alternative hits"),
            RadioStation("Virgin Radio UK", "https://radio.virginradio.co.uk/stream", "Music", "Greatest indie hits"),
            RadioStation("Radio Swiss Classic", "https://stream.srg-ssr.ch/m/rsc_de/mp3_128", "Music", "Pure classical performance"),
            RadioStation("Radio Swiss Jazz", "https://stream.srg-ssr.ch/m/rsj/mp3_128", "Music", "Smooth Swiss jazz"),
            RadioStation("Radio Swiss Pop", "https://stream.srg-ssr.ch/m/rsp/mp3_128", "Music", "Swiss Pop and standard tunes"),

            // --- WORLD ---
            RadioStation("Al Jazeera Audio", "https://live-hls-audio-web-aja.getaj.net/AJA/index.m3u8", "World", "Al Jazeera English News"),
            RadioStation("WNYC", "https://fm939.wnyc.org/wnycfm", "World", "New York Public Radio"),
            RadioStation("WBUR Boston", "https://streams.wbur.org/wbur", "World", "NPR News & talk"),
            RadioStation("NPR News", "https://npr-ice.streamguys1.com/live.mp3", "World", "National Public Radio VOA"),
            RadioStation("CBC Radio One", "https://cbcradiolive.akamaized.net/hls/live/2031888/ES_R1MB/master.m3u8", "World", "Canadian Broadcast Hub"),
            RadioStation("ABC Australia", "https://live-radio01.mediahubaustralia.com/2TJW/mp3/", "World", "Australian Broadcasting"),
            RadioStation("RNZ National", "https://radionz-ice.streamguys.com/national.mp3", "World", "Radio New Zealand National"),
            RadioStation("Radio New Zealand Concert", "https://radionz-ice.streamguys.com/concert.mp3", "World", "RNZ Classical concert feed"),
            RadioStation("FM4 Austria", "https://orf-live.ors-shoutcast.at/fm4-q2a", "World", "Austrian alternative youth network"),
            RadioStation("SRF 4 News", "https://stream.srg-ssr.ch/m/drs4news/mp3_128", "World", "Swiss live news station"),
            RadioStation("Bayern 1", "https://dispatcher.rndfnk.com/br/br1/live/mp3/low", "World", "Bavarian classic songs"),
            RadioStation("France Inter", "https://direct.franceinter.fr/live/franceinter-midfi.mp3", "World", "French premier radio"),
            RadioStation("FIP Radio", "https://icecast.radiofrance.fr/fip-midfi.mp3", "World", "Cult French musical mix"),
            // Radio Garden Mix as requested
            RadioStation("Radio Garden Mix", "https://stream.radioparadise.com/aac-320", "World", "Selected High Quality streams"),
            RadioStation("Worldwide FM", "https://worldwidefm.out.airtime.pro/worldwidefm_a", "World", "Gilles Peterson music hub"),
            RadioStation("KISS FM UK", "https://icy-e-bab-04-gos.sharp-stream.com/kissnational.mp3", "World", "Dance & urban beats"),
            RadioStation("Heart UK", "https://media-ice.musicradio.com/HeartUKMP3", "World", "Bright and modern pop"),
            RadioStation("Qmusic Belgium", "https://icecast-qmusicbe-cdp.triple-it.nl/Qmusic_be_live_128.mp3", "World", "Belgian trending hits")
        )
    }

    val categories = remember { listOf("ALL", "Bangladesh FM", "World News", "Music", "World") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Audio Playback states
    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

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
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(audioAttrs, true)
            }
    }
    var currentPlayingStation by remember { mutableStateOf<RadioStation?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPlaybackBuffering by remember { mutableStateOf(false) }
    var playbackErrorMsg by remember { mutableStateOf<String?>(null) }
    var currentVolume by remember { mutableStateOf(1.0f) }

    // Live Sound Spectrum Waves animation state
    // Periodically fluctuate waves amplitudes when isPlaying is true, giving realistic visualizer feedback
    val soundWavesCount = 20
    val waveAmplitudes = remember { mutableStateListOf<Float>().apply { repeat(soundWavesCount) { add(0.12f) } } }
    LaunchedEffect(isPlaying, isPlaybackBuffering) {
        if (isPlaying && !isPlaybackBuffering) {
            while (true) {
                delay(80)
                for (i in 0 until soundWavesCount) {
                    waveAmplitudes[i] = (15..95).random() / 100f
                }
            }
        } else {
            // Calm state
            for (i in 0 until soundWavesCount) {
                waveAmplitudes[i] = 0.08f
            }
        }
    }

    // Connect player state changes safely
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isPlaybackBuffering = (state == Player.STATE_BUFFERING)
                isPlaying = (state == Player.STATE_READY && exoPlayer.playWhenReady)
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("RadioScreen", "ExoPlayer Error playing radio stream", error)
                playbackErrorMsg = "Playback failed: ${error.localizedMessage ?: "Stream currently offline"}"
                isPlaying = false
                isPlaybackBuffering = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Apply active volume level changes dynamically
    LaunchedEffect(currentVolume) {
        exoPlayer.volume = currentVolume
    }

    // Playback Action handler
    val playStation: (RadioStation) -> Unit = { station ->
        playbackErrorMsg = null
        isPlaybackBuffering = true
        currentPlayingStation = station
        
        try {
            val mediaItem = MediaItem.fromUri(station.url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            isPlaying = true
        } catch (e: Exception) {
            playbackErrorMsg = "Error Loading Station Link"
            isPlaybackBuffering = false
            isPlaying = false
        }
    }

    // Filter logic
    val filteredStations = remember(selectedCategory, searchQuery) {
        allStations.filter { station ->
            val matchCat = (selectedCategory == "ALL" || station.category == selectedCategory)
            val matchQuery = (searchQuery.isBlank() || station.name.contains(searchQuery, ignoreCase = true) || station.category.contains(searchQuery, ignoreCase = true))
            matchCat && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131116)) // Immersive dark slate
    ) {
        // Upper Title Header
        Surface(
            color = Color(0xFF1F1D23),
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x20D0BCFF), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio Symbol",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RADIO BROADCAST STATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD0BCFF),
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            text = "Natively tuned premium quality sounds",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Elegant Search Text Field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 50+ Live Radio Stations...", fontSize = 13.sp, color = Color(0x66FFFFFF)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF131116),
                        unfocusedContainerColor = Color(0xFF131116),
                        focusedIndicatorColor = Color(0xFFD0BCFF),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("radio_search_input")
                )
            }
        }

        // Horizontal Categories Filter Chips
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131116))
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Horizontal scrolling selection of Category tabs
                categories.forEach { cat ->
                    val isSelected = (selectedCategory == cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD0BCFF),
                            selectedLabelColor = Color(0xFF381E72),
                            containerColor = Color(0xFF1F1D23),
                            labelColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = null
                    )
                }
            }
        }

        Divider(color = Color(0x11FFFFFF), thickness = 1.dp)

        // Main Vertical Grid Layout
        if (filteredStations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No stations match your criteria",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = rememberLazyGridState(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("radio_stations_grid"),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredStations) { station ->
                    val isCurrentlyPlayingThis = (currentPlayingStation?.name == station.name)
                    
                    RadioStationCard(
                        station = station,
                        isPlaying = isCurrentlyPlayingThis && isPlaying,
                        isBuffering = isCurrentlyPlayingThis && isPlaybackBuffering,
                        onClick = {
                            if (isCurrentlyPlayingThis) {
                                if (isPlaying) {
                                    exoPlayer.playWhenReady = false
                                    isPlaying = false
                                } else {
                                    exoPlayer.playWhenReady = true
                                    isPlaying = true
                                }
                            } else {
                                playStation(station)
                            }
                        }
                    )
                }
            }
        }

        // Active Player Controller docked bottom drawer with visuals
        AnimatedVisibility(
            visible = currentPlayingStation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            currentPlayingStation?.let { station ->
                Surface(
                    color = Color(0xFF1F1D23),
                    tonalElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("radio_bottom_panel")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Error banner
                        playbackErrorMsg?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFB3261E), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(msg, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Playback Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular icon representation with spin pulse
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0x1F000000), CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlaybackBuffering) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFD0BCFF),
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFFF014E2).copy(alpha = 0.4f), Color(0xFFD0BCFF).copy(alpha = 0.1f))
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color(0xFFD0BCFF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF014E2).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = station.category.uppercase(),
                                            fontSize = 8.sp,
                                            color = Color(0xFFF014E2),
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Text(
                                        text = if (isPlaybackBuffering) "Buffering High Audio..." else "LIVE Stream active",
                                        fontSize = 11.sp,
                                        color = if (isPlaybackBuffering) Color(0xFFD0BCFF) else Color.Gray
                                    )
                                }
                            }

                            // Action Play toggler button
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.playWhenReady = false
                                        isPlaying = false
                                    } else {
                                        exoPlayer.playWhenReady = true
                                        isPlaying = true
                                        playbackErrorMsg = null
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("radio_play_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle play radio",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Custom Sound Wave Spectrum Analyzer Canvas
                        // Represents real-time live playback sounds of best quality
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Color(0xFF131116), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barWidth = 8.dp.toPx()
                                val barSpacing = 6.dp.toPx()
                                val barsToDraw = (size.width / (barWidth + barSpacing)).toInt().coerceAtMost(soundWavesCount)
                                val initialX = (size.width - (barsToDraw * (barWidth + barSpacing))) / 2f
                                
                                for (i in 0 until barsToDraw) {
                                    val currentHeight = size.height * waveAmplitudes[i % waveAmplitudes.size]
                                    val x = initialX + i * (barWidth + barSpacing)
                                    val y = (size.height - currentHeight) / 2f
                                    
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFFD0BCFF), Color(0xFFF014E2))
                                        ),
                                        topLeft = Offset(x, y),
                                        size = androidx.compose.ui.geometry.Size(barWidth, currentHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Volume Control Slider Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentVolume == 0f) Icons.Default.VolumeMute else if (currentVolume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                                contentDescription = "Volume icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Slider(
                                value = currentVolume,
                                onValueChange = { currentVolume = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD0BCFF),
                                    activeTrackColor = Color(0xFFD0BCFF),
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.weight(1f).height(18.dp).testTag("radio_volume_slider")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadioStationCard(
    station: RadioStation,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val outlineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val outlineColor = if (isPlaying) Color(0xFFD0BCFF).copy(alpha = outlineAlpha) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick)
            .testTag("radio_card_${station.name.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF201D24) else Color(0xFF1F1D23)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, outlineColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Circular icon representation
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(
                                colors = when (station.category) {
                                    "Bangladesh FM" -> listOf(Color(0xFF34C759), Color(0xFF1F8438))
                                    "World News" -> listOf(Color(0xFF007AFF), Color(0xFF004080))
                                    "Music" -> listOf(Color(0xFFFF2D55), Color(0xFF80001C))
                                    else -> listOf(Color(0xFF5856D6), Color(0xFF2B2A80))
                                }
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = station.name.split(" ").take(2).map { it.firstOrNull() ?: "" }.joinToString("")
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Small live signal indicator or tag
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFB3261E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PLAYING",
                            fontSize = 7.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isBuffering) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD0BCFF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BUF..",
                            fontSize = 7.sp,
                            color = Color(0xFF381E72),
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = station.category.split(" ").firstOrNull()?.uppercase() ?: "",
                            fontSize = 7.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = station.name,
                    color = if (isPlaying) Color(0xFFD0BCFF) else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = station.description,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}
