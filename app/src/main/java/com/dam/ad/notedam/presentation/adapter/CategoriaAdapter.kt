package com.dam.ad.notedam.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dam.ad.notedam.R
import com.dam.ad.notedam.presentation.clases.Categoria
import java.text.SimpleDateFormat
import java.util.Locale

class CategoriaAdapter(private val categories: List<Categoria>
) :
    RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {
    private val selectedCategories = mutableSetOf<Categoria>()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.fechaTextView)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        init {
            itemView.setOnClickListener {
                val category = categories[adapterPosition]
                toggleSelection(category)
            }
        }
        fun bind(category: Categoria) {
            nameTextView.text = category.nombre
            dateTextView.text =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(category.fecha)
            checkBox.isChecked = selectedCategories.contains(category)
        }
    }
    fun getSelectedCategories(): Set<Categoria> {
        return selectedCategories
    }
    private fun toggleSelection(category: Categoria) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            selectedCategories.add(category)
        }
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }
    override fun getItemCount(): Int {
        return categories.size
    }
}