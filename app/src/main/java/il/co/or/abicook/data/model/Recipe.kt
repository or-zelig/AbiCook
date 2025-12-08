package il.co.or.abicook.data.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val ingredientsSummary: String,
    val stepsSummary: String,
    val createdAtMillis: Long
    // בעתיד נוסיף: categories, times, imageUrl, authorId וכו'
)
