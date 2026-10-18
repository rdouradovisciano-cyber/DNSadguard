package com.example.dnsswitch

import android.content.Context
import android.content.SharedPreferences

/** Configurações salvas: redes Wi-Fi escolhidas e estado do monitoramento. */
object Prefs {
    private fun sp(c: Context): SharedPreferences =
        c.getSharedPreferences("dns_prefs", Context.MODE_PRIVATE)

    fun ssids(c: Context): Set<String> =
        HashSet(sp(c).getStringSet("ssids", emptySet()) ?: emptySet())

    fun addSsid(c: Context, ssid: String) {
        val s = HashSet(ssids(c))
        s.add(ssid)
        sp(c).edit().putStringSet("ssids", s).apply()
    }

    fun removeSsid(c: Context, ssid: String) {
        val s = HashSet(ssids(c))
        s.remove(ssid)
        sp(c).edit().putStringSet("ssids", s).apply()
    }

    fun monitor(c: Context): Boolean = sp(c).getBoolean("monitor", false)
    fun setMonitor(c: Context, v: Boolean) = sp(c).edit().putBoolean("monitor", v).apply()

    /** true enquanto o celular está numa rede escolhida (DNS foi desativado por nós). */
    fun inExclusion(c: Context): Boolean = sp(c).getBoolean("in_exclusion", false)
    fun setInExclusion(c: Context, v: Boolean) = sp(c).edit().putBoolean("in_exclusion", v).apply()

    /** true se o AdGuard estava ligado antes de entrarmos na rede escolhida. */
    fun restoreOn(c: Context): Boolean = sp(c).getBoolean("restore_on", false)
    fun setRestoreOn(c: Context, v: Boolean) = sp(c).edit().putBoolean("restore_on", v).apply()
}
