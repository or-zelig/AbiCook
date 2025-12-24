package il.co.or.abicook.presentation.home

data class RecipeUiItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,

    val createdAtMillis: Long = 0L,
    val likes: Int = 0,

    val totalTimeMinutes: Int = 0, // לצורך הפילטר שלך
    val categories: List<String> = emptyList(),

    val authorId: String? = null,
    val ingredientsSummary: String? = null,
    val stepsSummary: String? = null
)
