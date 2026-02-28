package com.mr.mukto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.mr.mukto.ui.NoInternetDialog
import com.mr.mukto.ui.SmartHomeScreen
import com.mr.mukto.ui.theme.AMARISCATheme
import com.mr.mukto.utils.NetworkConnectivityManager

class MainActivity : ComponentActivity() {
    private lateinit var networkConnectivityManager: NetworkConnectivityManager
    private var isConnected by mutableStateOf(true)

    private val connectivityObserver = Observer<Boolean> { connected ->
        isConnected = connected
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkConnectivityManager = NetworkConnectivityManager(this)
        networkConnectivityManager.isConnected.observe(this, connectivityObserver)

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
                    NoInternetDialog(isVisible = !isConnected)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkConnectivityManager.startMonitoring()
    }

    override fun onStop() {
        networkConnectivityManager.stopMonitoring()
        super.onStop()
    }

    override fun onDestroy() {
        networkConnectivityManager.isConnected.removeObserver(connectivityObserver)
        super.onDestroy()
    }
}
