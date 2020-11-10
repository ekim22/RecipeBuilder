package com.sdp.recipebuilder.model


data class Step(var id: String, var step: String) {
    constructor(): this(
            "", ""
    )
}
