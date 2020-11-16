package com.sdp.recipebuilder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Recipe

/**
 * RecyclerView adapter for a list of Recipes.
 */
open class RecipeAdapter(query: Query, private val mListener: OnRecipeSelectedListener) :
        FirestoreAdapter<RecipeAdapter.RecipeViewHolder?>(query) {
    interface OnRecipeSelectedListener {
        fun onRecipeSelected(recipe: DocumentSnapshot?)
        fun handleDeleteRecipe(recipe: DocumentSnapshot?)
        fun handleEditRecipe(recipe: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RecipeViewHolder(inflater.inflate(R.layout.item_recipe, parent, false))
    }

    override fun onBindViewHolder(holderRecipe: RecipeViewHolder, position: Int) {
        holderRecipe.bind(getSnapshot(position), mListener)
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.recipe_item_image)
        var nameView: TextView = itemView.findViewById(R.id.recipe_item_name)
        var descView: TextView = itemView.findViewById(R.id.recipe_item_description)

        fun bind(snapshot: DocumentSnapshot,
                 listener: OnRecipeSelectedListener?) {
            snapshot.toObject(Recipe::class.java)?.let { recipe ->
                val resources = itemView.resources

                if (recipe.pic != "") {
                    Glide.with(imageView.context)
                            .load(recipe.pic)
                            .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.default_recipe_icon)
                }
                nameView.text = recipe.title
                descView.text = recipe.description

                // Click listener
                itemView.setOnClickListener { listener?.onRecipeSelected(snapshot) }
            }
        }

        fun deleteRecipe() {
            mListener.handleDeleteRecipe(getSnapshot(adapterPosition))
        }


    }

}
