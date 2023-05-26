package com.example.mainactivity

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object DatabaseManager {

    private val databaseInstance: FirebaseFirestore by lazy { Firebase.firestore }

    fun getDatabase(): FirebaseFirestore {
        return databaseInstance
    }

}