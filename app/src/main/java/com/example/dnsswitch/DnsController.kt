package com.example.dnsswitch

import android.content.Context
import android.provider.Settings

object DnsController {
    const val HOST = "dns.adguard-dns.com"
    private const val MODE = "private_dns_mode"
    private const val SPECIFIER = "private_dns_specifier"

    /** true quando o DNS particular está no modo "hostname" apontando para o AdGuard. */
    fun isAdGuardOn(context: Context): Boolean {
        val cr = context.contentResolver
        return Settings.Global.getString(cr, MODE) == "hostname" &&
            Settings.Global.getString(cr, SPECIFIER) == HOST
    }

    /** Liga o AdGuard ou desativa o DNS particular. Lança SecurityException se faltar a permissão. */
    fun setAdGuard(context: Context, on: Boolean) {
        val cr = context.contentResolver
        if (on) {
            Settings.Global.putString(cr, SPECIFIER, HOST)
            Settings.Global.putString(cr, MODE, "hostname")
        } else {
            Settings.Global.putString(cr, MODE, "off")
        }
    }

    /** Alterna entre desativado e AdGuard. */
    fun toggle(context: Context): Boolean {
        val turnOn = !isAdGuardOn(context)
        setAdGuard(context, turnOn)
        return turnOn
    }

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
}
