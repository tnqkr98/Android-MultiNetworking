package com.example.networktest

import android.content.Context
import android.net.*
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity() {
    private val DEBUG_TAG = "NetworkStatusExample"

    lateinit var connMgr: ConnectivityManager

    lateinit var requestNetworkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(
                    "DefaultNetwork",
                    "onAvailable Network : ${network.networkHandle}"
                )
                printNetworkInfo(network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d("DefaultNetwork", "onLost Network : ${network.networkHandle}")
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.d("DefaultNetwork", "onUnavailable")
            }
        })

        findViewById<Button>(R.id.get_all_network).setOnClickListener {

            connMgr.allNetworks.iterator().forEach {
                printNetworkInfo(it)
            }
        }


        findViewById<Button>(R.id.request_network).setOnClickListener {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid("THETAYN14100547.OSC")
                .setWpa2Passphrase("14100547")
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()


            requestNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(
                        "AdditionalNetwork",
                        "onAvailable Network : ${network.networkHandle}"
                    )

                    printNetworkInfo(network)
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.IO) {
                            val conn = network.openConnection(URL("http://192.168.1.1:80/osc/info")) as HttpURLConnection

                            conn.requestMethod = "GET"
                            conn.connect()

                            val inputStream: InputStream = conn.inputStream

                            val br = BufferedReader(InputStreamReader(inputStream))
                            var line: String?
                            val response = StringBuffer()
                            while (br.readLine().also { line = it } != null) {
                                response.append(line)
                                response.append('\r')
                            }
                            br.close()
                            Log.d("response", response.toString())
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("AdditionalNetwork", "onLost Network : ${network.networkHandle}")
                    printNetworkInfo(network)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d("AdditionalNetwork", "onUnavailable")
                }
            }

            connMgr.requestNetwork(
                request,
                requestNetworkCallback,
                Handler(Looper.getMainLooper()),
                25000
            )
        }
        //http://192.168.1.1:80/osc/info
        findViewById<Button>(R.id.ost_request).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://192.168.1.1:80/osc/info")
                    .build()

                val response = client.newCall(request).execute()
                response.body?.let { it1 -> Log.d("cupix network", it1.string()) }
            }
        }

        findViewById<Button>(R.id.mobile_button).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://google.com")
                    .build()

                val response = client.newCall(request).execute()
                response.body?.let { it1 -> Log.d("cupix network", it1.string()) }
            }
        }
    }

    private fun printNetworkInfo(network: Network) {
        // API 31 level 이상 부터 사용가능
        val capability: NetworkCapabilities? = connMgr.getNetworkCapabilities(network)
        Log.d(
            "All_Networks", "--- capability : ${capability?.capabilities?.toString()} " +
                    "\ntransportInfo : ${capability?.transportInfo} " +
                    "\nnetworkSpecifier : ${capability?.networkSpecifier} " +
                    //"\nisAvailable : ${capability?.enterpriseIds} " +       // API Level 33 부터 사용 가능
                    "\nownerUid : ${capability?.ownerUid} "
        )

        // API 31 level 이상 부터 사용가능
        val linkProperties: LinkProperties? = connMgr.getLinkProperties(network)
        Log.d(
            "All_Networks", " --- linkProperties dhcpServerAddress : ${linkProperties?.dhcpServerAddress} " +
                    "\ndnsServers : ${linkProperties?.dnsServers} " +
                    "\ndomains : ${linkProperties?.domains} " +
                    "\nhttpProxy : ${linkProperties?.httpProxy} " +
                    "\nisPrivateDnsActive : ${linkProperties?.isPrivateDnsActive} " +
                    "\nisWakeOnLanSupported : ${linkProperties?.isWakeOnLanSupported} " +
                    "\nlinkAddresses : ${linkProperties?.linkAddresses} " +
                    "\nnat64Prefix : ${linkProperties?.nat64Prefix} " +
                    "\nroutes : ${linkProperties?.routes} "
        )

        // API 29 이후부터 deprecated
        val info: NetworkInfo? = connMgr.getNetworkInfo(network)
        Log.d(
            "All_Networks", "--- Network handle : ${network.networkHandle} " +
                    "\ntypeName : ${info?.typeName} " +
                    "\nisConnected : ${info?.isConnected} " +
                    "\nisAvailable : ${info?.isAvailable} " +
                    "\ndetailedState name : ${info?.detailedState?.name} " +
                    "\ndetailedState ordinal : ${info?.detailedState?.ordinal} " +
                    "\nextraInfo : ${info?.extraInfo} " +
                    "\ntypeName : ${info?.typeName} " +
                    "\nsubtypeName : ${info?.subtypeName} " +
                    "\nisFailover : ${info?.isFailover}" +
                    "\nisRoaming : ${info?.isRoaming}" +
                    "\nisConnectedOrConnecting: ${info?.isConnectedOrConnecting}" +
                    "\nState : ${info?.state}"
        )
    }
}