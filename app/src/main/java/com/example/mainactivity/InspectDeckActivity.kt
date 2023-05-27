package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference

class InspectDeckActivity : AppCompatActivity() {

    private lateinit var rvCards: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private lateinit var deckName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_deck)

        deckName = intent.getStringExtra("DECK_NAME").toString()

        if (deckName.isBlank())
            finish()

        rvCards = findViewById(R.id.rv_CardList)
        rvCards.layoutManager = LinearLayoutManager(this)

        cardAdapter = CardAdapter(emptyList())
        rvCards.adapter = cardAdapter

        val db = DatabaseManager.getDatabase()

        val cards: MutableList<Card> = mutableListOf()
        var totalCards = 0

        db.collection("baralhos")
            .document(deckName)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if(!documentSnapshot.exists()) {
                    println("Document does not exist: $documentSnapshot")
                    return@addOnSuccessListener
                }
                val data = documentSnapshot.data
                val cardReferences = data?.get("cartas") as? List<DocumentReference>
                cardReferences?.forEach { reference ->
                    reference.get()
                        .addOnSuccessListener { referencedDocument ->
                            if(referencedDocument.exists()) {
                                val cardData = referencedDocument.data
                                totalCards++
                                if(cardData != null) {
                                    val card = Card(cardData["frente"]!! as String, cardData["verso"]!! as String )
                                    cards.add(card)
                                    if(cards.size == totalCards) {
                                        cardAdapter.setCards(cards)
                                    }
                                }
                            } else {
                                println("Referenced document does not exist: ${reference.path}")
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Error getting referenced document: $exception")
                        }
                    }
                }
        }
}