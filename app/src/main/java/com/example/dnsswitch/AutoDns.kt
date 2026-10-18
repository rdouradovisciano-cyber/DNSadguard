package com.example.dnsswitch

import android.content.Context

/**
 * Regra automática: ao entrar numa rede Wi-Fi escolhida, desativa o DNS (se o AdGuard estiver
 * ligado). Ao sair dela, volta ao estado em que estava.
 */
object AutoDns {

    /** Chamado quando o nome da rede Wi-Fi atual é conhecido. */
    fun onWifi(ctx: Context, ssid: String) {
        val matched = Prefs.ssids(ctx).contains(ssid)
        try {
            if (matched && !Prefs.inExclusion(ctx)) {
                val wasOn = DnsController.isAdGuardOn(ctx)
                Prefs.setRestoreOn(ctx, wasOn)
                Prefs.setInExclusion(ctx, true)
                if (wasOn) DnsController.setAdGuard(ctx, false)
            } else if (!matched && Prefs.inExclusion(ctx)) {
                restoreIfNeeded(ctx)
            }
        } catch (e: SecurityException) {
            // Sem a permissão WRITE_SECURE_SETTINGS: nada a fazer.
        }
    }

    /** Chamado quando o Wi-Fi é perdido (saiu do alcance ou trocou para dados móveis). */
    fun onWifiLost(ctx: Context) = restoreIfNeeded(ctx)

    fun restoreIfNeeded(ctx: Context) {
        if (!Prefs.inExclusion(ctx)) return
        try {
            if (Prefs.restoreOn(ctx)) DnsController.setAdGuard(ctx, true)
        } catch (e: SecurityException) {
            // ignorado
        }
        Prefs.setInExclusion(ctx, false)
    }
}
