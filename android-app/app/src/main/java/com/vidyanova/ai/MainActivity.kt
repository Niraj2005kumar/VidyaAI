package com.vidyanova.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vidyanova.ai.data.network.AuthTokenStore
import com.vidyanova.ai.navigation.VidyaNovaNavHost
import com.vidyanova.ai.ui.theme.VidyaNovaTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthTokenStore.initialize(applicationContext)
        enableEdgeToEdge()

        val app = application as? VidyaAiApplication

        setContent {
            VidyaNovaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val setupError by (app?.setupError ?: MutableStateFlow(null)).collectAsState()

                    if (setupError != null) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = { Text(text = "App Compatibility Warning") },
                            text = { Text(text = setupError ?: "Unsupported device hardware") },
                            confirmButton = {
                                TextButton(onClick = { finish() }) {
                                    Text("OK")
                                }
                            }
                        )
                    } else {
                        VidyaNovaNavHost()
                    }
                }
            }
        }
    }
}
