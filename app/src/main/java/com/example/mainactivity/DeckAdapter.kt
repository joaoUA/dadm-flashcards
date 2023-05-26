package com.example.mainactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeckAdapter (private var decks: List<Deck>) :
    RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    fun setDecks(decks: List<Deck>) {
        this.decks = decks
        notifyDataSetChanged()
    }

    class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemDeckName: TextView = itemView.findViewById(R.id.tvItemDeckName)
        val itemDeckBtnsLayout: LinearLayout = itemView.findViewById(R.id.llItemDeckActions)
        val itemDeckEditBtn: Button = itemView.findViewById(R.id.btnEditItemDeck)
        val itemDeckStudyBtn: Button = itemView.findViewById(R.id.btnStudyItemDeck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck, parent, false)
        return DeckViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val deck = decks[position]

        holder.itemDeckName.text = deck.name

        holder.itemDeckName.setOnClickListener {
            if (holder.itemDeckBtnsLayout.visibility == View.GONE)
                holder.itemDeckBtnsLayout.visibility = View.VISIBLE
            else
                holder.itemDeckBtnsLayout.visibility = View.GONE
        }

        holder.itemDeckEditBtn.setOnClickListener {
            //on click 'edit deck'
        }

        holder.itemDeckStudyBtn.setOnClickListener {
            //on click 'study deck'
        }
    }

    override fun getItemCount(): Int {
        return decks.size
    }
}