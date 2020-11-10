package com.sdp.recipebuilder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun handleLoginRegister(view: View) {
        val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(listOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.PhoneBuilder().build()))
                .setTosAndPrivacyPolicyUrls("https://example.com", "https://example.com")
                .setLogo(R.drawable.recipe)
                .setIsSmartLockEnabled(false)
                .build()
        startActivityForResult(intent, AUTHUI_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHUI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Successful sign-in
                var user = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, user?.email.toString())
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            } else {
                // Failed to sign-in
                var response = IdpResponse.fromResultIntent(data)
                if (response == null) {
                    Log.d(TAG, "onActivityResult: user has cancelled the sign in request")
                } else {
                    Log.e(TAG, "onActivityResult: ", response.error)
                }
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val AUTHUI_REQUEST_CODE = 10001
    }
}