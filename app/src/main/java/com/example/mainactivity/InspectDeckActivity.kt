package com.example.mainactivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InspectDeckActivity : AppCompatActivity() {

    private lateinit var addCardBtn: Button

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

        addCardBtn = findViewById(R.id.btn_AddNewCard)

        rvCards = findViewById(R.id.rv_CardList)
        rvCards.layoutManager = LinearLayoutManager(this)

        cardAdapter = CardAdapter(emptyList())
        cardAdapter.setDeck(deckName)
        rvCards.adapter = cardAdapter

        addCardBtn.setOnClickListener {
            val intent = Intent(this, AddNewCardActivity::class.java)
            intent.putExtra("DECK_NAME", deckName)
            this.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            cardAdapter.updateCardList()
        }
    }
}