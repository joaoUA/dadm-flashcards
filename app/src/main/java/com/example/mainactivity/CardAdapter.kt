package com.example.mainactivity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.NonDisposableHandle.parent

class CardAdapter(private var cards: List<Card>):
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    fun setCards(cards: List<Card>) {
        this.cards = cards
        notifyDataSetChanged()
    }

    class CardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemCardName: TextView = itemView.findViewById(R.id.tv_ItemCardName)
        val itemCardEditBtn: Button = itemView.findViewById(R.id.btn_EditItemCard)
        val itemCardRemoveBtn: Button = itemView.findViewById(R.id.btn_RemoveItemCard)
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
            holder.itemView.context.startActivity(intent)
        }

        holder.itemCardRemoveBtn.setOnClickListener {
            //remover carta
        }
    }
}
