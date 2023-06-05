package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class StudyDeckActivity : AppCompatActivity() {

    private lateinit var btnFlip: ImageButton
    private lateinit var btnWrong: ImageButton
    private lateinit var btnCorrect: ImageButton

    private lateinit var btnFinish: ImageButton

    private lateinit var flashcardText : TextView
    private lateinit var flashcardImage: ImageView

    private var deckName: String? = null
    private var cardIndex: Int = 0

    private var correctCounter: Int = 0
    private var wrongCounter: Int = 0

    private lateinit var cards: MutableList<Card>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_deck)

        btnFlip = findViewById(R.id.btn_flashcardFlipBtn)
        btnCorrect = findViewById(R.id.btn_flashcardCorrectBtn)
        btnWrong = findViewById(R.id.btn_flashcardWrongBtn)
        btnFinish = findViewById(R.id.btn_flashcardEndBtn)
        flashcardText = findViewById(R.id.tv_flashcardText)
        flashcardImage = findViewById(R.id.iv_flashcardImage)

        deckName = intent.getStringExtra("DECK_NAME")

        if (deckName == null || deckName!!.isEmpty()) {
            finish()
        }

        btnFinish.visibility = View.GONE
        btnFlip.visibility = View.GONE
        btnWrong.visibility = View.GONE
        btnCorrect.visibility = View.GONE
        flashcardText.visibility = View.GONE
        flashcardImage.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            cards = DatabaseManager.getCards(deckName!!)

            if (cards.size == 0) {
                finish()
            }

            withContext(Dispatchers.Main) {
                flashcardText.visibility = View.VISIBLE
                flashcardImage.visibility = View.VISIBLE

                updateFlashcardImage(cards[cardIndex].imagemLink)
                updateFlashcardText(cards[cardIndex].frente)
                toggleAnswerButtons(false)
            }
        }

        btnFlip.setOnClickListener {
            updateFlashcardText(cards[cardIndex].verso)
            toggleAnswerButtons(true)
        }
        btnCorrect.setOnClickListener {
            correctCounter++
            cardIndex++
            if (cardIndex == cards.size) {
                showResult()
                return@setOnClickListener
            }

            updateFlashcardImage(cards[cardIndex].imagemLink)
            updateFlashcardText(cards[cardIndex].frente)
            toggleAnswerButtons(false)
        }
        btnWrong.setOnClickListener {
            wrongCounter++
            cardIndex++
            if (cardIndex == cards.size) {
                showResult()
                return@setOnClickListener
            }

            updateFlashcardImage(cards[cardIndex].imagemLink)
            updateFlashcardText(cards[cardIndex].frente)
            toggleAnswerButtons(false)
        }

        btnFinish.setOnClickListener {
            finish()
        }
    }

    private fun showResult(){
        btnFinish.visibility = View.VISIBLE
        btnFlip.visibility = View.GONE
        btnWrong.visibility = View.GONE
        btnCorrect.visibility = View.GONE
        updateFlashcardImage("")
        updateFlashcardText("Correctas: $correctCounter \nErradas:$wrongCounter\n${Math.ceil((correctCounter * 100 / cards.size).toDouble())}%")

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

    private fun updateFlashcardImage(imageLink: String) {
        if (imageLink.isNotEmpty()) {
            flashcardImage.visibility = View.VISIBLE
            Glide.with(this.applicationContext)
                .load(imageLink)
                .into(flashcardImage)
        }
        else {
            flashcardImage.setImageDrawable(null)
            flashcardImage.visibility = View.GONE
        }
    }
}