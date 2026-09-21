package com.pocketpilot.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(onBack: () -> Unit, onReceiptSaved: (Double, String, String, String?, String) -> Unit) {
    val context=LocalContext.current; var photo by remember { mutableStateOf<Bitmap?>(null) }; var imagePath by remember { mutableStateOf<String?>(null) }
    var merchant by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Food") }; var status by remember { mutableStateOf("Capture a receipt to extract its details.") }
    fun extract(bitmap: Bitmap) { status="Reading receipt…"; TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener { result ->
        val text=result.text; merchant=text.lineSequence().firstOrNull { it.length > 2 && !it.any(Char::isDigit) }?.take(40).orEmpty()
        amount=Regex("(?:total|amount|grand total|rs\\.?|₹)\\s*[:.]?\\s*(\\d+(?:[,.]\\d{2})?)",RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.replace(",","") ?: Regex("\\b\\d+(?:[,.]\\d{2})\\b").findAll(text).lastOrNull()?.value?.replace(",","").orEmpty()
        category=when { text.contains("restaurant",true)||text.contains("cafe",true)||text.contains("food",true) -> "Food"; text.contains("uber",true)||text.contains("petrol",true) -> "Travel"; else -> "Shopping" }; status="Receipt fields were filled from on-device OCR. Please confirm before saving."
    }.addOnFailureListener { status="Could not read text automatically. Enter the details manually." } }
    val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> if(bitmap != null) { photo=bitmap; imagePath=saveReceipt(context,bitmap); extract(bitmap) } }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if(it) camera.launch(null) else status="Camera permission is required." }
    Scaffold(topBar={TopAppBar(title={Text("Scan receipt")},navigationIcon={TextButton(onClick=onBack){Text("Back")}})}) { padding -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()){Box(Modifier.fillMaxWidth().height(210.dp),contentAlignment=Alignment.Center){ if(photo==null) Text("Receipt preview") else Image(photo!!.asImageBitmap(),"Receipt",Modifier.fillMaxSize())}}
        Button(onClick={if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) camera.launch(null) else permission.launch(Manifest.permission.CAMERA)},Modifier.fillMaxWidth()){Text("Capture receipt")}
        Text(status,style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(merchant,{merchant=it},Modifier.fillMaxWidth(),label={Text("Merchant")},singleLine=true)
        OutlinedTextField(amount,{amount=it.filter { c->c.isDigit()||c=='.'}},Modifier.fillMaxWidth(),label={Text("Total amount (₹)")},singleLine=true)
        OutlinedTextField(category,{category=it},Modifier.fillMaxWidth(),label={Text("Category")},singleLine=true)
        Button(onClick={amount.toDoubleOrNull()?.takeIf { it>0 }?.let { onReceiptSaved(it,category,merchant.ifBlank{"Scanned receipt"},imagePath,merchant);onBack() }},Modifier.fillMaxWidth()){Text("Save to recent expenses")}
    }}
}
private fun saveReceipt(context: Context, bitmap: Bitmap): String? = try { File(context.filesDir,"receipts").apply{mkdirs()}.let { dir -> File(dir,"receipt_${System.currentTimeMillis()}.jpg") }.also { FileOutputStream(it).use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG,85,output) } }.absolutePath } catch(_: Exception){null}
