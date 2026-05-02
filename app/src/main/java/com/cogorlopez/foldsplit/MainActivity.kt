package com.cogorlopez.foldsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    private val viewModel: SetupViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val runner = ShizukuCommandRunner()
                runner.init()
                @Suppress("UNCHECKED_CAST")
                return SetupViewModel(runner, FoldMonitor(applicationContext), this@MainActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SetupScreen(viewModel)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Shizuku intercepts this via its own listener registered in ShizukuCommandRunner
    }
}

@Composable
private fun SetupScreen(viewModel: SetupViewModel) {
    val shizukuState by viewModel.shizukuState.collectAsState()
    val foldState by viewModel.foldState.collectAsState()
    val setupComplete by viewModel.setupComplete.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FoldSplit",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(32.dp))

        StatusRow(
            label = "Shizuku",
            status = when (shizukuState) {
                RunnerState.READY -> "Ready"
                RunnerState.PERMISSION_NEEDED -> "Permission needed"
                RunnerState.UNAVAILABLE -> "Not running"
            },
            ok = shizukuState == RunnerState.READY,
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusRow(
            label = "Device",
            status = if (foldState == FoldState.FLAT) "Open (inner display)" else "Folded",
            ok = foldState == FoldState.FLAT,
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (shizukuState == RunnerState.PERMISSION_NEEDED) {
            Button(onClick = { viewModel.runner.requestPermission() }) {
                Text("Grant Shizuku Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (setupComplete) {
            Text(
                text = "Ready! Add the \"Split Orientation\" tile from Quick Settings.",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else if (shizukuState == RunnerState.UNAVAILABLE) {
            Text(
                text = "Start Shizuku, then reopen this app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, status: String, ok: Boolean) {
    val color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Text(
        text = "$label: $status",
        style = MaterialTheme.typography.bodyLarge,
        color = color,
    )
}
