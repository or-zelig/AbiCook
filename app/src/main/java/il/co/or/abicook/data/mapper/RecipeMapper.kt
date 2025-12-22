package il.co.or.abicook.data.mapper

import il.co.or.abicook.data.model.Recipe
import il.co.or.abicook.domain.model.RecipePost

object RecipeMapper {
    fun Recipe.toPost(currentUserId: String? = null): RecipePost {
        return RecipePost(
            id = id,
            title = title,
            description = description,
            ingredientsSummary = ingredientsSummary,
            stepsSummary = stepsSummary,
            createdAtMillis = createdAtMillis,
            authorId = authorId,

            authorName = authorName.ifBlank { "Unknown" },
            imageUrl = imageUrl,
            likes = likes,
            commentsCount = commentsCount,
            isLikedByMe = false, // בהמשך נעשה לייקים אמיתי לפי user
            category = primaryCategory,
            prepTime = prepTimeMin,
            cookTime = cookTimeMin
        )
    }
}
