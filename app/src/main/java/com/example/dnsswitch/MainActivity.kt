package com.example.dnsswitch

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var toggleBtn: Button
    private lateinit var help: TextView
    private lateinit var copyBtn: Button

    private lateinit var monitorSwitch: Switch
    private lateinit var addBtn: Button
    private lateinit var networks: LinearLayout
    private lateinit var autoHelp: TextView
    private lateinit var autoCopyBtn: Button

    private var updating = false

    private val adbCommand =
        "adb shell pm grant com.example.dnsswitch android.permission.WRITE_SECURE_SETTINGS"

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = dp(24)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }

        // --- Alternância manual ---
        status = TextView(this).apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        toggleBtn = Button(this).apply {
            textSize = 18f
            setOnClickListener { onToggle() }
        }
        help = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, pad / 2, 0, pad / 2)
        }
        copyBtn = Button(this).apply {
            text = "Copiar comando ADB"
            setOnClickListener { copy(adbCommand) }
        }

        // --- Desativar em Wi-Fi específico ---
        val title = TextView(this).apply {
            text = "Desativar automaticamente em Wi-Fi específico"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, pad * 2, 0, pad / 3)
        }
        val explain = TextView(this).apply {
            text = "Ao conectar numa das redes abaixo, o DNS é desativado. Ao sair dela, volta ao estado anterior."
            textSize = 14f
        }
        monitorSwitch = Switch(this).apply {
            text = "Ativar monitoramento  "
            textSize = 16f
            setPadding(0, pad / 2, 0, pad / 2)
            setOnCheckedChangeListener { _, checked -> if (!updating) onMonitorChanged(checked) }
        }
        addBtn = Button(this).apply {
            text = "Adicionar rede Wi-Fi atual"
            setOnClickListener { addCurrentNetwork() }
        }
        networks = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        autoHelp = TextView(this).apply {
            textSize = 13f
            setPadding(0, pad / 2, 0, pad / 3)
        }
        autoCopyBtn = Button(this).apply {
            text = "Copiar comandos ADB"
            setOnClickListener { copy(missingPermissionCommands().joinToString("\n")) }
        }

        listOf(
            status, toggleBtn, help, copyBtn, title, explain, monitorSwitch,
            addBtn, networks, autoHelp, autoCopyBtn
        ).forEach { root.addView(it) }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        if (Prefs.monitor(this) && missingPermissionCommands().isEmpty()) {
            try {
                WifiWatcherService.start(this)
            } catch (e: Exception) {
            }
        }
        refresh()
    }

    private fun copy(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("adb", text))
        Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show()
    }

    private fun onToggle() {
        try {
            DnsController.toggle(this)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permissão ausente. Veja as instruções abaixo.", Toast.LENGTH_LONG).show()
        }
        refresh()
    }

    private fun onMonitorChanged(checked: Boolean) {
        if (checked) {
            if (missingPermissionCommands().isNotEmpty()) {
                Toast.makeText(this, "Faltam permissões. Veja os comandos abaixo.", Toast.LENGTH_LONG).show()
            } else {
                Prefs.setMonitor(this, true)
                WifiWatcherService.start(this)
            }
        } else {
            Prefs.setMonitor(this, false)
            WifiWatcherService.stop(this)
        }
        refresh()
    }

    private fun addCurrentNetwork() {
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        val raw = wm.connectionInfo?.ssid
        if (raw == null || raw == "<unknown ssid>") {
            Toast.makeText(
                this,
                "Não consegui ler o nome da rede. Conecte-se ao Wi-Fi, ative a Localização e conceda as permissões.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        Prefs.addSsid(this, raw.removeSurrounding("\""))
        refresh()
    }

    private fun missingPermissionCommands(): List<String> {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 29) perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        return perms
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
            .map { "adb shell pm grant $packageName $it" }
    }

    private fun refresh() {
        // Alternância manual
        val on = DnsController.isAdGuardOn(this)
        status.text = if (on) "DNS: AdGuard ativado" else "DNS: desativado"
        toggleBtn.text = if (on) "Desativar" else "Ativar AdGuard"

        val granted = DnsController.hasPermission(this)
        copyBtn.visibility = if (granted) View.GONE else View.VISIBLE
        help.text = if (granted) {
            "Adicione o bloco \"DNS AdGuard\" nas Configurações rápidas (ícone de lápis) para alternar com um toque."
        } else {
            "Falta uma permissão, concedida uma única vez. Conecte o celular ao computador com a depuração USB ativada e execute:\n\n$adbCommand"
        }

        // Redes Wi-Fi
        updating = true
        monitorSwitch.isChecked = Prefs.monitor(this)
        updating = false

        networks.removeAllViews()
        val saved = Prefs.ssids(this).sorted()
        if (saved.isEmpty()) {
            networks.addView(TextView(this).apply {
                text = "Nenhuma rede adicionada."
                textSize = 14f
                setPadding(0, dp(8), 0, dp(8))
            })
        }
        saved.forEach { ssid ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(TextView(this).apply {
                text = ssid
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(Button(this).apply {
                text = "Remover"
                setOnClickListener {
                    Prefs.removeSsid(this@MainActivity, ssid)
                    refresh()
                }
            })
            networks.addView(row)
        }

        val missing = missingPermissionCommands()
        autoCopyBtn.visibility = if (missing.isEmpty()) View.GONE else View.VISIBLE
        autoHelp.text = if (missing.isEmpty()) {
            "Permissões ok. Para o nome da rede ser lido, a Localização do celular precisa estar ligada."
        } else {
            "Para o monitoramento funcionar, execute no computador (celular em USB):\n\n" +
                missing.joinToString("\n") +
                "\n\nA Localização do celular também precisa estar ligada, só para ler o nome da rede."
        }
    }
}
