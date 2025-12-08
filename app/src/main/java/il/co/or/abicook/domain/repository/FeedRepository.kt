package il.co.or.abicook.domain.repository

import il.co.or.abicook.domain.model.RecipePost

interface FeedRepository {
    suspend fun getHomeFeed(): List<RecipePost>
}
