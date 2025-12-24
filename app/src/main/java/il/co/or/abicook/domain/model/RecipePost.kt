package il.co.or.abicook.domain.model

data class RecipePost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,

    val authorId: String = "",
    val authorName: String = "",

    val createdAtMillis: Long = 0L,

    val likes: Long = 0L,
    val commentsCount: Long = 0L,
    val isLikedByMe: Boolean = false,

    // לסיכומים (כמו מה שיש לך ב-Firestore כרגע)
    val ingredientsSummary: String = "",
    val stepsSummary: String = "",

    // קטגוריות/זמנים – שים default כדי שלא יתפוצץ
    val primaryCategory: String = "",
    val categories: List<String> = emptyList(),
    val prepTimeMin: Int = 0,
    val cookTimeMin: Int = 0
)
