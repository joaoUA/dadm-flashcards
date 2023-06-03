package com.example.mainactivity

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.*

object DatabaseManager {

    private val databaseInstance: FirebaseFirestore by lazy { Firebase.firestore }

    fun getDatabase(): FirebaseFirestore {
        return databaseInstance
    }

    suspend fun getCardsFromDeckID(deckName: String): MutableList<Card> {
        val cards: MutableList<Card> = mutableListOf()
        try {
            val deckDocumentSnapshot = getDatabase().collection("baralhos")
                .document(deckName)
                .get()
                .await()

            if (!deckDocumentSnapshot.exists())
                throw Exception("Document doesn't exist: $deckName")

            val data = deckDocumentSnapshot.data ?: throw Exception("Couldn't load data from document: $deckName")
            val cardReferences = data["cartas"] as List<DocumentReference>
                ?: throw Exception("Couldn't load cards from deck: $deckName")

            cardReferences.forEach { cardReference ->
                val cardDocument = cardReference.get().await()

                if(!cardDocument.exists())
                    throw Exception("Card doesn't exist: $cardReference")

                val cardData = cardDocument.data
                    ?: throw Exception("Couldn't load card data from: $cardReference")
                val card = Card(
                    id = cardDocument.id,
                    frente = cardData["frente"] as String,
                    verso = cardData["verso"] as String,
                    imagem = cardData["imagem"] as String,
                    estudada = cardData["estudada"] as Boolean
                )
                cards.add(card)
            }
        } catch (e: Exception) {
            Log.w("Firebase", "Error: $e")
        }
        return cards
    }
    
    fun generateUniqueName(): String {
        val timestamp = System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()
        return "${timestamp}_$uniqueId"
    }

}