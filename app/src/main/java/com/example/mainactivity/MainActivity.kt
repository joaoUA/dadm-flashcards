package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Firebase.firestore

        db.collection("baralhos")
            .get()
            .addOnSuccessListener { result ->
                for(document in result) {
                    println(document.id)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents.")
                println(exception.toString())
            }

    }
}