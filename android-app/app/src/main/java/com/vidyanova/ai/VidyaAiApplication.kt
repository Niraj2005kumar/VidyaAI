package com.vidyanova.ai

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.vidyanova.ai.ai.inference.LocalModelEngine
import com.vidyanova.ai.ai.model.ModelManager
import com.vidyanova.ai.data.local.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VidyaAiApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _setupProgress = MutableStateFlow(0)
    val setupProgress: StateFlow<Int> = _setupProgress.asStateFlow()

    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete.asStateFlow()

    private val _setupError = MutableStateFlow<String?>(null)
    val setupError: StateFlow<String?> = _setupError.asStateFlow()

    var is64BitSupported: Boolean = true
        private set

    var totalRamMb: Long = 0
        private set

    var isLowRamDevice: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        is64BitSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Process.is64Bit()
        } else {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
        }

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        isLowRamDevice = totalRamMb < 2500 || memoryInfo.lowMemory

        Log.i(TAG, "VidyaAiApplication initialized: 64Bit=$is64BitSupported, TotalRAM=${totalRamMb}MB, LowRAM=$isLowRamDevice")

        if (!is64BitSupported) {
            _setupError.value = "Is phone par ye app supported nahi hai (64-bit OS required)."
            return
        }

        if (totalRamMb < 2500) {
            _setupError.value = "Is phone me RAM kam hai (${totalRamMb}MB). App chalane ke liye kam se kam 2.5 GB RAM chahiye."
            Log.w(TAG, "Skipping model preload due to low RAM: ${totalRamMb}MB")
            return
        }

        // Schedule WorkManager Sync
        try {
            SyncWorker.schedulePeriodicSync(applicationContext)
            SyncWorker.scheduleOneTimeSync(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling WorkManager sync: ${e.message}")
        }

        applicationScope.launch {
            try {
                Log.i(TAG, "Starting first-run asset model verification/copy...")
                val modelManager = ModelManager(applicationContext)
                modelManager.ensureModelFromAssets { progress ->
                    _setupProgress.value = progress
                }
                _setupProgress.value = 100
                _isSetupComplete.value = true

                Log.i(TAG, "Preloading local GGUF model in background...")
                localModelEngine.loadModel()
            } catch (e: Exception) {
                Log.e(TAG, "Model setup/preload failed: ${e.message}", e)
                _setupError.value = e.message ?: "Model setup failed"
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
