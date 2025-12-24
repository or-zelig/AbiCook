package il.co.or.abicook.data.repository

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRecipeDataRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
)
