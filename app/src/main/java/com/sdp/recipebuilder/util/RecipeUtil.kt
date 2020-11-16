package com.sdp.recipebuilder.util

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Recipe
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


/**
 * Utilities for Recipes.
 */
object RecipeUtil : VolleyCallback {
    private const val TAG = "RecipeUtil"
    private const val url = "https://www.themealdb.com/api/json/v1/1/random.php"

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
     * Create a standard Recipe POJO
     */
    fun getTemplateRecipe(): Recipe {
        val recipe = Recipe()

        recipe.userId = FirebaseAuth.getInstance().currentUser!!.uid
        recipe.title = "New recipe"
        recipe.steps = hashMapOf()
        recipe.ingredients = arrayListOf()
        recipe.description = "New recipe description"

        return recipe
    }

    /**
     * Create a random Recipe POJO using mealDb
     */
    fun getRandomMealDb(context: Context, recipes: CollectionReference) {
        val recipe = Recipe()

        val queue = Volley.newRequestQueue(context)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    onSuccess(response, recipe, recipes)
                },
                { error ->
                    Log.e(TAG, "getRandom: ", error)
                }
        )
        // Queue request and get response
        queue.add(jsonObjectRequest)
    }

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

    override fun onSuccess(result: JSONObject, recipe: Recipe, recipes: CollectionReference) {
        val meals = result.get("meals") as JSONArray
        val mealsArray = meals.getJSONObject(0)
        recipe.userId = FirebaseAuth.getInstance().currentUser!!.uid
        recipe.title = mealsArray.getString("strMeal")
        recipe.description = mealsArray.getString("strCategory")
        recipe.pic = mealsArray.getString("strMealThumb")
        val instructions = mealsArray.getString("strInstructions").split("(\\r\\n)+".toRegex())
        for (x in 0..instructions.size.minus(1)) {
            recipe.steps[x.toString()] = instructions[x]
        }
        Log.d(TAG, "onSuccess: " + recipe.steps)
        for (x in 1..20) {
            if (mealsArray.getString("strIngredient$x") != ""
                    && mealsArray.getString("strIngredient$x") != "null") {
                recipe.ingredients.add(mealsArray.getString("strIngredient$x"))
            } else {
                break
            }
        }
        Log.d(TAG, "onSuccess: ingreds: " + recipe.ingredients)
        recipes.add(recipe)
    }

}

interface VolleyCallback {
    fun onSuccess(result: JSONObject, recipe: Recipe, recipes: CollectionReference)
}