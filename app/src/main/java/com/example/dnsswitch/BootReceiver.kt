package com.example.dnsswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Prefs.monitor(context)) {
            try {
                WifiWatcherService.start(context)
            } catch (e: Exception) {
                // Início em segundo plano pode ser bloqueado em algumas versões do Android.
            }
        }
    }
}
