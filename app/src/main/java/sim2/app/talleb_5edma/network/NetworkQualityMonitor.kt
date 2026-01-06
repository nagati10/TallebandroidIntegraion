// Add this class to your project
package sim2.app.talleb_5edma.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log

class NetworkQualityMonitor(private val context: Context) {
    
    enum class NetworkQuality {
        POOR,      // 2G/Edge, high latency
        FAIR,      // 3G, moderate latency  
        GOOD,      // 4G/LTE
        EXCELLENT  // WiFi, 5G
    }
    
    interface NetworkQualityListener {
        fun onNetworkQualityChanged(quality: NetworkQuality)
        fun onNetworkTypeChanged(type: String)
    }
    
    private var listener: NetworkQualityListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private var monitoring = false
    
    // Network metrics
    private var lastPacketLoss = 0.0
    private var lastLatency = 0L
    private var lastBandwidth = 0L
    
    fun startMonitoring(listener: NetworkQualityListener) {
        this.listener = listener
        monitoring = true
        handler.post(monitoringRunnable)
        Log.d("NetworkMonitor", "Started network quality monitoring")
    }
    
    fun stopMonitoring() {
        monitoring = false
        handler.removeCallbacks(monitoringRunnable)
        Log.d("NetworkMonitor", "Stopped network quality monitoring")
    }
    
    private val monitoringRunnable = object : Runnable {
        override fun run() {
            if (monitoring) {
                checkNetworkQuality()
                handler.postDelayed(this, 2000) // Check every 2 seconds
            }
        }
    }
    
    private fun checkNetworkQuality() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val networkType = getNetworkType(capabilities)
        val quality = estimateNetworkQuality(capabilities, networkType)
        
        listener?.onNetworkTypeChanged(networkType)
        listener?.onNetworkQualityChanged(quality)
        
        Log.d("NetworkMonitor", "Network: $networkType, Quality: $quality")
    }
    
    private fun getNetworkType(capabilities: NetworkCapabilities?): String {
        return when {
            capabilities == null -> "DISCONNECTED"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> "MOBILE"
                    else -> "MOBILE_LIMITED"
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "UNKNOWN"
        }
    }
    
    private fun estimateNetworkQuality(capabilities: NetworkCapabilities?, networkType: String): NetworkQuality {
        return when (networkType) {
            "WiFi", "ETHERNET" -> {
                // Check if it's actually good WiFi
                if (capabilities?.linkDownstreamBandwidthKbps ?: 0 > 5000) {
                    NetworkQuality.EXCELLENT
                } else {
                    NetworkQuality.GOOD
                }
            }
            "MOBILE" -> {
                val bandwidth = capabilities?.linkDownstreamBandwidthKbps ?: 0
                when {
                    bandwidth > 10000 -> NetworkQuality.GOOD      // LTE Advanced
                    bandwidth > 2000 -> NetworkQuality.FAIR       // 4G/LTE
                    else -> NetworkQuality.POOR                   // 3G/Edge
                }
            }
            "MOBILE_LIMITED" -> NetworkQuality.POOR
            else -> NetworkQuality.POOR
        }
    }
    
    // Call these methods from WebSocket events to update network metrics
    fun updatePacketLoss(lossPercentage: Double) {
        lastPacketLoss = lossPercentage
    }
    
    fun updateLatency(latencyMs: Long) {
        lastLatency = latencyMs
    }
    
    fun updateBandwidth(bandwidthKbps: Long) {
        lastBandwidth = bandwidthKbps
    }
}