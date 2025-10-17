package com.example.espScanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var receiver: BroadcastReceiver
    private lateinit var adapter: WifiAdapter
    private var filterPrefix = "prise-"

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val ok = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (ok) startScan()
        else Toast.makeText(this, "Permission location requise pour scanner le Wi-Fi", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val recycler = findViewById<RecyclerView>(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = WifiAdapter { sr -> onItemClicked(sr) }
        recycler.adapter = adapter

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val results = wifiManager.scanResults.filter {
                    it.SSID.startsWith(filterPrefix, ignoreCase = true)
                }.sortedByDescending { it.level }
                adapter.submitList(results)
                findViewById<TextView>(R.id.tvCount).text = "Modules trouvés: ${results.size}"
            }
        }
        registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        findViewById<Button>(R.id.btnRefresh).setOnClickListener { startScan() }
        findViewById<Button>(R.id.btnSettings).setOnClickListener { showSettingsDialog() }

        // request permissions and start scan
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun startScan() {
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        val ok = wifiManager.startScan()
        if (!ok) Toast.makeText(this, "Échec du démarrage du scan Wi-Fi", Toast.LENGTH_SHORT).show()
    }

    private fun showSettingsDialog() {
        val et = EditText(this)
        et.setText(filterPrefix)
        AlertDialog.Builder(this)
            .setTitle("Filtre SSID")
            .setMessage("Entrer le préfixe à détecter (ex: prise-)")
            .setView(et)
            .setPositiveButton("OK") { _, _ ->
                filterPrefix = et.text.toString()
                startScan()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun onItemClicked(sr: ScanResult) {
        val info = "SSID: ${sr.SSID}\nBSSID: ${sr.BSSID}\nRSSI: ${sr.level} dBm"
        val dlg = AlertDialog.Builder(this)
            .setTitle(sr.SSID)
            .setMessage(info)
            .setPositiveButton("Ouvrir réglages Wi‑Fi") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            .setNeutralButton("Copier SSID") { _, _ ->
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("SSID", sr.SSID))
                Toast.makeText(this, "SSID copié", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .create()
        dlg.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

class WifiAdapter(private val onClick: (ScanResult) -> Unit) : RecyclerView.Adapter<WifiAdapter.VH>() {
    private var data = listOf<ScanResult>()

    fun submitList(newList: List<ScanResult>) {
        data = newList
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val sr = data[position]
        holder.tvName.text = sr.SSID
        holder.tvInfo.text = "RSSI: ${sr.level} dBm  •  ${sr.BSSID}"
        holder.itemView.setOnClickListener { onClick(sr) }
    }

    override fun getItemCount(): Int = data.size
}
