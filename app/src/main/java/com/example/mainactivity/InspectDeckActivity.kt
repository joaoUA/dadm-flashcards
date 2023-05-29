package com.example.mainactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InspectDeckActivity : AppCompatActivity() {

    private lateinit var rvCards: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private lateinit var deckName: String
    private lateinit var cards: MutableList<Card>

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
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            cards = withContext(Dispatchers.Default) {
                DatabaseManager.getCardsFromDeckID(deckName)
            }
            runOnUiThread {
                cardAdapter.setCards(cards)
            }
        }
    }
}