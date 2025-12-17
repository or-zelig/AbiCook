package il.co.or.abicook.domain.model

data class RecipePost(
    val id: String = "",
    val title: String = "",

    // מה שכבר יש לך במסד לפי הסקרין: description/createdAtMillis/authorId/summaries
    val description: String = "",
    val ingredientsSummary: String = "",
    val stepsSummary: String = "",
    val createdAtMillis: Long = 0L,
    val authorId: String = "",

    // שדות לפיד/UI (אפשר להוסיף בהמשך בלי לשבור כלום)
    val imageUrl: String? = null,
    val authorName: String = "Unknown",
    val likes: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,

    // בשביל Feed V2 (פילטור)
    val category: String = "",
    val prepTime: Int = 0,
    val cookTime: Int = 0
) {
    val totalTime: Int get() = prepTime + cookTime
}
