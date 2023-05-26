package com.example.mainactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var rvDecks: RecyclerView
    private lateinit var deckAdapter: DeckAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDecks = findViewById(R.id.rv_DeckList)
        rvDecks.layoutManager = LinearLayoutManager(this)

        deckAdapter = DeckAdapter(emptyList())
        rvDecks.adapter = deckAdapter

        val db = Firebase.firestore

        val startingDeck: MutableList<Deck> = mutableListOf()

        db.collection("baralhos")
            .get()
            .addOnSuccessListener { result ->
                for(document in result) {
                    println("Baralho Nome: ${document.id}");

                    startingDeck.add(Deck(document.id))

                    val cartas = document.data["cartas"] as? List<DocumentReference>
                    cartas?.forEach { cartaRef ->
                        cartaRef.get()
                            .addOnSuccessListener { cartaSnapshot ->
                                if(cartaSnapshot.exists()) {
                                    val cartaData = cartaSnapshot.data
                                    val cartaFrente = cartaData?.get("frente")
                                    val cartaVerso = cartaData?.get("verso")
                                } else {
                                    println("Carta não existe: ${cartaRef}")
                                }
                            }
                            .addOnFailureListener { exception ->
                                println("Erro a ler carta: ${exception}")
                            }
                    }
                }

                deckAdapter.setDecks(startingDeck)
            }
            .addOnFailureListener { exception ->
                println("Error getting documents.")
                println(exception.toString())
            }

    }
}