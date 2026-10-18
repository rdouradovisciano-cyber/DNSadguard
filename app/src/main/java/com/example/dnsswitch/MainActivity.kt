package com.example.dnsswitch

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var toggleBtn: Button
    private lateinit var help: TextView
    private lateinit var copyBtn: Button

    private val adbCommand =
        "adb shell pm grant com.example.dnsswitch android.permission.WRITE_SECURE_SETTINGS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
        }

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
            setPadding(0, pad, 0, pad / 2)
        }
        copyBtn = Button(this).apply {
            text = "Copiar comando ADB"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("adb", adbCommand))
                Toast.makeText(this@MainActivity, "Comando copiado", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(status)
        root.addView(toggleBtn)
        root.addView(help)
        root.addView(copyBtn)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun onToggle() {
        try {
            DnsController.toggle(this)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permissão ausente. Veja as instruções abaixo.", Toast.LENGTH_LONG).show()
        }
        refresh()
    }

    private fun refresh() {
        val on = DnsController.isAdGuardOn(this)
        status.text = if (on) "DNS: AdGuard ativado" else "DNS: desativado"
        toggleBtn.text = if (on) "Desativar" else "Ativar AdGuard"

        val granted = DnsController.hasPermission(this)
        copyBtn.visibility = if (granted) android.view.View.GONE else android.view.View.VISIBLE
        help.text = if (granted) {
            "Pronto! Adicione o bloco \"DNS AdGuard\" nas Configurações rápidas (ícone de lápis) para alternar com um toque."
        } else {
            "Falta uma permissão, concedida uma única vez. Conecte o celular ao computador com a depuração USB ativada e execute:\n\n$adbCommand"
        }
    }
}
