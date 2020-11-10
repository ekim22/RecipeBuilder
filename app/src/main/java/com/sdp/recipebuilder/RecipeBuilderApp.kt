package com.sdp.recipebuilder

import android.app.Application
import android.content.Intent
import android.service.voice.VoiceInteractionService

class RecipeBuilderApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * Find the assistant package name
     */
    private fun getAssistantPackage(): String? {
        val resolveInfoList = packageManager?.queryIntentServices(
                Intent(VoiceInteractionService.SERVICE_INTERFACE), 0
        )
        return resolveInfoList?.firstOrNull()?.serviceInfo?.packageName
    }
}