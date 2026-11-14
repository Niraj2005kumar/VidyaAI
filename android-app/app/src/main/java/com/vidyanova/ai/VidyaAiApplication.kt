package com.vidyanova.ai

import android.app.Application
import android.util.Log
import com.vidyanova.ai.ai.inference.LocalModelEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VidyaAiApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.i(TAG, "VidyaAiApplication started. Preloading local GGUF model in background...")
        applicationScope.launch {
            try {
                localModelEngine.loadModel()
            } catch (e: Exception) {
                Log.e(TAG, "Error during background model preloading: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "VidyaAiApplication"

        lateinit var instance: VidyaAiApplication
            private set

        val localModelEngine: LocalModelEngine by lazy {
            LocalModelEngine(instance.applicationContext)
        }
    }
}
