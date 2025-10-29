package ViniShuet.com.github.android_crypto_monitor

import ViniShuet.com.github.android_crypto_monitor.service.MercadoBitcoinServiceFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoMonitorApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoMonitorApp() {
    val scope = rememberCoroutineScope()
    var bitcoinValue by remember { mutableStateOf("—") }
    var lastUpdate by remember { mutableStateOf("—") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotação Bitcoin", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Cotação Atual", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(bitcoinValue, fontSize = 36.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Última atualização: $lastUpdate", fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val service = MercadoBitcoinServiceFactory().create()
                            val response = service.getTicker()
                            if (response.isSuccessful) {
                                val ticker = response.body()?.ticker
                                val lastValue = ticker?.last?.toDoubleOrNull()
                                val date = ticker?.date?.let { Date(it * 1000L) }

                                val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                                bitcoinValue = lastValue?.let { numberFormat.format(it) } ?: "Erro"

                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                lastUpdate = sdf.format(date ?: Date())
                            } else {
                                errorMessage = "Erro: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Falha: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else
                    Text("Atualizar")
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
