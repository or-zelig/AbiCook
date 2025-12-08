package il.co.or.abicook.domain.model

data class RecipePost(
    val id: String,
    val title: String,
    val authorName: String,
    val likes: Int,
    val commentsCount: Int,
    val imageUrl: String?,
    val isLikedByMe: Boolean = false
)
