package com.sdp.recipebuilder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdp.recipebuilder.adapter.IngredientAdapter
import com.sdp.recipebuilder.model.Ingredient

class IngredientFragment(private val ingredients: ArrayList<String>) : Fragment() {
    private var TAG = IngredientFragment::class.qualifiedName
    private var ingredientView: View? = null
    private var ingredientRecyclerView: RecyclerView? = null
    private var ingredientList = ArrayList<Ingredient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (ingredient in ingredients) {
            ingredientList.add(Ingredient(ingredient))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        ingredientView = inflater.inflate(R.layout.fragment_ingredient, container, false)
        ingredientRecyclerView = ingredientView!!.findViewById(R.id.ingredient_recycler) as RecyclerView
        val adapter = this.context?.let { IngredientAdapter(it, ingredientList) }
        ingredientRecyclerView!!.layoutManager = LinearLayoutManager(this.context)
        ingredientRecyclerView!!.adapter = adapter

        return ingredientView
    }

}