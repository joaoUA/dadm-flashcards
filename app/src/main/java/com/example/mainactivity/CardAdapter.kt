package com.example.mainactivity

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class CardAdapter(private var cards: List<Card>):
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private lateinit var deckName: String

    fun setDeck(deckName: String) {
        this.deckName = deckName
    }

    private fun setCards(cards: List<Card>) {
        this.cards = cards
        notifyDataSetChanged()
    }

    class CardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemCardName: TextView = itemView.findViewById(R.id.tv_ItemCardName)
        val itemCardEditBtn: ImageButton = itemView.findViewById(R.id.btn_EditItemCard)
        val itemCardRemoveBtn: ImageButton = itemView.findViewById(R.id.btn_RemoveItemCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.itemCardName.text = card.frente

        holder.itemCardEditBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, InspectCardActivity::class.java)
            intent.putExtra("CARD_ID", card.id)
            intent.putExtra("DECK_NAME", deckName)
            holder.itemView.context.startActivity(intent)
        }

        holder.itemCardRemoveBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (card.imagemLink.isNotEmpty())
                        DatabaseManager.deleteImageFromStorage(card.imagemRef)
                    DatabaseManager.removeCard(card.id, deckName)
                    updateCardList()
                } catch (e: Exception) {
                    Log.w("DATABASE", "Erro a tentar remover carta ${card.id} de $deckName: $e")
                }
            }
        }
    }

    suspend fun updateCardList() {
        try {
            val cards = DatabaseManager.getCards(deckName)
            withContext(Dispatchers.Main) {
                setCards(cards)
            }
        } catch (e: Exception) {
            Log.w("DATABASE", "Erro a tentar ler conteúdo do baralho $deckName: $e")
        }
    }
}
