package com.sdp.recipebuilder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Recipe

/**
 * RecyclerView adapter for a list of Recipes.
 */
class RecipeAdapter(query: Query, private val mListener: OnRecipeSelectedListener) :
        FirestoreAdapter<RecipeAdapter.ViewHolder?>(query) {
    interface OnRecipeSelectedListener {
        fun onRecipeSelected(recipe: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_recipe, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(snapshot: DocumentSnapshot,
                 listener: OnRecipeSelectedListener?) {
            val recipe: Recipe? = snapshot.toObject(Recipe::class.java)
            val resources = itemView.resources


            // Click listener
            itemView.setOnClickListener { listener?.onRecipeSelected(snapshot) }
        }

    }

}