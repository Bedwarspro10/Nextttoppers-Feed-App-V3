package com.example.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("youtube/channel-videos")
    suspend fun getChannelVideos(
        @Query("channelId") channelId: String,
        @Query("maxResults") maxResults: Int = 20
    ): List<YouTubeVideo>

    @GET("youtube/search")
    suspend fun searchYouTubeVideos(
        @Query("channelId") channelId: String,
        @Query("query") query: String,
        @Query("maxResults") maxResults: Int = 20
    ): List<YouTubeVideo>

    @POST("contact")
    suspend fun submitContactMessage(
        @Body message: ContactMessageRequest
    ): Any // Usually returns a 200 OK void or success message
}

data class YouTubeVideo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val publishedAt: String,
    val channelTitle: String,
    val viewCount: String?,
    val duration: String?
)

data class ContactMessageRequest(
    val name: String,
    val email: String,
    val subject: String?,
    val message: String
)
