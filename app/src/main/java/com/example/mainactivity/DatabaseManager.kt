package com.example.mainactivity

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.HashMap

object DatabaseManager {

    private const val DECK_COLLECTION_NAME = "baralhos"
    private const val CARDS_FIELD_NAME = "cartas"

    private val databaseInstance: FirebaseFirestore by lazy { Firebase.firestore }

    fun getDatabase(): FirebaseFirestore {
        return databaseInstance
    }

    suspend fun getCards(deckName: String): MutableList<Card> {
        val cards = mutableListOf<Card>()

        val deckDocument = databaseInstance
            .collection(DECK_COLLECTION_NAME)
            .document(deckName)
            .get()
            .await()

        if (!deckDocument.exists())
            throw Exception("Baralho não existe: $deckName")
        val deckData = deckDocument.data ?: throw Exception("Erro ao ler dados do baralho: $deckName")
        val deckCards = deckData[CARDS_FIELD_NAME] as? List<HashMap<String, Any>>

        deckCards?.forEachIndexed { index, cardHashmap ->
            val card = Card(
                id = index,
                frente = cardHashmap["frente"] as String,
                verso = cardHashmap["verso"] as String,
                estudada = cardHashmap["estudada"] as Boolean,
                imagemLink = cardHashmap["imagemLink"] as String,
                imagemRef = cardHashmap["imagemRef"] as String
            )
            cards.add(card)
            Log.d("DATABASE", "Carta lida! id=${card.id}, frente:${card.frente}")
        }

        return cards
    }

    suspend fun getCard(cardId: Int, deckId: String): Card {
        val deckDocument = databaseInstance.collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .get()
            .await()

        if (!deckDocument.exists())
            throw Exception("Baralho não existe: $deckId")
        val deckData = deckDocument.data ?: throw Exception("Erro ao ler dados do baralho: $deckId")
        val deckCards = deckData[CARDS_FIELD_NAME] as List<HashMap<String, Any>>
        val card = deckCards[cardId]
        return Card(
            id = cardId,
            frente = card["frente"] as String,
            verso = card["verso"] as String,
            estudada = card["estudada"] as Boolean,
            imagemLink = card["imagemLink"] as String,
            imagemRef = card["imagemRef"] as String
        )
    }

    suspend fun getDecks(): List<Deck> {
        val decks = mutableListOf<Deck>()
        val decksDocuments = databaseInstance.collection(DECK_COLLECTION_NAME)
            .get()
            .await()

        decksDocuments.forEach { deck ->
            decks.add(Deck(deck.id))
        }

        return decks
    }

    suspend fun addNewDeck(deckName: String) {
        databaseInstance
            .collection(DECK_COLLECTION_NAME)
            .document(deckName)
            .set(hashMapOf(
                CARDS_FIELD_NAME to arrayListOf<HashMap<String, Any>>()
            ))
            .await()
    }

    suspend fun addNewCard(card: Card, deckId: String) {
        databaseInstance
            .collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .update(CARDS_FIELD_NAME, FieldValue.arrayUnion(
                hashMapOf(
                    "frente" to card.frente,
                    "verso" to card.verso,
                    "estudada" to card.estudada,
                    "imagemLink" to card.imagemLink,
                    "imagemRef" to card.imagemRef
                )
            ))
            .await()
    }

    suspend fun removeCard(cardId: Int, deckId: String) {
        val deckSnapshot = databaseInstance.collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .get()
            .await()

        if (!deckSnapshot.exists())
            throw Exception("Baralho não existe: $deckId")

        val cards = deckSnapshot.get(CARDS_FIELD_NAME) as? MutableList<*>
            ?: throw Exception("Erro ao tentar obter cartas do baralho: $deckId")

        if(cardId < 0 || cardId >= cards.size)
            throw Exception("Index fora do intervalo: $cardId")

        cards.removeAt(cardId)

        databaseInstance.collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .update(CARDS_FIELD_NAME, cards)
            .await()
    }

    suspend fun updateCard(card: Card, deckId: String) {
        val deckDocument = databaseInstance.collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .get()
            .await()

        if (!deckDocument.exists())
            throw Exception("Baralho $deckId não existe!")

        val cards = deckDocument.get(CARDS_FIELD_NAME) as? MutableList<HashMap<String, Any>>
            ?: throw Exception("Erro ao tentar obter cartas do baralho: $deckId")

        cards[card.id] = hashMapOf(
            "frente" to card.frente,
            "verso" to card.verso,
            "estudada" to card.estudada,
            "imagemLink" to card.imagemLink,
            "imagemRef" to card.imagemRef
        )

        databaseInstance.collection(DECK_COLLECTION_NAME)
            .document(deckId)
            .update(CARDS_FIELD_NAME, cards)

        Log.d("DATABASE", "Carta atualizada com sucesso")
    }

    suspend fun deleteImageFromStorage(imageRef: String) {
        FirebaseStorage.getInstance().reference.child(imageRef).delete().await()
    }

    suspend fun uploadImageToStorage(imageUri: Uri): Pair<String, String> {
        val imageId = generateUniqueName()
        val imageRef = FirebaseStorage.getInstance().reference.child("imagens/$imageId")
        imageRef.putFile(imageUri).await()
        val imageLink = imageRef.downloadUrl.await()

        return Pair("imagens/$imageId", imageLink.toString())
    }

    private fun generateUniqueName(): String {
        val timestamp = System.currentTimeMillis()
        val uniqueId = UUID.randomUUID().toString()
        return "${timestamp}_$uniqueId"
    }


}