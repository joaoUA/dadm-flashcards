package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class StudyDeckActivity : AppCompatActivity() {

    private lateinit var btnFlip: Button
    private lateinit var btnWrong: Button
    private lateinit var btnCorrect: Button
    private lateinit var flashcardText : TextView

    private lateinit var deckName: String
    private var cardIndex: Int = 0

    private lateinit var db: FirebaseFirestore
    private lateinit var cards: MutableList<Card>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_deck)

        btnFlip = findViewById(R.id.btn_flashcardFlipBtn)
        btnCorrect = findViewById(R.id.btn_flashcardCorrectBtn)
        btnWrong = findViewById(R.id.btn_flashcardWrongBtn)
        flashcardText = findViewById(R.id.tv_flashcardText)

        deckName = intent.getStringExtra("DECK_NAME").toString()

        db = DatabaseManager.getDatabase()
        cards = mutableListOf()

        btnFlip.visibility = View.GONE
        btnWrong.visibility = View.GONE
        btnCorrect.visibility = View.GONE

        btnFlip.setOnClickListener {
            updateFlashcardText(cards[cardIndex].verso)
            toggleAnswerButtons(true)
        }
        btnCorrect.setOnClickListener {
            cardIndex++
            if(cardIndex == cards.size) {
                finish()
                return@setOnClickListener
            }
            updateFlashcardText(cards[cardIndex].frente)
            toggleAnswerButtons(false)
        }
        btnWrong.setOnClickListener {
            cardIndex++
            if(cardIndex == cards.size) {
                finish()
                return@setOnClickListener
            }
            updateFlashcardText(cards[cardIndex].frente)
            toggleAnswerButtons(false)
        }

        val loadCardData: Job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val deckDocumentSnapshot = db.collection("baralhos")
                    .document(deckName)
                    .get()
                    .await()
                if(!deckDocumentSnapshot.exists()) {
                    println("Document doesn't exist!")
                    return@launch
                }
                val deckData = deckDocumentSnapshot.data
                val cardReferences = deckData?.get("cartas") as? List<DocumentReference>
                val cardDeferreds = cardReferences?.map { reference ->
                    async {
                        val referencedDocument = reference.get().await()
                        if(referencedDocument.exists()) {
                            val cardData = referencedDocument.data
                            if(cardData != null) {
                                val card = Card(
                                    id=referencedDocument.id,
                                    frente=cardData["frente"]!! as String,
                                    verso=cardData["verso"]!! as String,
                                    estudada=cardData["estudada"]!! as Boolean,
                                    imagem=cardData["imagem"]!! as String
                                )
                                card
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }
                } ?: emptyList()
                val retrievedCard = cardDeferreds.awaitAll().filterNotNull()
                cards.addAll(retrievedCard)
            } catch (e: Exception) {
                println("Error Retrieving Cards: $e")
            }
        }
        loadCardData.invokeOnCompletion {
            if(cards.isEmpty()) {
                Toast.makeText(this as StudyDeckActivity, "Failed Loading Cards", Toast.LENGTH_SHORT).show()
                finish()
                return@invokeOnCompletion
            }
            runOnUiThread {
                toggleAnswerButtons(false)
                updateFlashcardText(cards[cardIndex].frente)
            }
        }
    }

    private fun toggleAnswerButtons(show: Boolean) {
        if (show) {
            btnFlip.visibility = View.GONE
            btnWrong.visibility = View.VISIBLE
            btnCorrect.visibility = View.VISIBLE
        } else {
            btnFlip.visibility = View.VISIBLE
            btnWrong.visibility = View.GONE
            btnCorrect.visibility = View.GONE
        }
    }

    private fun updateFlashcardText(text: String) {
        flashcardText.text = text
    }

}