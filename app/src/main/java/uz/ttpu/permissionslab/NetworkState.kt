package uz.ttpu.permissionslab

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

@Composable
fun NetworkState() {
    val context = LocalContext.current
    val manager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    var online by remember { mutableStateOf(manager.isOnline()) }

    DisposableEffect(manager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                online = manager.isOnline()
            }

            override fun onLost(network: Network) {
                online = manager.isOnline()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                online = manager.isOnline()
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager.registerNetworkCallback(request, callback)
        onDispose {
            manager.unregisterNetworkCallback(callback)
        }
    }

    Text("Network: ${if (online) "Online" else "Offline"}")

    var requestResult by remember { mutableStateOf("Network test not run.") }
    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            requestResult = try {
                withContext(Dispatchers.IO) {
                    val connection = URL("https://example.com").openConnection()
                    connection.connectTimeout = 5_000
                    connection.readTimeout = 5_000
                    connection.connect()
                }
                "Network request succeeded."
            } catch (error: SecurityException) {
                "SecurityException: ${error.message}"
            } catch (error: IOException) {
                "Network request failed: ${error.message}"
            }
        }
    }) {
        Text("Test Internet permission")
    }
    Text(requestResult)
}

private fun ConnectivityManager.isOnline(): Boolean {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
        return activeNetworkInfo?.isConnected == true
    }
    val network = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(network) ?: return false
    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
        return false
    }
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
