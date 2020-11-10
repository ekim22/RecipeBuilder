package com.sdp.recipebuilder.model

import com.google.firebase.firestore.DocumentId


data class Recipe(@DocumentId var uid: String, var title: String, var steps: HashMap<String, String>,
                  var ingredients: ArrayList<String>, var description: String?) {
    constructor() : this(
            "",
            "",
            hashMapOf<String, String>(),
            arrayListOf(),
            ""
    )
}
