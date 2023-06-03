package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddDeck: Button
    private lateinit var deckNameText: EditText

    private lateinit var rvDecks: RecyclerView
    private lateinit var deckAdapter: DeckAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAddDeck = findViewById(R.id.btn_AddNewDeck)
        deckNameText = findViewById(R.id.et_AddNewDeck)

        rvDecks = findViewById(R.id.rv_DeckList)
        rvDecks.layoutManager = LinearLayoutManager(this)

        deckAdapter = DeckAdapter(emptyList())
        rvDecks.adapter = deckAdapter

        val db = DatabaseManager.getDatabase()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val decks = DatabaseManager.getDecks().await()
                withContext(Dispatchers.Main) {
                    deckAdapter.setDecks(decks)
                }
            } catch (e: Exception) {
                Log.w("DATABASE", "Erro a tentar ler baralhos do Firebase: $e")
            }
        }

        btnAddDeck.setOnClickListener {
            val newDeckName = deckNameText.text.toString().trim()
            if (newDeckName.isEmpty())
                return@setOnClickListener

            deckNameText.setText("")

            db.collection("baralhos")
                .document(newDeckName)
                .set(hashMapOf(
                    "cartas" to arrayListOf<DocumentReference>()
                ))
                .addOnSuccessListener {
                    Log.d("DATABASE", "Baralho $newDeckName: adicionado com sucesso")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val updatedDecks = DatabaseManager.getDecks().await()
                            withContext(Dispatchers.Main) {
                                deckAdapter.setDecks(updatedDecks)
                            }

                        } catch (e: Exception) {
                            Log.w("DATABASE", "Erro a tentar ler baralhos do Firebase: $e")
                        }

                    }
                }
                .addOnFailureListener {
                    Log.d("DATABASE", "Baralho $newDeckName: erro ao adicionar: $it")
                }
        }
    }
}