package com.example.mainactivity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var btnAddDeck: ImageButton
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

        //OBTER LISTA DE BARALHOS
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val decks = DatabaseManager.getDecks()
                withContext(Dispatchers.Main) {
                    deckAdapter.setDecks(decks)
                }
            } catch (e: Exception) {
                Log.w("DATABASE", "Erro a tentar ler baralhos do Firebase: $e")
            }
        }

        //CRIAR BARALHO COM O NOME INTRODUZIDO
        btnAddDeck.setOnClickListener {
            val newDeckName = deckNameText.text.toString().trim()
            if (newDeckName.isEmpty())
                return@setOnClickListener

            deckNameText.setText("")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    DatabaseManager.addNewDeck(newDeckName)

                    val decks = DatabaseManager.getDecks()
                    withContext(Dispatchers.Main) {
                        deckAdapter.setDecks(decks)
                    }
                } catch (e: Exception) {
                    Log.w("DATABASE", "Erro ao tentar adicionar novo baralho: $e")
                    Toast.makeText(this@MainActivity, "Erro a adicionar baralho!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}