package com.sdp.recipebuilder

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class RecipeActivity : AppCompatActivity() {

    var isRotate = false
    var btnAnimation = ButtonAnimation()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        val addBtn = findViewById<View>(R.id.fabAdd)
        val micBtn = findViewById<View>(R.id.fabMic)
        val writeBtn = findViewById<View>(R.id.fabWrite)

        btnAnimation.init(micBtn)
        btnAnimation.init(writeBtn)

        addBtn.setOnClickListener { v ->
            isRotate = btnAnimation.rotateFab(v, !isRotate)
            if (isRotate) {
                btnAnimation.showIn(micBtn)
                btnAnimation.showIn(writeBtn)
            } else {
                btnAnimation.showOut(micBtn)
                btnAnimation.showOut(writeBtn)
            }
        }
    }
}