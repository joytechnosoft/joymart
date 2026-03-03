package com.jminnovatech.joymart.ui.distributor.modal

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import okhttp3.ResponseBody

@Composable
fun ProductModal(
    onDismiss: () -> Unit
) {

    val api = RetrofitClient.distributorApi
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {

        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        ) {

            Box {

                IconButton(
                    onClick = { if (!loading) onDismiss() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, null)
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .width(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Product Tools",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                try {
                                    loading = true
                                    message = "Preparing Product PDF..."
                                    val body = api.exportProductsPdf()
                                    val file = savePdf(context, body, "products.pdf")
                                    openPdf(context, file)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                                }
                                loading = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Download Product PDF")
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                try {
                                    loading = true
                                    message = "Preparing Barcode PDF..."
                                    val body = api.exportBarcodePdf()
                                    val file = savePdf(context, body, "barcodes.pdf")
                                    openPdf(context, file)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show()
                                }
                                loading = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.QrCode, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Download Barcode PDF")
                    }

                    if (loading) {
                        Spacer(Modifier.height(20.dp))
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(message)
                    }
                }
            }
        }
    }
}

/* ---------------- Helper Functions ---------------- */

private fun savePdf(
    context: Context,
    body: ResponseBody,
    fileName: String
): File {

    val file = File(context.getExternalFilesDir(null), fileName)

    body.byteStream().use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }

    return file
}

private fun openPdf(context: Context, file: File) {

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(intent)
}