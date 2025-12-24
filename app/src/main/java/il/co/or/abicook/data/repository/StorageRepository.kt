package il.co.or.abicook.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

data class UploadResult(
    val downloadUrl: String,
    val path: String
)

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadRecipeCover(recipeId: String, uri: Uri): UploadResult {
        val path = "recipes/$recipeId/cover.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        return UploadResult(downloadUrl = url, path = path)
    }

    suspend fun uploadRecipeStepImage(recipeId: String, stepIndex: Int, uri: Uri): UploadResult {
        val path = "recipes/$recipeId/steps/$stepIndex.jpg"
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        return UploadResult(downloadUrl = url, path = path)
    }
}
