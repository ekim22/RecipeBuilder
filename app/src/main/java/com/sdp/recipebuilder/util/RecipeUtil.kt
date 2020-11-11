package com.sdp.recipebuilder.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Recipe
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Utilities for Recipes.
 */
object RecipeUtil {
    private const val TAG = "RecipeUtil"
    private val EXECUTOR = ThreadPoolExecutor(2, 4, 60,
            TimeUnit.SECONDS, LinkedBlockingQueue())
    private val NAME_FIRST_WORDS = arrayOf(
            "Apple",
            "Cherry",
            "Lemon",
            "Pecan",
            "Pumpkin")
    private val NAME_SECOND_WORDS = arrayOf(
            "Pie",
            "Latte",
            "Muffins",
            "Juice",
            "Cider")

    /**
     * Create a random Recipe POJO.
     */
    fun getRandom(context: Context): Recipe {
        val recipe = Recipe()
        val random = Random()

        // Steps
        var steps = context.resources.getStringArray(R.array.sample_recipe_steps)
        steps = steps.copyOfRange(0, steps.size)

        // Ingredients
        var ingredients = context.resources.getStringArray(R.array.sample_recipe_ingredients)
        ingredients = ingredients.copyOfRange(0, ingredients.size)
        recipe.userId = FirebaseAuth.getInstance().currentUser!!.uid
        recipe.title = getRandomName(random)
        recipe.steps = getRandomSteps(steps, random)
        recipe.ingredients = getRandomIngredients(ingredients, random)
        recipe.description = "Made by RecipeUtil"

        return recipe
    }

    private fun getRandomName(random: Random): String {
        return (getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random))
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

    private fun getRandomSteps(array: Array<String>, random: Random): HashMap<String, String> {
        val steps = hashMapOf<String, String>()
        for (x in 0..random.nextInt(array.size)) {
            steps.put(x.toString(), array[random.nextInt(array.size)])
        }
        return steps
    }

    private fun getRandomIngredients(array: Array<String>, random: Random): ArrayList<String> {
        val ingredients = arrayListOf<String>()
        for (x in 0..random.nextInt(array.size)) {
            ingredients.add(array[random.nextInt(array.size)])
        }
        return ingredients
    }
}