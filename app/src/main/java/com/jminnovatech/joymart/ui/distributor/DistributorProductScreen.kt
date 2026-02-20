package com.jminnovatech.joymart.ui.distributor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.*
import com.jminnovatech.joymart.data.model.distributor.*
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorProductScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var products by remember { mutableStateOf<List<DistributorProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<DistributorCategory>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var showForm by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<DistributorProduct?>(null) }
    var deleteProduct by remember { mutableStateOf<DistributorProduct?>(null) }

    suspend fun loadData() {
        try {
            val p = api.getProducts()
            if (p.success) products = p.data?.data ?: emptyList()

            val c = api.getCategories()
            if (c.success) categories = c.data ?: emptyList()
        } catch (_: Exception) {}
        loading = false
        refreshing = false
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editProduct = null
                showForm = true
            }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->

        SwipeRefresh(
            state = rememberSwipeRefreshState(refreshing),
            onRefresh = {
                refreshing = true
                scope.launch { loadData() }
            }
        ) {

            LazyColumn(Modifier.padding(padding)) {

                items(products) { product ->

                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Row(Modifier.padding(14.dp)) {

                            AsyncImage(
                                model = product.image_url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(Modifier.width(14.dp))

                            Column(Modifier.weight(1f)) {

                                Text(product.title, fontWeight = FontWeight.Bold)

                                Text("MRP: ₹${product.mrp}")
                                Text("Sell: ₹${product.sell_price}")
                                Text("Discount: ${product.discount_percent?.roundToInt()}%")

                                if (product.stock_qty <= 5) {
                                    Text(
                                        "Low Stock!",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text("Stock: ${product.stock_qty} ${product.unit}")
                            }

                            Column {
                                IconButton(onClick = {
                                    editProduct = product
                                    showForm = true
                                }) {
                                    Icon(Icons.Default.Edit, null)
                                }

                                IconButton(onClick = {
                                    deleteProduct = product
                                }) {
                                    Icon(Icons.Default.Delete, null)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // ================= DELETE DIALOG =================

    deleteProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteProduct = null },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        api.deleteProduct(product.id)
                        deleteProduct = null
                        loadData()
                        snackbar.showSnackbar("Deleted")
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteProduct = null }) { Text("Cancel") }
            },
            title = { Text("Delete Product") },
            text = { Text("Are you sure?") }
        )
    }

    // ================= FULL FORM DIALOG =================

    if (showForm) {

        var title by remember { mutableStateOf(editProduct?.title ?: "") }
        var basePrice by remember { mutableStateOf(editProduct?.base_price?.toString() ?: "") }
        var sellPrice by remember { mutableStateOf(editProduct?.sell_price?.toString() ?: "") }
        var mrp by remember { mutableStateOf(editProduct?.mrp?.toString() ?: "") }
        var stock by remember { mutableStateOf(editProduct?.stock_qty?.toString() ?: "") }
        var description by remember { mutableStateOf(editProduct?.description ?: "") }
        var unit by remember { mutableStateOf(editProduct?.unit ?: "pcs") }
        var uploading by remember { mutableStateOf(false) }

        var selectedCategory by remember(editProduct, categories) {
            mutableStateOf(categories.find { it.id == editProduct?.category_id })
        }

        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

        val discount = remember(mrp, sellPrice) {
            val m = mrp.toDoubleOrNull() ?: 0.0
            val s = sellPrice.toDoubleOrNull() ?: 0.0
            if (m > 0) ((m - s) / m * 100) else 0.0
        }

        val galleryLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                it?.let { uri ->
                    val stream = context.contentResolver.openInputStream(uri)
                    imageBitmap = BitmapFactory.decodeStream(stream)
                }
            }

        val cameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                imageBitmap = it
            }

        Dialog(onDismissRequest = { if (!uploading) showForm = false }) {

            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 8.dp
            ) {

                Box {

                    LazyColumn(Modifier.padding(20.dp)) {

                        item {
                            Text(
                                if (editProduct == null) "Add Product" else "Update Product",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        item { Spacer(Modifier.height(14.dp)) }

                        item {
                            OutlinedTextField(title, { title = it }, label = { Text("Title") })
                        }

                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            OutlinedTextField(basePrice, { basePrice = it }, label = { Text("Base Price") })
                        }

                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            OutlinedTextField(mrp, { mrp = it }, label = { Text("MRP") })
                        }

                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            OutlinedTextField(sellPrice, { sellPrice = it }, label = { Text("Sell Price") })
                        }

                        item {
                            Text(
                                "Discount: ${discount.roundToInt()}%",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            OutlinedTextField(stock, { stock = it }, label = { Text("Stock") })
                        }

                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            OutlinedTextField(description, { description = it }, label = { Text("Description") })
                        }

                        item { Spacer(Modifier.height(12.dp)) }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { cameraLauncher.launch(null) }) {
                                    Icon(Icons.Default.CameraAlt, null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Camera")
                                }
                                Button(onClick = { galleryLauncher.launch("image/*") }) {
                                    Icon(Icons.Default.Image, null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Gallery")
                                }
                            }
                        }

                        item { Spacer(Modifier.height(10.dp)) }

                        item {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp)
                                )
                            } else if (editProduct?.image_url != null) {
                                AsyncImage(
                                    model = editProduct!!.image_url,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp)
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        item {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {

                                        try {
                                            uploading = true

                                            val imagePart = imageBitmap?.let {
                                                val stream = ByteArrayOutputStream()
                                                it.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                                                MultipartBody.Part.createFormData(
                                                    "image",
                                                    "product.jpg",
                                                    stream.toByteArray()
                                                        .toRequestBody("image/jpeg".toMediaTypeOrNull())
                                                )
                                            }

                                            if (editProduct == null) {
                                                api.addProduct(
                                                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    selectedCategory?.id?.toString()
                                                        ?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    description.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    basePrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    sellPrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    mrp.takeIf { it.isNotBlank() }
                                                        ?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    stock.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    unit.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    imagePart
                                                )
                                            } else {
                                                api.updateProduct(
                                                    editProduct!!.id,
                                                    title.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    selectedCategory?.id?.toString()
                                                        ?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    description.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    basePrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    sellPrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    mrp.takeIf { it.isNotBlank() }
                                                        ?.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    stock.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    unit.toRequestBody("text/plain".toMediaTypeOrNull()),
                                                    imagePart
                                                )
                                            }

                                            showForm = false
                                            loadData()
                                            snackbar.showSnackbar("Saved Successfully")

                                        } catch (e: HttpException) {
                                            snackbar.showSnackbar("Validation Error")
                                        }

                                        uploading = false
                                    }
                                }
                            ) {
                                Text(if (uploading) "Uploading..." else "Save")
                            }
                        }

                        item { Spacer(Modifier.height(20.dp)) }
                    }

                    if (uploading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}