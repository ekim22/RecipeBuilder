package com.sdp.recipebuilder.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Ingredient

class IngredientAdapter(private val context: Context, private val ingredientList: List<Ingredient>)
    : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_ingredient,
                parent, false)
        return IngredientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val currentIngredient = ingredientList[position]
        Log.d("StepAdapter", "onBindViewHolder: currentStep " + currentIngredient.toString())
        holder.ingredientView.text = currentIngredient.ingredient
    }

    override fun getItemCount() = ingredientList.size

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientView: TextView = itemView.findViewById(R.id.ingredient_list_item)
    }
}