package il.co.or.abicook.data.model

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredientsSummary: String = "",
    val stepsSummary: String = "",
    val createdAtMillis: Long = 0L,
    val authorId: String = ""           // NEW
)
