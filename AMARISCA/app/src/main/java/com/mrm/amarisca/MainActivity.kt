package com.mrm.amarisca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mrm.amarisca.ui.SmartHomeScreen
import com.mrm.amarisca.ui.theme.AMARISCATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Edge-to-Edge
        enableEdgeToEdge()

        // Force status bar icons/text to white
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false    // <-- WHITE TEXT
        insetsController.isAppearanceLightNavigationBars = false // <-- WHITE NAV TEXT

        setContent {
            AMARISCATheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    SmartHomeScreen(modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}
