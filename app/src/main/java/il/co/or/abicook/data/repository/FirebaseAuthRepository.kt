package il.co.or.abicook.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import il.co.or.abicook.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        return try {
            // 1. יצירת המשתמש ב-Auth
            auth.createUserWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user ID"))

            // 2. שמירת username ב-Firestore - לא חוסמים את הזרימה
            val userData = mapOf("username" to username)

            // שומרים ברקע, בלי await - גם אם זה ייכשל, זה לא יתקע את ההרשמה
            firestore.collection("users").document(userId).set(userData)
                .addOnFailureListener {
                    // אפשר לתת לוג בעתיד אם תרצה, אבל לא נוגעים ב-UI פה
                }

            // 3. מבחינת ה-ViewModel - הכול הצליח
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun logout() {
        auth.signOut()
    }
}
