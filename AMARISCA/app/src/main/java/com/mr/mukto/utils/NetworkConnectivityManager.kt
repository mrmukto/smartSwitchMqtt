package com.mr.mukto.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkConnectivityManager(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected
    private var isMonitoring = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _isConnected.postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            checkConnectivity()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _isConnected.postValue(true)
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        isMonitoring = true
        checkConnectivity()
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Ignore; callback can already be unregistered in rare lifecycle races.
        } finally {
            isMonitoring = false
        }
    }

    private fun checkConnectivity() {
        val isConnected = isNetworkConnected()
        _isConnected.postValue(isConnected)
    }

    private fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
