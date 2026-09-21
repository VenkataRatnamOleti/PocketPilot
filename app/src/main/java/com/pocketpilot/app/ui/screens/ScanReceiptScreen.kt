package com.pocketpilot.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(onBack: () -> Unit, onReceiptSaved: (Double, String, String) -> Unit) {
    val context = LocalContext.current
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var error by remember { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> photo = bitmap }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) camera.launch(null) else error = "Camera permission is needed to capture a receipt."
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Scan receipt") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Capture the receipt, then confirm the transaction details before saving.", style = MaterialTheme.typography.bodyMedium)
            Card(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    if (photo == null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(8.dp)); Text("No receipt captured")
                    } else Image(photo!!.asImageBitmap(), "Captured receipt", modifier = Modifier.fillMaxSize())
                }
            }
            OutlinedButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) camera.launch(null) else permission.launch(Manifest.permission.CAMERA)
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(if (photo == null) "Open camera" else "Retake photo") }
            Text("Receipt review", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text("Merchant or note") }, singleLine = true)
            OutlinedTextField(amount, { input -> if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) amount = input }, Modifier.fillMaxWidth(), label = { Text("Total amount (₹)") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                val total = amount.toDoubleOrNull()
                if (total == null || total <= 0) error = "Enter a valid receipt total." else {
                    onReceiptSaved(total, category.ifBlank { "Other" }, merchant.ifBlank { "Scanned receipt" }); onBack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save receipt expense") }
            Text("Your receipt image stays on this device; PocketPilot stores only the confirmed transaction details.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
