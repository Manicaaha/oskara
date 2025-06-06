data class Post(val id: String?, val username: String, val content: String, val avatarUrl: String?, val imageUrls: List<String> = emptyList(), var likeCount: Int = 0)
