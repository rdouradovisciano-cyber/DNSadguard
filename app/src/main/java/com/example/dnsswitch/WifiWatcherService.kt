package com.example.dnsswitch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder

/** Fica observando o Wi-Fi para aplicar a regra de [AutoDns]. */
class WifiWatcherService : Service() {

    private var callback: ConnectivityManager.NetworkCallback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startAsForeground()
        } catch (e: Exception) {
            // Sem permissão de localização ou início em segundo plano não permitido.
            stopSelf()
            return START_NOT_STICKY
        }
        register()
        return START_STICKY
    }

    override fun onDestroy() {
        callback?.let {
            try {
                (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                    .unregisterNetworkCallback(it)
            } catch (e: Exception) {
            }
        }
        callback = null
        super.onDestroy()
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Monitoramento de Wi-Fi", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_dns)
            .setContentTitle("Alternar DNS")
            .setContentText("Monitorando redes Wi-Fi")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }

    private fun register() {
        if (callback != null) return
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val cb: ConnectivityManager.NetworkCallback =
            if (Build.VERSION.SDK_INT >= 31) {
                object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                    override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) =
                        handleCaps(caps)

                    override fun onLost(network: Network) = AutoDns.onWifiLost(this@WifiWatcherService)
                }
            } else {
                object : ConnectivityManager.NetworkCallback() {
                    override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) =
                        handleCaps(caps)

                    override fun onLost(network: Network) = AutoDns.onWifiLost(this@WifiWatcherService)
                }
            }
        cm.registerNetworkCallback(request, cb)
        callback = cb
    }

    private fun handleCaps(caps: NetworkCapabilities) {
        val ssid = readSsid(caps) ?: return
        AutoDns.onWifi(this, ssid)
    }

    private fun readSsid(caps: NetworkCapabilities): String? {
        var raw: String? = null
        if (Build.VERSION.SDK_INT >= 29) {
            raw = (caps.transportInfo as? WifiInfo)?.ssid
        }
        if (raw == null || raw == UNKNOWN) {
            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            raw = wm.connectionInfo?.ssid
        }
        if (raw == null || raw == UNKNOWN) return null
        return raw.removeSurrounding("\"")
    }

    companion object {
        private const val CHANNEL = "wifi_watcher"
        private const val UNKNOWN = "<unknown ssid>"

        fun start(c: Context) {
            val i = Intent(c, WifiWatcherService::class.java)
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i) else c.startService(i)
        }

        fun stop(c: Context) {
            c.stopService(Intent(c, WifiWatcherService::class.java))
            AutoDns.restoreIfNeeded(c)
        }
    }
}
