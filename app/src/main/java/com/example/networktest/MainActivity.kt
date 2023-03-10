package com.example.networktest

import android.content.Context
import android.net.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var connMgr: ConnectivityManager

    private lateinit var requestNetworkCallback: ConnectivityManager.NetworkCallback

    var mobileNetwork: Network? = null
    var iotNetwork: Network? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE,
            ),
            123
        )

        connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerDefaultNetworkCallback(
            @RequiresApi(Build.VERSION_CODES.S)
            object :
                ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(
                        "DefaultNetwork",
                        "onAvailable Network : ${network.networkHandle}"
                    )
                    //printNetworkInfo(network, "DefaultNetwork")
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
            // API 29 ???????????? deprecated
            connMgr.allNetworks.iterator().forEach {
                //printNetworkInfo(it, "All Network")
            }

            connMgr.activeNetwork?.let {
                //printNetworkInfo(it, "Active Network")
                Log.d(
                    "Active Network",
                    "connMgr.isActiveNetworkMetered :${connMgr.isActiveNetworkMetered}"
                )
            }

            Log.d("Bound Network", "Bound Network : ${connMgr.boundNetworkForProcess}")
            connMgr.boundNetworkForProcess?.let {
                //printNetworkInfo(it, "Bound Network")
            }
        }


        findViewById<Button>(R.id.request_network).setOnClickListener {
            //removeSsid("THETAYN14100547.OSC","14100547")

            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid("THETAYN14100547.OSC")
                .setWpa2Passphrase("14100547")
                //.setSsid("ONE R XUFKFW.OSC")
                //.setWpa2Passphrase("88888888")
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            /*val request2 = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()*/

            requestNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    iotNetwork = network
                    Log.d(
                        "AdditionalNetwork",
                        "onAvailable Network : ${network.networkHandle}"
                    )

                    //printNetworkInfo(network, "AdditionalNetwork")
                    /*CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.IO) {
                            val conn =
                                network.openConnection(URL("http://192.168.1.1:80/osc/info")) as HttpURLConnection

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
                    }*/
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("AdditionalNetwork", "onLost Network : ${network.networkHandle}")
                    //printNetworkInfo(network, "AdditionalNetwork")
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d("AdditionalNetwork", "onUnavailable")
                }
            }

            //val handler = Handler(Looper.getMainLooper())
            //CoroutineScope(Dispatchers.IO).launch {
            connMgr.requestNetwork(
                request,
                requestNetworkCallback,
                //       handler,
                25000
            )
            //}
        }

        findViewById<Button>(R.id.register_network).setOnClickListener {
            val specifier = WifiNetworkSpecifier.Builder()
                //.setSsid("ONE X2 XUFKFW.OSC")
                //.setWpa2Passphrase("88888888")
                //.setSsid("THETAYN14100547.OSC")
                //.setWpa2Passphrase("14100547")
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            requestNetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    iotNetwork = network
                    Log.d(
                        "AdditionalNetwork",
                        "onAvailable Network : ${network.networkHandle}"
                    )

                    //printNetworkInfo(network, "AdditionalNetwork")
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d("AdditionalNetwork", "onLost Network : ${network.networkHandle}")
                    //printNetworkInfo(network, "AdditionalNetwork")
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    Log.d("AdditionalNetwork", "onUnavailable")
                }
            }

            connMgr.registerNetworkCallback(
                request,
                requestNetworkCallback,
                //Handler(Looper.getMainLooper()),
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

        findViewById<Button>(R.id.use_mobile).setOnClickListener {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            // registerNetworkCallback ??? OS ???????????? ???????????? ????????? ???????????? ????????? ???. ????????? ?????????????????? ????????? ?????????.
            connMgr.requestNetwork(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        Log.d("MobileNetwork", "onAvailable")
                        //printNetworkInfo(network, "MobileNetwork")
                        mobileNetwork = network
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        Log.d("MobileNetwork", "onLost")
                    }

                    override fun onUnavailable() {
                        super.onUnavailable()
                        Log.d("MobileNetwork", "onUnavailable")
                    }
                })
        }

        findViewById<Button>(R.id.bind_iot).setOnClickListener {
            iotNetwork?.let {
                Log.d("bindProcess", "BindProcess To Iot")
                connMgr.bindProcessToNetwork(it)
            }
        }

        findViewById<Button>(R.id.bind_mobile).setOnClickListener {
            mobileNetwork?.let {
                Log.d("bindProcess", "BindProcess To Mobile")
                connMgr.bindProcessToNetwork(it)
            }
        }

        findViewById<Button>(R.id.network_capability).setOnClickListener {
            connMgr.activeNetwork?.let { network ->
                //printNetworkInfo(network, "activeNetwork")
                val capabilities = connMgr.getNetworkCapabilities(network)
                val hasInternet =
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                Log.d("HAS_INTERNET", "has internet ? : $hasInternet")
            }
            connMgr.boundNetworkForProcess?.let { network ->
                //printNetworkInfo(network, "activeNetwork")
                val capabilities = connMgr.getNetworkCapabilities(network)
                val hasInternet =
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                Log.d("HAS_INTERNET", "has internet ? : $hasInternet")
            }
        }

        findViewById<Button>(R.id.get_ssid).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                getCurrentWifiSsid(applicationContext)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun printNetworkInfo(network: Network, tag: String) {
        // API 31 level ?????? ?????? ????????????
        val capability: NetworkCapabilities? = connMgr.getNetworkCapabilities(network)
        Log.d(
            tag, "--- capability : ${capability?.capabilities?.toString()} " +
                    "\ntransportInfo : ${capability?.transportInfo} " +
                    "\nnetworkSpecifier : ${capability?.networkSpecifier} " +
                    //"\nisAvailable : ${capability?.enterpriseIds} " +       // API Level 33 ?????? ?????? ??????
                    "\nownerUid : ${capability?.ownerUid} "
        )

        // API 31 level ?????? ?????? ?????? ??????
        val linkProperties: LinkProperties? = connMgr.getLinkProperties(network)
        Log.d(
            tag,
            " --- linkProperties dhcpServerAddress : ${linkProperties?.dhcpServerAddress} " +
                    "\ndnsServers : ${linkProperties?.dnsServers} " +
                    "\ndomains : ${linkProperties?.domains} " +
                    "\nhttpProxy : ${linkProperties?.httpProxy} " +
                    "\nisPrivateDnsActive : ${linkProperties?.isPrivateDnsActive} " +
                    "\nisWakeOnLanSupported : ${linkProperties?.isWakeOnLanSupported} " +
                    "\nlinkAddresses : ${linkProperties?.linkAddresses} " +
                    "\nnat64Prefix : ${linkProperties?.nat64Prefix} " +
                    "\nroutes : ${linkProperties?.routes} "
        )

        // API 29 ???????????? deprecated
        val info: NetworkInfo? = connMgr.getNetworkInfo(network)
        Log.d(
            tag, "--- Network handle : ${network.networkHandle} " +
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

    fun isInternetReachable(): Boolean {
        try {
            // google site has a very good SLA, https://en.wikipedia.org/wiki/Service-level_agreement
            val connection =
                URL("https://www.google.com").openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Test")
            connection.setRequestProperty("Connection", "close")
            connection.connectTimeout = 1500 // configurable
            connection.connect()
            Log.d("Reachable", "isInternetReachable : ${(connection.responseCode == 200)}")
            return (connection.responseCode == 200)
        } catch (e: IOException) {
            Log.e("Reachable", "Error checking internet connection", e)
        }

        Log.d("Reachable", "hasInternetConnected: false")
        return false
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun removeSsid(ssid: String, pass: String) {
        /*val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)
            .build()*/
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        /*val sList = ArrayList<WifiNetworkSuggestion>()
        sList.add(suggestion)*/
        wifiManager.removeNetworkSuggestions(wifiManager.networkSuggestions)
    }

    private fun getCurrentWifiSsid(context: Context) {
        if (Build.VERSION_CODES.S > Build.VERSION.SDK_INT) {
            val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo

            Log.d("GET_SSID", "get ssid : ${wifiInfo.ssid}")
            //return wifiInfo.ssid
        }

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val connMgr = context.getSystemService(ConnectivityManager::class.java)

        val callback = @RequiresApi(Build.VERSION_CODES.S)
        object :
                ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)

                    val info = networkCapabilities.transportInfo as WifiInfo
                    Log.d("GET_SSID", "get ssid : ${info.ssid}")
                }

            }

        connMgr.registerNetworkCallback(networkRequest, callback)

        return
    }
}