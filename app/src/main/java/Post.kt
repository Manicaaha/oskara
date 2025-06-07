package com.example.oskarchatter

data class Post(
    val id: String = "",
    val username: String = "",
    val content: String = "",
    val avatarUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val userId: String = "",
    var likeCount: Int = 0
)
