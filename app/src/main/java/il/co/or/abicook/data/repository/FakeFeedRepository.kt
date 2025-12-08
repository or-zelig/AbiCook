package il.co.or.abicook.data.repository

import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.delay

class FakeFeedRepository : FeedRepository {

    override suspend fun getHomeFeed(): List<RecipePost> {
        // סימולציה של רשת איטית
        delay(800)

        return listOf(
            RecipePost(
                id = "1",
                title = "Creamy Mushroom Pasta",
                authorName = "AbiCook",
                likes = 24,
                commentsCount = 5,
                imageUrl = null
            ),
            RecipePost(
                id = "2",
                title = "Homemade Pizza Margherita",
                authorName = "Chef Alex",
                likes = 41,
                commentsCount = 12,
                imageUrl = null
            ),
            RecipePost(
                id = "3",
                title = "Chocolate Lava Cake",
                authorName = "Sweet Tooth",
                likes = 67,
                commentsCount = 18,
                imageUrl = null
            )
        )
    }
}
