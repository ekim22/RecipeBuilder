package com.sdp.recipebuilder

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var profilePic = findViewById<View>(R.id.profilePic)
        var profileName = findViewById<View>(R.id.profileName)
        var profileEmail = findViewById<View>(R.id.profileEmail)
        var profilePhoneNum = findViewById<View>(R.id.profilePhoneNumber)
        var updateProfileBtn = findViewById<View>(R.id.updateProfile)
        var profileProgBar = findViewById<View>(R.id.profileProgressBar)

    }

    fun updateProfile(view: View) {}
}