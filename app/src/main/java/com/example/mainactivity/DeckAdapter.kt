package com.example.mainactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeckAdapter (private var decks: List<Deck>) :
    RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setDecks(decks: List<Deck>) {
        this.decks = decks
        notifyDataSetChanged()
    }

    class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemDeckName: TextView = itemView.findViewById(R.id.tv_ItemDeckName)
        val itemDeckBtnsLayout: LinearLayout = itemView.findViewById(R.id.ll_ItemDeckActions)
        val itemDeckEditBtn: ImageButton = itemView.findViewById(R.id.btn_EditItemDeck)
        val itemDeckStudyBtn: ImageButton = itemView.findViewById(R.id.btn_StudyItemDeck)
        val itemDeckDeleteBtn: ImageButton = itemView.findViewById(R.id.btn_DeleteItemDeck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck, parent, false)
        return DeckViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val deck = decks[position]

        holder.itemDeckName.text = deck.name

        //TOGGLE VISIBILIDADE DAS OPÇÕES DE CADA ITEM_BARALHO
        holder.itemDeckName.setOnClickListener {
            if (holder.itemDeckBtnsLayout.visibility == View.GONE)
                holder.itemDeckBtnsLayout.visibility = View.VISIBLE
            else
                holder.itemDeckBtnsLayout.visibility = View.GONE
        }

        //INICIAR ATIVIDADE DE EDITAR BARALHO
        holder.itemDeckEditBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, InspectDeckActivity::class.java)
            Log.d("DATABASE", "NOME DO BARALHO: ${deck.name}")
            intent.putExtra("DECK_NAME", deck.name)
            holder.itemView.context.startActivity(intent)
        }

        //INICIAR ATIVIDADE DE ESTUDAR BARALHO
        holder.itemDeckStudyBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, StudyDeckActivity::class.java)
            intent.putExtra("DECK_NAME", deck.name)
            holder.itemView.context.startActivity(intent)
        }

        //APAGAR BARALHO
        holder.itemDeckDeleteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                //todo: mover para o manager
                DatabaseManager.getDatabase()
                    .collection("baralhos")
                    .document(deck.name)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("DATABASE", "Baralho eliminado com sucesso!")

                        CoroutineScope(Dispatchers.IO).launch {
                            val decks = DatabaseManager.getDecks()
                            withContext(Dispatchers.Main) {
                                setDecks(decks)
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w("DATABASE", "Erro ao eliminar o baralho: $it")
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return decks.size
    }
}