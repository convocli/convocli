package com.convocli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.convocli.ui.screens.CommandBlocksScreen
import com.convocli.ui.theme.ConvoCLITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * ConvoCLI main activity.
 *
 * Entry point for the application. Displays initial "Hello ConvoCLI" screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvoCLITheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CommandBlocksScreen()
                }
            }
        }
    }
}
