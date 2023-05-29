package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InspectCardActivity : AppCompatActivity() {

    private lateinit var card: Card
    private lateinit var db: FirebaseFirestore

    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var cardFrontText: EditText
    private lateinit var cardBackText: EditText
    private lateinit var inspectCardLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspect_card)

        cancelBtn = findViewById(R.id.btn_InspectCardCancel)
        confirmBtn = findViewById(R.id.btn_InspectConfirm)
        cardFrontText = findViewById(R.id.et_inspectCardFrontText)
        cardBackText = findViewById(R.id.et_inspectCardBackText)
        inspectCardLayout = findViewById(R.id.ll_inspectCard)

        inspectCardLayout.visibility = View.GONE

        val cardID = intent.getStringExtra("CARD_ID")
        if (cardID == null || cardID.isEmpty()) {
            finish()
        }

        db = DatabaseManager.getDatabase()
        try {
            db.collection("cartas")
                .document(cardID!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if(!documentSnapshot.exists())
                        throw Exception("Document does not exist: $documentSnapshot")
                    val data = documentSnapshot.data ?: throw Exception("Couldn't load data from document")
                    val cardDataID = documentSnapshot.id
                    val cardFront = data["frente"] as String
                    val cardBack = data["verso"] as String
                    val cardStudied = data["estudada"] as Boolean
                    val cardImage = data["imagem"] as String
                    card = Card(cardDataID, cardFront, cardBack, cardStudied, cardImage)

                    runOnUiThread {
                        cardFrontText.setText(card.frente)
                        cardBackText.setText(card.verso)
                        inspectCardLayout.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    throw Exception("Couldn't get card document")
                }
        } catch (e: Exception) {
            Log.d("Card", "$e")
        }

        cancelBtn.setOnClickListener {
            finish()
        }
        confirmBtn.setOnClickListener {
            if(cardID == null)
                return@setOnClickListener

            card.frente = cardFrontText.text.toString().trim()
            card.verso = cardBackText.text.toString().trim()

            if(card.frente.isEmpty() || card.verso.isEmpty()) {
                Toast.makeText(this, "Can't have empty fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newCardData = hashMapOf(
                "frente" to card.frente,
                "verso" to card.verso,
                "imagem" to card.imagem,
                "estudada" to card.estudada
            )
            try {
                db.collection("cartas").document(cardID)
                    .set(newCardData)
                    .addOnSuccessListener {
                        Log.d("Card", "DocumentSnapshot successfully written!")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Card", "Error writing document", e)
                    }
            } catch (e: Exception) {
                Log.d("Card", "$e")
            }
        }


    }
}