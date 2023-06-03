package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddNewCardActivity : AppCompatActivity() {

    private lateinit var btnConfirmAddNewCard: Button
    private lateinit var btnCancelAddNewCard: Button
    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var cardImage: ImageView

    private lateinit var deckName: String

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_card)

        btnConfirmAddNewCard = findViewById(R.id.btn_AddNewCardConfirm)
        btnCancelAddNewCard = findViewById(R.id.btn_AddNewCardCancel)
        cardFrontText = findViewById(R.id.et_inspectCardFrontText)
        cardBackText = findViewById(R.id.et_inspectCardBackText)
        cardImage = findViewById(R.id.iv_inspectCardImage)

        deckName = intent.getStringExtra("DECK_NAME").toString()

        btnConfirmAddNewCard.setOnClickListener {
            val frontText = cardFrontText.text.toString().trim()
            val backText = cardBackText.text.toString().trim()
            if (frontText.isEmpty() || backText.isEmpty()) {
                Toast.makeText(this, "Campos não podem estar vazios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var imageIdentifier = ""
            if (cardImage.drawable != null) {
                //carregar image para o firebase storage
            }

            val cardHaspMap = hashMapOf(
                "frente" to frontText,
                "verso" to backText,
                "imagem" to imageIdentifier,
                "estudada" to false
            )

            db = DatabaseManager.getDatabase()

            db.collection("cartas")
                .add(cardHaspMap)
                .addOnSuccessListener { documentSnapshot ->
                    Log.d("DATABASE", "Nova carta adicionada com sucesso")
                    //addicionar carta ao baralho correto
                    val cardReference = documentSnapshot.id
                    db.collection("baralhos")
                        .document(deckName)
                        .update("cartas",  FieldValue.arrayUnion(db.document("cartas/$cardReference")))
                        .addOnSuccessListener {
                            Log.d("DATABASE", "Nova carta adicionada ao baralho com sucesso")
                        }
                        .addOnFailureListener {
                            Log.w("DATABASE", "Erro a tentar adicionar nova carta a baralho: $it")
                        }
                }
                .addOnFailureListener {
                    Log.w("DATABASE", "Erro a tentar adicionar carta: $it")
                }
        }
    }
}