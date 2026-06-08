# 🎬 FBROADCAST (StreamCast)

[![License: Proprietary](https://img.shields.io/badge/License-Proprietary-red.svg)](https://github.com/)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Built with: Jetpack Compose](https://img.shields.io/badge/Built%20with-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Security: Anti--Clone Shield](https://img.shields.io/badge/Security-Anti--Clone%20Shield-gold.svg)]()

**FBROADCAST** is a premium, ultra-fast, and secure live-broadcaster streaming application for Android. Engineered using modern Jetpack Compose architecture and a highly optimized ExoPlayer backend, it delivers a seamless, zero-lag television and radio streaming experience featuring regional and worldwide channels.

---

## 🚀 Premium Features

### 📺 Cinematic Full-Screen Player
* **Immersive Playback:** Dynamically toggles interface elements. All timeline bars, stream badges, and overlay headers automatically fade out during full-screen view to offer a clean, distraction-free display.
* **Tap-to-Toggle Interface:** Quickly access play/pause, volume adjustment, and screen resizing with responsive, lightweight screen overlays.
* **Multi-Format Compatibility:** Out-of-the-box support for Live HLS (`.m3u8`), MPEG-DASH (`.mpd`), and standard MP4/TS media feeds.

### 🔍 Instant-Fast Channel Search & Navigation
* **Predictive Filtering:** Built-in Material 3 top search bar dynamically filters matching stations based on names, languages, descriptors, or categories.
* **In-Memory UI Caching:** Search execution runs through Kotlin `StateFlows` and internal memory caching (`remember` blocks), preventing UI micro-stutters.

### ⚡ Performance Optimization
* **Low-Latency Buffering:** Customized media engine buffer thresholds configured down to a rapid **1.5-second initial delay**, triggering instant live video loads.
* **R8/ProGuard Compressed Build:** Fully structured compiler rules enforcing resource shrinking, asset compression, and reduced device memory consumption.

---

## 🔒 Security & Intellectual Property Shield

To protect the integrity of the application layout, a proprietary security layer is engineered directly into the runtime environment:

* **Dynamic Anti-Cloning Shield:** Runtime bundle-ID authentication dynamically validates the package signature (`com.aistudio.streamcast.vxntpy`). Decompiling or repackaging under cloned signatures automatically flags and prevents application instances from running.
* **Obfuscation Matrix:** ProGuard layout rules fully mangle and encrypt data network classes, database layers (Room/Moshi), and critical pipeline variables against reverse-engineering tools.
* **Licensing Dashboard:** An integrated developer console screen reflecting live application health metrics, low-latency statuses, and ownership authorization data.

---

## 🛠️ Organized Broadcasting Channels

The integrated automated database splits streams cleanly into dedicated local content sections:

| Category | Description | Featured Networks |
| :--- | :--- | :--- |
| **🇧🇩 Bangladesh** | Local terrestrial & satellite channels | BTV, Banglavision, Channel9, Gazi TV, Maasranga |
| **⚽ Sports** | High-definition live events & sports talk | T Sports HD, Somoy FIFA World Cup 2026 |
| **🎬 Movie** | Global cinema, Bollywood, and Hollywood | Cineedge HD, Superrix HD, HBO, Goldmines, Sony Max |
| **👶 Kids** | Animated cartoon feeds and children's content | Doraemon 24/7 Bangla |
| **🌍 All / News** | Global international networks and entertainment | CNN, TRT World, Sony Entertainment, Zee Bangla |

---

## 👤 Developer Profile

> ### HELLO EVERYONE 👋
> **I AM MD FAHAD HOSSAIN** > 🇧🇩 From Bangladesh  
> 💻 **Software Developer | Web Developer | Android Developer | Cybersecurity Expert**
>
> I build broadcasts for you so that you can watch TV and radio channels from Bangladesh and all over the world. If you want to contact me or if you have any ideas to share, feel free to send over a message! All of my dynamic links are accessible directly via the interactive developer panel in the app profile.
> 
> *Thank you for visiting my apps! Share it with each other so that everyone can enjoy it.* 💐

---

## 🛡️ License & Copyright

**Copyright © 2026 Md. Fahad Hossain. All rights reserved.**

This application is proprietary software. Unauthorized copying, modification, decompilation, redistribution, or repackaging of this source code, assets, or compiled binaries via any medium is strictly prohibited without explicit written consent from the licensed application owner.
