package com.example.data.repository

import com.example.data.dao.StreamDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class StreamRepository(private val streamDao: StreamDao) {

    val allVideos: Flow<List<Video>> = streamDao.getAllVideosFlow()
    val allPosts: Flow<List<Post>> = streamDao.getAllPostsFlow()
    val allFollows: Flow<List<Follow>> = streamDao.getAllFollowsFlow()

    fun getVideoByIdFlow(videoId: String): Flow<Video?> = streamDao.getVideoByIdFlow(videoId)

    fun getCommentsFlow(targetId: String, isLiveChat: Boolean): Flow<List<Comment>> =
        streamDao.getCommentsFlow(targetId, isLiveChat)

    suspend fun insertVideo(video: Video) = streamDao.insertVideo(video)
    suspend fun insertPost(post: Post) = streamDao.insertPost(post)

    suspend fun toggleLikeVideo(videoId: String) {
        val video = streamDao.getVideoById(videoId) ?: return
        val updatedLiked = !video.isLiked
        val updatedLikesCount = video.likesCount + (if (updatedLiked) 1 else -1)
        streamDao.updateVideo(video.copy(isLiked = updatedLiked, likesCount = updatedLikesCount))
    }

    suspend fun toggleLikePost(postId: String) {
        val postFlow = streamDao.getAllPostsFlow()
        val allPostsList = postFlow.firstOrNull() ?: return
        val post = allPostsList.find { it.id == postId } ?: return
        val updatedLiked = !post.isLiked
        val updatedLikesCount = post.likesCount + (if (updatedLiked) 1 else -1)
        streamDao.updatePost(post.copy(isLiked = updatedLiked, likesCount = updatedLikesCount))
    }

    suspend fun toggleFollowCreator(creatorId: String) {
        val isFollowing = streamDao.isFollowing(creatorId) ?: false
        if (isFollowing) {
            streamDao.deleteFollow(Follow(creatorId, false))
        } else {
            streamDao.insertFollow(Follow(creatorId, true))
        }
    }

    suspend fun addComment(
        targetId: String,
        authorName: String,
        authorAvatar: String,
        content: String,
        isLiveChat: Boolean
    ) {
        val comment = Comment(
            targetId = targetId,
            authorName = authorName,
            authorAvatar = authorAvatar,
            content = content,
            timestamp = System.currentTimeMillis(),
            isLiveChat = isLiveChat
        )
        streamDao.insertComment(comment)
        
        // If it's a post, increase comments count
        if (!isLiveChat) {
            val allPostsList = streamDao.getAllPostsFlow().firstOrNull() ?: return
            val post = allPostsList.find { it.id == targetId }
            if (post != null) {
                streamDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
            }
        }
    }

    suspend fun publishUserPost(content: String, authorName: String, authorAvatar: String, imageUrl: String?) {
        val post = Post(
            id = UUID.randomUUID().toString(),
            content = content,
            creatorId = "current_user_id",
            creatorName = authorName,
            creatorAvatar = authorAvatar,
            likesCount = 0,
            isLiked = false,
            commentsCount = 0,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        streamDao.insertPost(post)
    }

    suspend fun publishUserVideo(
        title: String,
        description: String,
        isLive: Boolean,
        authorName: String,
        authorAvatar: String,
        videoUrl: String,
        thumbnailUrl: String,
        category: String
    ) {
        val video = Video(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            creatorId = "current_user_id",
            creatorName = authorName,
            creatorAvatar = authorAvatar,
            videoUrl = videoUrl,
            thumbnailUrl = thumbnailUrl,
            likesCount = 0,
            viewsCount = if (isLive) 12 else 0,
            isLiked = false,
            timestamp = System.currentTimeMillis(),
            isLive = isLive,
            category = category
        )
        streamDao.insertVideo(video)
    }

    suspend fun seedMockDataIfEmpty() {
        try {
            // We checking if there's any video or post. If not, seed high quality items.
            val existingVideos = streamDao.getAllVideosFlow().firstOrNull() ?: emptyList()
            if (existingVideos.isEmpty()) {
            data class ChannelSeed(
                val id: String,
                val name: String,
                val logo: String,
                val url: String,
                val group: String,
                val desc: String
            )

            val rawSeeds = listOf(
                ChannelSeed("dd_sports", "DD Sports", "https://i.postimg.cc/7hVCMDyZ/dd-sports.jpg", "https://d3qs3d2rkhfqrt.cloudfront.net/out/v1/b17adfe543354fdd8d189b110617cddd/index_3.m3u8", "Sports", "DD Sports broadcasts premier athletic tournaments, cricket, national tournaments and physical wellness guides."),
                ChannelSeed("redbull_tv", "Redbull TV", "https://i.postimg.cc/nrz1NpPV/Red-Bull-TV.jpg", "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_3360.m3u8", "Sports", "Redbull TV coverage of extraordinary action sports, adventure culture blogs and competitive gaming events."),
                ChannelSeed("asian_tv", "Asian TV", "https://i.postimg.cc/D04SQBVt/Asian-TV.jpg", "https://tvsen6.aynaott.com/asiantv/index.m3u8", "Bangladesh", "Asian TV is a popular Bengali entertainment channel featuring family serials, musical shows, and drama."),
                ChannelSeed("atn_news", "ATN News", "https://i.postimg.cc/PxyNJrGm/ATN-News.jpg", "https://tvsen6.aynaott.com/atnnews/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "ATN News is a first dedicated 24-hour news channel from Bangladesh with quick journalism and fair coverage."),
                ChannelSeed("bijoy_tv", "Bijoy TV", "https://i.postimg.cc/1tnFM9wC/Bijoy-TV.jpg", "https://tvsen6.aynaott.com/bijoytv/index.m3u8", "Bangladesh", "Bijoy TV is an entertainment oriented satellite television channel in Bangladesh with news column stories."),
                ChannelSeed("bangla_tv", "Bangla TV", "https://i.postimg.cc/qqXGm6D1/Bangla-TV.jpg", "https://tvsen6.aynaott.com/banglatv/index.m3u8", "Bangladesh", "Bangla TV provides high standard broadcasts of popular soaps, cultural music and world class movies."),
                ChannelSeed("channel_i_live", "Channel i", "https://i.postimg.cc/tJq1jBzG/Channel-i.jpg", "https://tvsen6.aynaott.com/channeli/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "Channel i is highly regarded for its agriculture initiatives, soap dramas and cultural documentaries."),
                ChannelSeed("channel_24", "Channel 24", "https://i.postimg.cc/C1CCfh6n/Channel-24.jpg", "https://tvsen6.aynaott.com/channel24/index.m3u8", "Bangladesh", "Channel 24 provides news updates, political panel discussions, and visual lifestyle magazines."),
                ChannelSeed("dbc_news", "DBC News", "https://i.postimg.cc/BQzrR9JZ/DBC-News.jpg", "https://tvsen6.aynaott.com/dbcnews/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "DBC News reports national and international affairs 24 hours a day with accurate summaries."),
                ChannelSeed("deepto_tv_live", "Deepto TV", "https://i.postimg.cc/L6WP5g60/Deepto-TV.jpg", "https://byphdgllyk.gpcdn.net/hls/deeptotv/index.m3u8", "Bangladesh", "Deepto TV is known for highly rated Bengali serialized fiction, kid series, and entertainment columns."),
                ChannelSeed("desh_tv", "Desh TV", "https://i.postimg.cc/wvBbd56q/Desh-TV.jpg", "https://tvsen6.aynaott.com/deshtv/index.m3u8", "Bangladesh", "Desh TV features talk shows, developmental discussions, and native musical traditions."),
                ChannelSeed("deshi_tv", "Deshi TV", "https://i.postimg.cc/t4cxjxRj/Deshi-TV.jpg", "https://deshitv.deshitv24.net/live/myStream/playlist.m3u8", "Bangladesh", "Deshi TV features local community initiatives and visual stories of lifestyle and music."),
                ChannelSeed("ekattor_tv", "Ekattor TV", "https://i.postimg.cc/JzDLh7pB/Ekattor.jpg", "https://tvsen6.aynaott.com/ekattorbdtv/index.m3u8", "Bangladesh", "Ekattor TV is a premium 24-hour current affairs channel based in Dhaka with robust political talkshows."),
                ChannelSeed("ekattor_tv_alt", "Ekattor TV HD", "https://i.postimg.cc/JzDLh7pB/Ekattor.jpg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1705/output/1705-audio_113352_eng=113200-video=1692000.m3u8", "Bangladesh", "Ekattor TV HD stream bringing crisp clarity and insightful analytics."),
                ChannelSeed("ekhon_tv", "Ekhon TV", "https://i.postimg.cc/G2XchN1w/Ekhon-TV.jpg", "https://tvsen6.aynaott.com/ekhontv/index.m3u8", "Bangladesh", "Ekhon TV is the first dedicated business news and financial satellite TV channel of Bangladesh."),
                ChannelSeed("ekushe_etv", "Ekushe ETV", "https://i.postimg.cc/Nj5XJGD8/Ekushe.jpg", "https://tvsen6.aynaott.com/etv/index.m3u8", "Bangladesh", "ETV Ekushey television broadcasts educational columns, music archives and folk soap operas."),
                ChannelSeed("global_tv", "Global TV", "https://i.postimg.cc/gJqSzqBb/Global-TV.jpg", "https://tvsen6.aynaott.com/globaltvhd/index.m3u8", "Bangladesh", "Global TV broadcasts local musical performances, lifestyle series, and entertainment shows."),
                ChannelSeed("jamuna_tv_live", "Jamuna TV", "https://i.postimg.cc/63K8YKmD/Jamuna-TV.jpg", "https://tvsen6.aynaott.com/jamunatv/index.m3u8", "Bangladesh", "Jamuna TV offers high fidelity news coverage, analytical research columns, and objective documentaries."),
                ChannelSeed("me_tv", "Me TV", "https://i.postimg.cc/qMZKm8Fc/ME-TV.jpg", "https://iptvbd.live/metv1080/1080.m3u8", "Bangladesh", "Me TV provides custom lifestyle programming, retro movies, and popular music collections."),
                ChannelSeed("news_24_hd", "News 24 HD", "https://i.ibb.co.com/5gCvWJv6/News-24-HD.jpg", "https://tvsen6.aynaott.com/news24/index.m3u8", "Bangladesh", "News 24 HD brings hourly national headlines, global highlights, and sports stories."),
                ChannelSeed("rtv_live", "RTV", "https://i.postimg.cc/Qdr7mnkW/RTV.jpg", "https://tvsen6.aynaott.com/rtv/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "RTV offers award winning dramas, local singing reality shows, and live talk shows."),
                ChannelSeed("sa_tv", "SA TV", "https://i.postimg.cc/Ss8dZ7pz/SA-TV.jpg", "https://tvsen6.aynaott.com/satv/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "SA TV delivers global and national news, live music events, and family drama serials."),
                ChannelSeed("somoy_tv_live", "Somoy TV", "https://i.postimg.cc/hvcWR1Yz/Somoy-TV.jpg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1702/output/1702-audio_113322_eng=113200-video=1692000.m3u8", "Bangladesh", "Somoy TV provides live bulletins, quick breaking headlines, and economic focus reports."),
                ChannelSeed("colors_bangla", "Colors Bangla HD", "https://i.postimg.cc/LsyGKzFC/Colors-Bangla-HD.jpg", "https://live20.bozztv.com/giatvplayout7/giatv-209627/index.m3u8", "Indian Bangla", "Colors Bangla HD brings popular Indian-Bengali mega serials, musical shows, and cinema blocks."),
                ChannelSeed("abp_ananda", "ABP Ananda", "https://i.postimg.cc/662vVv5x/ABP-Ananda.jpg", "https://abplivetv.akamaized.net/hls/live/2043012/ananda/master.m3u8", "Indian Bangla", "ABP Ananda is a premier Indian Bengali regional news channel based in Kolkata, West Bengal."),
                ChannelSeed("bangla_plus", "Bangla Plus", "https://i.postimg.cc/Wbkn7K32/Akash-Aath.jpg", "https://live-stream.utkalbongo.com/hls/livebanglatvstream.m3u8", "Indian Bangla", "Bangla Plus is a vibrant station featuring entertainment columns and classic cinema of West Bengal."),
                ChannelSeed("dd_bangla", "DD Bangla", "https://i.postimg.cc/MZQcv8Hm/Bangla-Jago.jpg", "https://d3qs3d2rkhfqrt.cloudfront.net/out/v1/7ff57cc9046b4c188b51a0d506f36e7f/index_3.m3u8", "Indian Bangla", "Doordarshan DD Bangla brings classical educational resources, news updates and cultural theater."),
                ChannelSeed("enter_10_bangla", "Enter 10 Bangla", "https://i.postimg.cc/qvPXDQ68/Enter10-Bangla.jpg", "https://live-bangla.akamaized.net/liveabr/pub-iobanglakp3sff/live_720p/chunks.m3u8", "Indian Bangla", "Enter 10 Bangla broadcasts dubbed animated shows, action series, and drama."),
                ChannelSeed("r_plus_news", "R Plus News", "https://i.postimg.cc/3wPnKf19/Sony-Aath.jpg", "https://thelegitpro.in/pntv/rplusnews24x7/index.m3u8", "Indian Bangla", "R Plus News brings round the clock updates on politics, finance and sports from Kolkata."),
                ChannelSeed("sony_aath", "Sony Aath", "https://i.postimg.cc/3wPnKf19/Sony-Aath.jpg", "https://live20.bozztv.com/giatvplayout7/giatv-209611/index.m3u8", "Indian Bangla", "Sony Aath is highly renowned for Gopal Bhar, visual cartoon blocks and suspense dramas."),
                ChannelSeed("tv9_bangla", "TV9 Bangla", "https://i.postimg.cc/tTNPLBMs/24-Ghanta.jpg", "https://dyjmyiv3bp2ez.cloudfront.net/pub-iotv9banaen8yq/liveabr/playlist.m3u8", "Indian Bangla", "TV9 Bangla provides high impact local journalism and investigative news updates."),
                ChannelSeed("zb_cinema", "ZB Cinema", "https://server.zillarbarta.com/ZBCINEMA/index.m3u8", "https://server.zillarbarta.com/ZBCINEMA/index.m3u8", "Indian Bangla", "ZB Cinema features spectacular round-the-clock Bengali movie broadcast blocks."),
                ChannelSeed("zb_news", "ZB News", "https://server.zillarbarta.com/zillarbarta/index.m3u8", "https://server.zillarbarta.com/zillarbarta/index.m3u8", "Indian Bangla", "ZB News provides Bengali regional news updates, talkshows and lifestyle reports."),
                ChannelSeed("b4u_kadak", "B4U Kadak", "https://i.postimg.cc/rmBYB7GQ/Bhojpuri-Cinema.jpg", "https://tvsen6.aynaott.com/B4U_Kadak/index.m3u8", "India", "B4U Kadak is a Hindi entertainment and movie channel showcasing blockbuster action films."),
                ChannelSeed("bhojpuri_cinema", "Bhojpuri Cinema", "https://i.postimg.cc/rmBYB7GQ/Bhojpuri-Cinema.jpg", "https://live-bhojpuri.akamaized.net/liveabr/playlist.m3u8", "India", "Bhojpuri Cinema broadcasts highly popular regional movies, music programs and live events."),
                ChannelSeed("dangal", "Dangal", "https://i.postimg.cc/J4n7YN2D/Colors.jpg", "https://live-dangal.akamaized.net/liveabr/playlist.m3u8", "India", "Dangal TV is India's most viewed free-to-air general entertainment channel showing beautiful mythological family serials."),
                ChannelSeed("dangal_2", "Dangal 2", "https://i.postimg.cc/J4n7YN2D/Colors.jpg", "https://live-dangal2.akamaized.net/liveabr/playlist.m3u8", "India", "Dangal 2 brings additional general entertainment dramas and romantic soaps 24/7."),
                ChannelSeed("goldmines", "Goldmines", "https://i.postimg.cc/J4n7YN2D/Colors.jpg", "https://tvsen6.aynaott.com/Goldmines/index.m3u8", "India", "Goldmines offers massive South Indian action films dubbed in top quality Hindi audio."),
                ChannelSeed("goldmines_bollywood", "Goldmines Bollywood", "https://i.postimg.cc/J4n7YN2D/Colors.jpg", "https://tvsen6.aynaott.com/GoldminesBollywood/index.m3u8", "India", "Goldmines Bollywood plays visual evergreen Indian movies, songs and retrospective programs."),
                ChannelSeed("goldmines_movies", "Goldmines Movies", "https://i.postimg.cc/J4n7YN2D/Colors.jpg", "https://tvsen6.aynaott.com/GoldminesMovies/index.m3u8", "India", "Goldmines Movies delivers additional continuous blockbusters, drama films, and action cinema."),
                ChannelSeed("shemaroo_tv", "Shemaroo TV", "https://i.postimg.cc/T3yTcTHZ/Sony-Max-HD.jpg", "https://tvsen6.aynaott.com/ShemarooTV/index.m3u8", "India", "Shemaroo TV is a popular Indian entertainment destination showcasing retro serials and comedy shows."),
                ChannelSeed("ninex_jalwa", "9X Jalwa", "https://i.postimg.cc/2S9YWq3f/9X-Jalwa.jpg", "https://d3kdywbtdfbp9z.cloudfront.net/v1/manifest/93ce20f0f52760bf38be911ff4c91ed02aa2fd92/dff423e0-3c82-46d6-9ecb-3baa96b5694a/70fca4d1-156e-4c03-baa4-9a4b602e33d5/0.m3u8", "Music", "9X Jalwa features evergreen Bollywood music hits and comedy segments of 90s, 00s."),
                ChannelSeed("ninex_jhakaas", "9X Jhakaas", "https://i.postimg.cc/154N6F39/9-X-Jhakaas.jpg", "https://d3kdywbtdfbp9z.cloudfront.net/v1/manifest/93ce20f0f52760bf38be911ff4c91ed02aa2fd92/dff423e0-3c82-46d6-9ecb-3baa96b5694a/ec26ed6c-3d9d-47d3-ac70-d23e781adbdf/0.m3u8", "Music", "9X Jhakaas features Marathi visual hits, upbeat music sequences, and celebrity columns."),
                ChannelSeed("ninex_tashan", "9X Tashan", "https://i.postimg.cc/RZJmcSt6/9XM.jpg", "https://d3kdywbtdfbp9z.cloudfront.net/v1/manifest/93ce20f0f52760bf38be911ff4c91ed02aa2fd92/dff423e0-3c82-46d6-9ecb-3baa96b5694a/c18ba53f-d825-4f26-971c-65ec6ab892cc/0.m3u8", "Music", "9X Tashan plays high-energy Punjabi pop, underground visual tracks, and artist blogs."),
                ChannelSeed("balle_balle", "Balle Balle", "https://i.postimg.cc/MGP3pRsh/Balle-Balle.jpg", "https://mcncdndigital.com/balleballetv/index.m3u8", "Music", "Balle Balle is a Punjabi music channel with top charts, regional pop hits, and dance loops."),
                ChannelSeed("baraza_music_tv", "Baraza Music TV", "https://i.imgur.com/rEhhYpe.png", "https://rtmp.streams.ovh:1936/barazarelax/barazazararelax/barazarelax/playlist.m3u8", "Music", "Baraza Music TV relax broadcasts ambient chill sounds, cozy synth loops and sleep soundscapes."),
                ChannelSeed("joo_music", "Joo Music", "https://i.imgur.com/MkFsM39.png", "https://livecdn.live247stream.com/joomusic/tv/playlist.m3u8", "Music", "Joo Music features continuous visual collections of indie tracks and mainstream chart-toppers."),
                ChannelSeed("love_does", "Love Does", "https://i.imgur.com/MkFsM39.png", "https://live20.bozztv.com/giatvplayout7/giatv-209587/index.m3u8", "Music", "Love Does plays romantic melodies, cozy visual albums and classic pop songs."),
                ChannelSeed("b4u_music", "B4U Music", "https://i.postimg.cc/MGP3pRsh/Balle-Balle.jpg", "https://cdn-uw2-prod.tsv2.amagi.tv/linear/amg01412-xiaomiasia-yrfmusic-xiaomi/playlist.m3u8", "Music", "B4U Music plays Indian film songs, chart topping visual releases, and artist interviews."),
                ChannelSeed("wion_live", "WION", "https://i.postimg.cc/MGP3pRsh/Balle-Balle.jpg", "https://d7x8z4yuq42qn.cloudfront.net/index_1.m3u8", "Music", "WION provides international breaking news from an Indian perspective, featuring global affairs columns."),
                ChannelSeed("zb_music", "ZB Music", "https://i.postimg.cc/RhgbbqFx/Star-Gold.jpg", "https://server.zillarbarta.com/zbmusic/index.m3u8", "Music", "ZB Music delivers non-stop playback of popular visual tracks, pop songs, and movie background scores."),
                ChannelSeed("zb_cartoon", "ZB Cartoon", "https://i.postimg.cc/FFwSjdzS/Pogo.jpg", "https://server.zillarbarta.com/zbcatun/video.m3u8", "Kids", "ZB Cartoon broadcasts high quality regional cartoons, moral fables and children visual stories."),
                ChannelSeed("bbc_cbeebies", "BBC Cbeebies", "https://i.postimg.cc/d3j5T2F8/Cartoon-Network.jpg", "https://live20.bozztv.com/giatvplayout7/giatv-209622/index.m3u8", "Kids", "BBC Cbeebies offers world-renowned early childhood educational comedies and interactive animations."),
                ChannelSeed("cartoon_network", "Cartoon Network", "https://i.postimg.cc/d3j5T2F8/Cartoon-Network.jpg", "https://live20.bozztv.com/giatvplayout7/giatv-209624/index.m3u8", "Kids", "Cartoon Network broadcasts timeless animated classics, superhero adventures, and comedic anime."),
                ChannelSeed("discovery_kids", "Discovery Kids", "https://i.postimg.cc/gjZzG3WD/Fight-Network.jpg", "https://live20.bozztv.com/giatvplayout7/giatv-209633/index.m3u8", "Kids", "Discovery Kids delivers educational programs, scientific experiments, and interesting animal documentaries."),
                ChannelSeed("jungle_book", "Jungle Book", "https://i.imgur.com/ubZMeQv.jpg", "https://cc-4bhi5osabejc9.akamaized.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-4bhi5osabejc9/junglebook.m3u8", "Kids", "The Jungle Book plays continuous epic cartoons about Mowgli, Bagheera and Baloo."),
                ChannelSeed("pbs_kids", "PBS Kids", "https://i.imgur.com/ubZMeQv.jpg", "https://2-fss-2.streamhoster.com/pl_140/amlst:200914-1298290/playlist.m3u8", "Kids", "PBS Kids features trustworthy children educational cartoon episodes, interactive music programs, and stories."),
                ChannelSeed("rongeen_tv", "Rongeen TV", "https://i.postimg.cc/zBCLNtGZ/Duronto.jpg", "https://server.thelegitpro.in/rongeentv/rongeentv/tracks-v1a1/mono.m3u8", "Kids", "Rongeen TV plays highly engaging regional moral comics, dubbed educational cartoons, and fun quizzes."),
                ChannelSeed("srk_tv", "SRK TV", "https://i.imgur.com/ubZMeQv.jpg", "https://srknowapp.ncare.live/srktvhlswodrm/srktv.stream/playlist.m3u8", "Kids", "SRK TV features high-morale animation loops, visual cartoon films, and interactive science cards."),
                ChannelSeed("action_hollywood_movies", "Action Hollywood Movies", "https://i.postimg.cc/02CNFVwv/Star-Movies-HD.jpg", "https://amg01076-lightningintern-actionhollywood-samsungnz-82rry.amagi.tv/playlist/amg01076-lightningintern-actionhollywood-samsungnz/playlist.m3u8", "Others", "Action Hollywood Movies operates round-the-clock action blockbuster feeds with thrilling cinematic visual edits."),
                ChannelSeed("wild_earth", "Wild Earth", "https://i.postimg.cc/HxDhtfTy/Wild-Earth.jpg", "https://wildearth-plex.amagi.tv/master.m3u8", "Infotainment", "Wild Earth features raw, live safaris, incredible wildlife captures, and close encounters with predators in South Africa."),
                ChannelSeed("aaj_tak_hd", "Aaj Tak HD", "https://i.imgur.com/e3cYtrb.jpg", "https://feeds.intoday.in/aajtak/api/aajtakhd/master.m3u8", "News", "Aaj Tak HD is a premium Hindi news network bringing real-time bulletins and hot political news."),
                ChannelSeed("breaking_news_feed", "Breaking News", "https://i.postimg.cc/ZnD8CtvQ/BBC.jpg", "https://cdn-ue1-prod.tsv2.amagi.tv/linear/amg02703-leadstory-leadstory-samsungau/playlist.m3u8", "News", "Live continuous global broadcast covering breaking world incidents, flash headlines, and emergency dispatches."),
                ChannelSeed("cgtn_live", "CGTN English", "https://i.postimg.cc/ZnD8CtvQ/BBC.jpg", "https://0472.org/hls/cgtn.m3u8", "News", "CGTN English provides comprehensive international news updates paired with deep geopolitical documentaries from Beijing."),
                ChannelSeed("cna_news", "CNA News", "https://i.postimg.cc/vH3MKVPY/CNN.jpg", "https://d2e1asnsl7br7b.cloudfront.net/7782e205e72f43aeb4a48ec97f66ebbe/index_5.m3u8", "News", "Channel NewsAsia brings real-time Asian business briefs, stock market charts, and global updates."),
                ChannelSeed("cnn_live", "CNN (US)", "https://i.postimg.cc/vH3MKVPY/CNN.jpg", "https://turnerlive.warnermediacdn.com/hls/live/586495/cnngo/cnn_slate/VIDEO_0_3564000.m3u8", "News", "CNN US Live Feed delivers breaking national headlines, comprehensive election maps, and investigative features."),
                ChannelSeed("global_news_us", "Global News (US)", "https://i.imgur.com/kcMUGOE.jpg", "https://live.corusdigitaldev.com/groupb/live/3062d0e3-ed4c-4f47-8482-95648250f4b8/live.isml/.m3u8", "News", "Global News US covers international updates, financial summaries, and local meteorological reports."),
                ChannelSeed("india_today_live", "India Today", "https://indiatodaylive.akamaized.net/hls/live/2014320/indiatoday/indiatodaylive/playlist.m3u8", "https://indiatodaylive.akamaized.net/hls/live/2014320/indiatoday/indiatodaylive/playlist.m3u8", "News", "India Today live features prime time arguments, economic updates, and investigative stories."),
                ChannelSeed("ndtv_english", "NDTV English", "https://ndtv24x7elemarchana.akamaized.net/hls/live/2003678/ndtv24x7/master.m3u8", "https://ndtv24x7elemarchana.akamaized.net/hls/live/2003678/ndtv24x7/master.m3u8", "News", "NDTV 24x7 is a trusted Indian English news station delivering in-depth political discussions."),
                ChannelSeed("ndtv_hindi", "NDTV Hindi", "https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8", "https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8", "News", "NDTV India provides high-quality Hindi reporting, ground realities, and talkshows."),
                ChannelSeed("news_max_2", "News Max 2", "https://i.postimg.cc/RZJmcSt6/9XM.jpg", "https://nmxlive.akamaized.net/hls/live/529965/Live_1/index.m3u8", "News", "News Max 2 features continuous debates, political breakdowns, and opinion columns."),
                ChannelSeed("tv9_india", "TV9 India", "https://i.imgur.com/e3cYtrb.jpg", "https://dyjmyiv3bp2ez.cloudfront.net/pub-iotv9hinjzgtpe/liveabr/playlist.m3u8", "News", "TV9 Bharatvarsh reports round-the-clock national headlines and controversial debates in Hindi."),
                ChannelSeed("yahoo_news_feed", "Yahoo News", "https://yahoo-samsung.amagi.tv/playlist.m3u8", "https://yahoo-samsung.amagi.tv/playlist.m3u8", "News", "Yahoo News brings global lifestyle trends, digital features, tech releases, and politics."),
                ChannelSeed("sanskar_tv", "Sanskar TV", "https://d26idhjf0y1p2g.cloudfront.net/out/v1/cd66dd25b9774cb29943bab54bbf3e2f/index.m3u8", "https://d26idhjf0y1p2g.cloudfront.net/out/v1/cd66dd25b9774cb29943bab54bbf3e2f/index.m3u8", "Religion", "Sanskar TV plays visual spiritual chants, yoga classes, and motivational guides."),
                ChannelSeed("zb_bhakti", "ZB Bhakti", "https://server.zillarbarta.com/zbbhakti/index.m3u8", "https://server.zillarbarta.com/zbbhakti/index.m3u8", "Religion", "ZB Bhakti features regional Indian devotionals, morning prayers, and religious epics."),
                ChannelSeed("discover_pakistan", "Discover Pakistan", "https://livecdn.live247stream.com/discoverpakistan/web/playlist.m3u8", "https://livecdn.live247stream.com/discoverpakistan/web/playlist.m3u8", "Pakistan", "Discover Pakistan showcases historic landmarks, cultural festivals, and beautiful mountain peaks."),
                ChannelSeed("lahore_news", "Lahore News", "https://vcdn.dunyanews.tv/lahorelive/ngrp:lnews_1_all/playlist.m3u8", "https://vcdn.dunyanews.tv/lahorelive/ngrp:lnews_1_all/playlist.m3u8", "Pakistan", "Lahore News live keeps viewers updated on regional infrastructure, policies and local sports."),
                ChannelSeed("such_tv", "Such TV", "https://video.primexsports.com/suchnews/live/playlist.m3u8", "https://video.primexsports.com/suchnews/live/playlist.m3u8", "Pakistan", "Such TV reports international and national headlines 24 hours a day with accurate summaries."),
                ChannelSeed("cartoon_network_usa", "Cartoon Network USA", "https://cdn.tvpassport.com/image/station/240x135/v2/s12131_h15_aa.png", "https://vodzong.mjunoon.tv:8087/streamtest/cartoon-network-87/playlist.m3u8", "Kids", "Cartoon Network USA feeds continuous action-hero episodes and contemporary comic block cartoons."),
                ChannelSeed("duck_tv", "Duck TV", "https://static.wikia.nocookie.net/logopedia/images/b/bc/Duck_TV.svg", "https://d6lk10bkdgfae.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-n0stmvwxmsf5c/playlist.m3u8", "Kids", "Duck TV provides baby-friendly colorful shapes, relaxing music, and early learning segments."),
                ChannelSeed("kids_flix", "KIDS FLIX", "https://www.kidsflix.com/themes/custom/viva/kidsflix/logo.png", "https://stream-us-east-1.getpublica.com/playlist.m3u8?network_id=50", "Kids", "Kids Flix plays a rich library of visual animated movies, fun quizzes, and adventure cartoons."),
                ChannelSeed("live_football", "Live FOOTBALL", "https://bugsfreeweb.github.io/LiveTVCollector/BugsfreeLogo/default-logo.png", "https://rmtv.akamaized.net/hls/live/2043154/rmtv-en-web/bitrate_3.m3u8", "Sports", "Live FOOTBALL delivers soccer matches, stadium coverage, pre match lineups, and historic goals."),
                ChannelSeed("cricket_gold", "Cricket Gold", "https://d229kpbsb5jevy.cloudfront.net/tv/150/150/bnw/Cricket-Gold-Channel_black.png", "https://d1nj4u39ja4cn0.cloudfront.net/v1/master/9d062541f2ff39b5c0f48b743c6411d25f62fc25/FLS-MuxIP-CricketGold/418.m3u8", "Sports", "Cricket Gold plays legendary historic cricket matches, classic bowling saves, and masterclasses."),
                ChannelSeed("delicious_infotainment", "Delicious", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1778086478755.png", "https://nomawnoijl.gpcdn.net/akash/delicious/playlist.m3u8", "Infotainment", "Delicious features culinary masterclasses, global travel recipes, and cozy restaurant reviews."),
                ChannelSeed("ary_digital_hd", "ARY Digital HD", "https://static.wikia.nocookie.net/logopedia/images/0/0e/ARY_Digital.png", "https://ml-pull-dvc-myco.io:2096/ARY_DIGITAL/tracks-v4a1/mono.ts.m3u8", "Pakistan", "ARY Digital HD operates premium Pakistani family serials, visual reality programs, and comedy blocks."),
                ChannelSeed("express_tv", "Express TV", "https://static.wikia.nocookie.net/logopedia/images/e/e8/Express_Entertainment.png", "https://live.thebosstv.com:30443/dwlive/EXP-ENTERTAINMENT/chunks.m3u8", "Pakistan", "Express TV features popular lifestyle serials, musical collections, and visual dramas."),
                ChannelSeed("geo_tv", "GEO TV", "https://static.wikia.nocookie.net/logopedia/images/d/d6/Geo_TV.png", "https://jk3lz82elw79-hls-live.5centscdn.com/harPalGeo/955ad3298db330b5ee880c2c9e6f23a0.sdp/playlist.m3u8", "Pakistan", "GEO TV is renowned for top-tier family fiction serials, cultural plays, and prime talkshows."),
                ChannelSeed("disney_channel_hd", "Disney Channel HD", "https://static.wikia.nocookie.net/logopedia/images/1/11/Disney_Channel_India_HD.png", "https://master-proxy.wispy-boat-fc77.workers.dev/?url=https://t.freetv.fun/live/disney-channel.m3u8", "Kids", "Disney Channel HD plays blockbuster animated series, family teenage sitcoms, and kids fantasy serials."),
                ChannelSeed("tom_and_jerry", "Tom & Jerry", "https://i.imgur.com/GXzqIYy.png", "https://live20.bozztv.com/giatvplayout7/giatv-208314/playlist.m3u8", "Kids", "Tom & Jerry runs continuous visual feeds of legendary cat and dog comedic chase battles."),
                ChannelSeed("gopal_bhar_live", "Gopal Bhar", "https://i.imgur.com/HzfIL6z.jpeg", "https://live20.bozztv.com/giatvplayout7/giatv-209611/tracks-v1a1/mono.ts.m3u8", "Kids", "Gopal Bhar features regional historical comic animations about custom humor, moral stories, and fables."),
                ChannelSeed("knowledge_network", "Knowledge Network", "https://i.imgur.com/wtuoS93.png", "https://d1wal6k3d7ssin.cloudfront.net/out/v1/ea91db0906c847a4931b46a9ec36e77b/index_2.m3u8", "Kids", "Knowledge Network streams beautiful space explorations, basic science visuals, and math riddles."),
                ChannelSeed("tiny_pop_live", "Tiny Pop", "https://static.wikia.nocookie.net/logopedia/images/7/7e/Tiny_Pop_2018.svg", "https://amg01753-narrativeuk-amg01753c1-rakuten-uk-2339.playouts.now.amagi.tv/480p/index.m3u8", "Kids", "Tiny Pop features colorful preschoolers moral stories, animal cartoons, and puzzle games."),
                ChannelSeed("makkah_live", "Makkah Live", "https://i.imgur.com/colOISC.jpeg", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/makkah.stream/tracks-v1a1/mono.m3u8", "Islamic", "Makkah Live delivers official 24-hour video fee of holy Grand Mosque prayers, Taraweeh, and Quran chants."),
                ChannelSeed("madina_live", "Madina Live", "https://images-na.ssl-images-amazon.com/images/I/71CywdrFaZL.png", "https://cdn-globecast.akamaized.net/live/eds/saudi_sunnah/hls_roku/index.m3u8", "Islamic", "Madina Live brings live broadcast streams of Al-Masjid an-Nabawi prayers, sermons and peace segments."),
                ChannelSeed("peace_tv_bangla", "Peace Tv Bangla", "https://www.jagobd.com/wp-content/uploads/2024/08/logo_50.png", "https://dzkyvlfyge.erbvr.com/PeaceTvBangla/tracks-v3a1/mono.m3u8", "Islamic", "Peace Tv Bangla delivers educational speech sessions, moral principles discussions and religious studies in Bengali."),
                
                // Bangladesh requested premium additions
                ChannelSeed("btv_bangladesh", "BTV", "https://www.btvlive.gov.bd/_next/image?url=https%3A%2F%2Fd38ll44lbmt52p.cloudfront.net%2Fcms%2Fchannel_poster%2F1677040358634_BTVogo.png&w=1920&q=75", "https://owrcovcrpy.gpcdn.net/bpk-tv/1709/output/index.m3u8", "Bangladesh", "BTV brings public programming, news and cultural broadcasts directly to viewers in Bangladesh and worldwide."),
                ChannelSeed("anirban_bangladesh", "Anirban", "https://i.imgur.com/rwd1rei.png", "https://live20.bozztv.com/giatvplayout7/giatv-209627/tracks-v1a1/mono.ts.m3u8", "Bangladesh", "Anirban features family shows, local music loops, and cultural programs."),
                ChannelSeed("banglavision_bangladesh", "Banglavision", "https://i.postimg.cc/JzGFfbYb/20250529-072833.png", "https://owrcovcrpy.gpcdn.net/bpk-tv/1715/output/index.m3u8", "Bangladesh", "Banglavision offers premier Bengali entertainment, native serials, and live bulletins."),
                ChannelSeed("channel9_bangladesh", "Channel9", "https://www.jagobd.com/wp-content/uploads/2015/10/ch9-150x150.jpg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1729/output/index.m3u8", "Bangladesh", "Channel9 is a popular satellite channel in Bangladesh featuring general entertainment and sports reviews."),
                ChannelSeed("channels_bangladesh", "CHANNEL S", "https://www.jagobd.com/wp-content/uploads/2024/08/chsbd.jpg", "https://app.ncare.live/live-orgin/channels.stream/playlist.m3u8", "Bangladesh", "CHANNEL S broadcasts news, talkshows and lifestyle content from around the globe."),
                ChannelSeed("dbc_news_bangladesh", "DBC NEWS", "https://www.jagobd.com/wp-content/uploads/2017/01/dbc-news.jpg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1728/output/index.m3u8", "Bangladesh", "DBC NEWS reports national and international affairs 24 hours a day with accurate summaries."),
                ChannelSeed("ekhon_tv_bangladesh", "Ekhon TV", "https://www.jagobd.com/wp-content/uploads/2024/08/ekhontv.jpg", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/globaltv.stream/tracks-v1a1/mono.m3u8", "Bangladesh", "Ekhon TV is a business-focused Bengali satellite channel bringing finance and commerce news."),
                ChannelSeed("ekushey_tv_bangladesh", "Ekushey TV", "https://i.postimg.cc/L6R2F0jj/20250529_105209.png", "https://master-proxy.wispy-boat-fc77.workers.dev/?url=https://ekusheyserver.com/hls-live/livepkgr/_definst_/liveevent/livestream3.m3u8", "Bangladesh", "Ekushey TV broadcasts educational content, lifestyle magazines, and drama series."),
                ChannelSeed("gazi_tv_bangladesh", "Gazi TV", "https://i.imgur.com/yTMY0wW.png", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/gazibdz.stream/tracks-v1a1/mono.m3u8", "Bangladesh", "Gazi TV is a highly popular target for general entertainment and cricket matches overview."),
                ChannelSeed("maasranga_bangladesh", "Maasranga", "https://i.imgur.com/s49AcXv.jpeg", "https://bozztv.com/rongo/rongo-Maasranga/index.m3u8", "Bangladesh", "Maasranga features quality Bengali serialized dramas, kid series, and visual magazines."),
                ChannelSeed("mohona_tv_bangladesh", "Mohona TV", "https://i.imgur.com/ENyPcL0.png", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/mohonatv.stream/tracks-v1a1/mono.m3u8", "Bangladesh", "Mohona TV is an entertainment channel presenting custom cultural and family columns."),
                ChannelSeed("movie_bangla_bangladesh", "Movie Bangla", "https://i.postimg.cc/wBN1BtXk/20251127_095813.png", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/moviebanglalink2.stream/tracks-v1a1/mono.m3u8", "Bangladesh", "Movie Bangla features continuous blockbusters, drama films, and action cinema from West Bengal and Bangladesh."),
                ChannelSeed("mytv_bangladesh", "MY TV", "https://i.postimg.cc/HxGF4V2b/20250529_103226.png", "https://app24.jagobd.com.bd/c3VydmVyX8RpbEU9Mi8xNy8yMFDEEHGcfRgzQ6NTAgdEoaeFzbF92YWxIZTO0U0ezN1IzMyfvcEdsEfeDeKiNkVN3PTOmdFseWRtaW51aiPhnPTI2/mytv-up-off.stream/tracks-v1a1/mono.m3u8", "Bangladesh", "MY TV plays popular entertainment shows, local songs, and talkshows."),
                ChannelSeed("satv_bangladesh", "SATV", "https://i.imgur.com/j8JlVp7.jpeg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1720/output/index.m3u8", "Bangladesh", "SATV delivers global and national news, live music events, and family drama serials."),
                ChannelSeed("sangsad_television_bangladesh", "Sangsad Television", "https://i.imgur.com/boekJnD.png", "https://owrcovcrpy.gpcdn.net/bpk-tv/1713/output/index.m3u8", "Bangladesh", "Sangsad Television is the state-run broadcaster of standard parliamentary assemblies."),

                // All list requested additions (mapped nicely into correct groups)
                ChannelSeed("cnn_news_all", "CNN", "https://s3.aynaott.com/storage/e0b6da4715f468eb39591911a0597546", "https://tvsen6.aynaott.com/cnn/index.m3u8?e=1780908430&u=78be6644-0a65-48ec-81a4-089ac65a2619&token=cbe63cedf7533ca70c8c9ef9e917100f", "News", "CNN is a premier international news channel delivering live headlines and investigative journalism worldwide."),
                ChannelSeed("trt_world_all", "TRT World", "https://s3.aynaott.com/storage/f63d4aad95532175f7f44be439f74111", "https://tv-trtworld.medya.trt.com.tr/master.m3u8", "News", "TRT World provides comprehensive international stories and documentary films."),
                ChannelSeed("sony_atth_all", "Sony ATTH", "https://s3.aynaott.com/storage/f7bfca4f0a3860067bf2eac37f41214c", "https://live20.bozztv.com/giatvplayout7/giatv-209611/index.m3u8", "Music", "Sony ATTH is highly renowned for visual cartoon blocks and suspense dramas."),
                ChannelSeed("hindi_movie_classic_24_all", "Hindi Movie Classic 24", "https://s3.aynaott.com/storage/3132515182ec50091b496fe515564084", "https://vods2.aynaott.com/hindimovies/index.m3u8", "Movie", "Hindi Movie Classic 24 plays beautiful vintage Indian hit movies."),
                ChannelSeed("dd_bangla_all", "DD Bangla", "https://s3.aynaott.com/storage/e5117c508d18adf0a3f2475eb1fd5a9d", "https://d3qs3d2rkhfqrt.cloudfront.net/out/v1/7ff57cc9046b4c188b51a0d506f36e7f/index_3.m3u8", "Bangladesh", "Doordarshan DD Bangla brings classical educational resources, news updates and cultural theater."),

                // Sports requested premium additions
                ChannelSeed("t_sports_hd", "T Sports HD", "https://s3.aynaott.com/storage/dbc585f70a60b9855b6e13a8ce4cb6f4", "https://tvsen7.aynaott.com/tsports-hd/index.m3u8?e=1780908430&u=78be6644-0a65-48ec-81a4-089ac65a2619&token=63d6829ce965f7f82528b03d97909362", "Sports", "T Sports HD is Bangladesh's first premier sports channel broadcasting live events and updates."),
                ChannelSeed("t_sports_720p", "T Sports (720p)", "https://s3.aynaott.com/storage/dbc585f70a60b9855b6e13a8ce4cb6f4", "https://tvsen7.aynaott.com/tsportsfhd/index.m3u8", "Sports", "T Sports (720p) is a high definition channel for all popular regional games."),
                ChannelSeed("t_sports_hd_alt", "T Sports (720p) Alt", "https://s3.aynaott.com/storage/dbc585f70a60b9855b6e13a8ce4cb6f4", "https://tvsen7.aynaott.com/tsports-hd/index.m3u8", "Sports", "Alternate high speed stream for real-time sports coverage."),
                ChannelSeed("somoy_fifa", "Somoy FIFA World Cup 2026", "https://i.postimg.cc/hvcWR1Yz/Somoy-TV.jpg", "https://owrcovcrpy.gpcdn.net/bpk-tv/1702/output/1702-audio_113322_eng=113200-video=2202800.m3u8", "Sports", "Dedicated action stream covering prep and matches for the FIFA World Cup 2026."),

                // Movies requested premium additions
                ChannelSeed("cineedge_hd_movie", "Cineedge HD", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770347851305.png", "https://nomawnoijl.gpcdn.net/akash/cineedge/playlist.m3u8", "Movie", "Cineedge HD streams cinematic films and drama around the clock."),
                ChannelSeed("uniques_hd_movie", "Uniques HD", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770347327658.png", "https://nomawnoijl.gpcdn.net/akash/uniques/playlist.m3u8", "Movie", "Uniques HD features specialized indie film blocks and romantic soaps."),
                ChannelSeed("superrix_hd_movie", "Superrix HD", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770348388925.png", "https://nomawnoijl.gpcdn.net/akash/superrix/playlist.m3u8", "Movie", "Superrix HD brings blockbuster Hollywood and regional action blockbusters."),
                ChannelSeed("screem_movie_movie", "Screem", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770312098339.png", "https://nomawnoijl.gpcdn.net/akash/screem/playlist.m3u8", "Movie", "Screem features classic suspense, thriller and action-packed motion pictures."),
                ChannelSeed("crimes_movie_movie", "Crimes", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770380126540.png", "https://nomawnoijl.gpcdn.net/akash/crimes/playlist.m3u8", "Movie", "Crimes features detective series, investigations, and mystery films."),
                ChannelSeed("truestories_movie_movie", "True Stories", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770380306806.png", "https://nomawnoijl.gpcdn.net/akash/truestories/playlist.m3u8", "Movie", "True Stories covers inspiring documentaries, lifestyle features, and real life dramas."),
                ChannelSeed("intelligence_movie_movie", "Intelligence", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1770380460488.png", "https://nomawnoijl.gpcdn.net/akash/intelligence/playlist.m3u8", "Movie", "Intelligence is a specialized network for technological features, detective movies and scifi."),
                ChannelSeed("originals_movie_movie", "Originals", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1778085327477.png", "https://nomawnoijl.gpcdn.net/akash/originals/playlist.m3u8", "Movie", "Originals presents top director collaborations and custom theatrical releases."),
                ChannelSeed("moviesphere_movie_movie", "Movie Sphere", "https://s3.aynaott.com/storage/4d343b446b1e7164bb7239bbe822a570", "https://moviesphereuk-samsunguk.amagi.tv/playlist.m3u8", "Movie", "Movie Sphere operates 24/7 blockbuster film feeds with cinematic edits."),
                ChannelSeed("rakuten_movies_movie", "Rakuten Movies", "https://s3.aynaott.com/storage/22af43810a37af9a151f1e0a23adde63", "https://0145451975a64b35866170fd2e8fa486.mediatailor.eu-west-1.amazonaws.com/v1/master/0547f18649bd788bec7b67b746e47670f558b6b2/production-LiveChannel-5987/master.m3u8", "Movie", "Rakuten Movies broadcasts premier hit films and entertainment reviews."),
                ChannelSeed("cowboy_movie_movie", "Cowboy Movie Channel", "https://s3.aynaott.com/storage/cb1ab052f5b733bbf600a7a8e3a1164b", "https://streams2.sofast.tv/sofastplayout/32eb332e-f644-46e5-ad91-e55ad80d14f7_0_HLS/master.m3u8", "Movie", "Cowboy Movie Channel plays continuous western retro cinema and cowboy tales."),
                ChannelSeed("action_hollywood_movie_alt2", "Action Hollywood Movies", "https://s3.aynaott.com/storage/baef6dd41c3ee6fabbb59bb8403cc1eb", "https://cdn-apse1-prod.tsv2.amagi.tv/linear/amg01076-lightningintern-actionhollywood-samsungnz/playlist.m3u8", "Movie", "Thrilling top speed blockbuster feature feeds."),
                ChannelSeed("hbo_movie_movie", "HBO", "https://s3.aynaott.com/storage/4a1291716680b5c095d33e106337bb04", "https://tvsen5.aynaott.com/hbo/index.m3u8?e=1780908432&u=78be6644-0a65-48ec-81a4-089ac65a2619&token=5d0a9956aa405c54c55ce7f00696d073", "Movie", "HBO streams Hollywood blockbusters, award-winning original shows, and family feature films."),
                ChannelSeed("hbo2_movie_movie", "HBO 2", "https://s3.aynaott.com/storage/b64c028d8c0895ed81f3201d5979f7ba", "https://tvsen7.aynaott.com/hbo2/index.m3u8?e=1780908433&u=78be6644-0a65-48ec-81a4-089ac65a2619&token=f920162f150a758ffdde005e0e721395", "Movie", "HBO 2 brings additional blockbuster movie selections and premier TV content."),
                ChannelSeed("khushboo_movie_movie", "Khushboo", "https://s3.aynaott.com/storage/d90eb718a6674494d2dac28e1d96bf44", "https://cdn-4.pishow.tv/live/1473/master.m3u8", "Movie", "Khushboo plays beautiful evergreen romantic melodies and films."),
                ChannelSeed("abn_movie_movie", "ABN", "https://s3.aynaott.com/storage/9882913a8d68aa99c0501b64749d6320", "https://mediaserver.abnvideos.com/streams/abnurdu.m3u8", "Movie", "ABN features high quality regional cinematic blocks."),
                ChannelSeed("persiana_korea_movie_movie", "Persiana Korea", "https://s3.aynaott.com/storage/f24e50516ccf6b3e94a4ca749ccb3533", "https://korhls.persiana.live/hls/stream.m3u8", "Movie", "Persiana Korea plays premium dubbed cinema, K-dramas, and romantic soaps."),
                ChannelSeed("sheemaroo_boll", "Sheemaroo Bollywood", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://cdn-uw2-prod.tsv2.amagi.tv/linear/amg00864-shemarooenterta-shemabollywood-ono/playlist.m3u8", "Movie", "Sheemaroo Bollywood plays evergreen Indian cinema blocks."),
                ChannelSeed("zb_cinema_movie_movie", "ZB Cinema", "https://server.zillarbarta.com/ZBCINEMA/tracks-v1a1/mono.ts.m3u8", "https://server.zillarbarta.com/ZBCINEMA/tracks-v1a1/mono.ts.m3u8", "Movie", "ZB Cinema features premium regional Bengali movie broadcast blocks."),
                ChannelSeed("action_hollywood_alt3", "Action Hollywood Movies (Alt)", "https://s3.aynaott.com/storage/baef6dd41c3ee6fabbb59bb8403cc1eb", "https://amg01076-lightningintern-actionhollywood-samsungnz-82rry.amagi.tv/playlist/amg01076-lightningintern-actionhollywood-samsungnz/playlist.m3u8", "Movie", "Action Hollywood Movies operates continuous hit films."),
                ChannelSeed("goldmines_movie_m", "Goldmines", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://cdn-2.pishow.tv/live/1459/master.m3u8", "Movie", "Goldmines offers massive action films dubbed in top quality Hindi audio."),
                ChannelSeed("goldmines_movies_m", "Goldmines Movies", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://cdn-2.pishow.tv/live/1461/master.m3u8", "Movie", "Goldmines Movies delivers additional continuous blockbusters."),
                ChannelSeed("b4u_kadak_movie", "B4U Kadak", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://cdn-2.pishow.tv/live/227/master.m3u8", "Movie", "B4U Kadak is a Hindi entertainment and movie channel showcasing films."),
                ChannelSeed("goldmines_movies2_m", "Goldmines Movies 2", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://cdn-2.pishow.tv/live/1460/master.m3u8", "Movie", "Goldmines Movies 2 delivers continuous content."),
                ChannelSeed("south_movies_m", "South Movies", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://live20.bozztv.com/giatvplayout7/giatv-209593/tracks-v1a1/mono.ts.m3u8", "Movie", "South Movies broadcasts high quality dubbed regional Indian cinema."),
                ChannelSeed("hindi_movies_m", "Hindi Movies", "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=400&auto=format&fit=crop", "https://live20.bozztv.com/giatvplayout7/giatv-209612/tracks-v1a1/mono.ts.m3u8", "Movie", "Hindi Movie block showcasing premium entertainment and vintage hits."),
                ChannelSeed("goldmines_bollywood_m", "Goldmines Bollywood", "https://s3.aynaott.com/storage/e92e9e2fb70909f3dd30a8d89e644119", "https://tvsen6.aynaott.com/GoldminesBollywood/index.m3u8", "Movie", "Goldmines Bollywood plays visual evergreen Indian movies."),

                // Kids requested premium additions
                ChannelSeed("doraemon_bangla", "Doraemon", "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=400&auto=format&fit=crop", "https://live20.bozztv.com/giatvplayout7/giatv-209902/tracks-v1a1/mono.ts.m3u8", "Kids", "Doraemon plays continuous popular dubbed cartoons 24/7."),

                // User requested premium additions
                ChannelSeed("sony_entertainment_all", "Sony Entertainment", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1740380280065.png", "https://owrcovcrpy.gpcdn.net/bpk-tv/1718/output/index.mpd", "Entertainment", "Sony Entertainment Television offers premier Hindi dramas, popular reality shows, and comprehensive family entertainment."),
                ChannelSeed("zee_bangla_all", "ZEE BANGLA", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1756017182009.png", "https://owrcovcrpy.gpcdn.net/bpk-tv/1714/output/index.mpd", "Entertainment", "ZEE BANGLA brings top-rated Bengali serials, family drama series, special events, and musical reality shows."),
                ChannelSeed("sony_max_all", "Sony Max", "https://tstatic.akash-go.com/cms-ui/images/custom-content/1780310920616.png", "https://owrcovcrpy.gpcdn.net/bpk-tv/1730/output/index.mpd", "Movies", "Sony Max is a premier movie channel broadcasting blockbuster Bollywood cinema and action-packed entertainment.")
            )

            val mockVideos = rawSeeds.mapIndexed { idx, s ->
                val category = when (s.group) {
                    "Bangladesh", "Indian Bangla" -> "Bangladesh"
                    "Kids" -> "Kids"
                    "Islamic", "Religion" -> "Islamic"
                    "News" -> "News"
                    "Music" -> "Music"
                    "Sports" -> "Sports"
                    "Movies", "Movie" -> "Movie"
                    else -> "Others"
                }

                Video(
                    id = s.id,
                    title = "🔴 ${s.name} Live Broadcast",
                    description = s.desc,
                    creatorId = s.id,
                    creatorName = s.name,
                    creatorAvatar = s.logo,
                    videoUrl = s.url,
                    thumbnailUrl = s.logo,
                    likesCount = 200 + (idx * 17) % 500,
                    viewsCount = 1000 + (idx * 43) % 2000,
                    isLiked = false,
                    isLive = true,
                    category = category,
                    timestamp = System.currentTimeMillis() - (idx * 60000)
                )
            }.toMutableList()

            // Keep Fahad developer video
            mockVideos.add(
                Video(
                    id = "video_1",
                    title = "Building a Full Live-Video Streamer App with Jetpack Compose & Clean Architecture",
                    description = "In this comprehensive masterclass, we explore edge-to-edge UI layouts, local SQLite caching, nested custom canvas playback grids, and real-time asynchronous comment feeds, all using Android 36 with Material 3 design tokens.",
                    creatorId = "fahad_developer",
                    creatorName = "Fahad Hossain",
                    creatorAvatar = "android.resource://com.example/drawable/img_fahad",
                    videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?q=80&w=600&auto=format&fit=crop",
                    likesCount = 125,
                    viewsCount = 740,
                    isLiked = false,
                    isLive = false,
                    category = "Tech",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )

            streamDao.insertVideos(mockVideos)

            val mockPosts = listOf(
                Post(
                    id = "post_1",
                    content = "Hey everyone! Exciting news: I am preparing a special live masterclass tomorrow walking through the real-time websocket rendering used in active Twitch streams. What time works best for you all? Let me know in the comments! 👇",
                    creatorId = "fahad_developer",
                    creatorName = "Fahad Hossain",
                    creatorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=250&auto=format&fit=crop",
                    likesCount = 42,
                    isLiked = false,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 1800000
                ),
                Post(
                    id = "post_2",
                    content = "The grand stage is set for the Apex Legends Esports Grand Finals! Team BrightnessWorld is locked in and ready to dominate. Check out this view from our setup in Tokyo! 🤩",
                    creatorId = "apex_gaming",
                    creatorName = "Apex Esports Official",
                    creatorAvatar = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=250&auto=format&fit=crop",
                    likesCount = 1108,
                    isLiked = false,
                    commentsCount = 14,
                    imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?q=80&w=600&auto=format&fit=crop",
                    timestamp = System.currentTimeMillis() - 14400000
                )
            )
            streamDao.insertPosts(mockPosts)

            // Seed some mock comments
            val mockComments = listOf(
                Comment(
                    targetId = "stream_1",
                    authorName = "GamerChor_99",
                    authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100&auto=format&fit=crop",
                    content = "LET S GO SQUAD! TEAM RED IS COOKING!",
                    isLiveChat = true,
                    timestamp = System.currentTimeMillis() - 60000
                ),
                Comment(
                    targetId = "stream_1",
                    authorName = "StreamWatcher",
                    authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=100&auto=format&fit=crop",
                    content = "This stream quality is incredible in the new FBRODCUST app!",
                    isLiveChat = true,
                    timestamp = System.currentTimeMillis() - 40000
                ),
                Comment(
                    targetId = "stream_1",
                    authorName = "AndroidCoder",
                    authorAvatar = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=100&auto=format&fit=crop",
                    content = "Apex Finals is insane! Incredible clutched victory on Match 5",
                    isLiveChat = true,
                    timestamp = System.currentTimeMillis() - 20000
                ),
                Comment(
                    targetId = "stream_2",
                    authorName = "CodeNoodle",
                    authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100&auto=format&fit=crop",
                    content = "Loving this track, perfectly styled lofi vibes! ☕☕☕",
                    isLiveChat = true,
                    timestamp = System.currentTimeMillis() - 110000
                ),
                Comment(
                    targetId = "stream_2",
                    authorName = "MidnightHacker",
                    authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=100&auto=format&fit=crop",
                    content = "Working on my Compose layouts, this music keeps my brain focused.",
                    isLiveChat = true,
                    timestamp = System.currentTimeMillis() - 50000
                ),
                Comment(
                    targetId = "post_1",
                    authorName = "Rashed007",
                    authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=100&auto=format&fit=crop",
                    content = "Definitely 7 PM EST! Can't wait Fahad!",
                    isLiveChat = false,
                    timestamp = System.currentTimeMillis() - 1200000
                ),
                Comment(
                    targetId = "post_1",
                    authorName = "AlexM",
                    authorAvatar = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?q=80&w=100&auto=format&fit=crop",
                    content = "Will we see live canvas drawings? Totally hyped!",
                    isLiveChat = false,
                    timestamp = System.currentTimeMillis() - 600000
                )
            )
            for (comment in mockComments) {
                streamDao.insertComment(comment)
            }
        }
        } catch (e: Exception) {
            android.util.Log.e("StreamRepository", "Error seeding database", e)
        }
    }
}
