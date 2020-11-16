package com.sdp.recipebuilder.model


data class Recipe(var userId: String, var title: String, var steps: HashMap<String, String>,
                  var ingredients: ArrayList<String>, var description: String?, var pic: String?) {
    constructor() : this(
            "",
            "",
            hashMapOf<String, String>(),
            arrayListOf(),
            "",
            ""
    )
}
