package com.jminnovatech.joymart.ui.distributor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.*
import com.jminnovatech.joymart.data.model.distributor.DistributorProduct
import com.jminnovatech.joymart.data.model.distributor.DistributorCategory
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorProductScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var products by remember { mutableStateOf<List<DistributorProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<DistributorCategory>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var showSheet by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<DistributorProduct?>(null) }

    // ================= LOAD DATA =================

    suspend fun loadData() {
        try {
            val productRes = api.getProducts()
            if (productRes.success) {
                products = productRes.data?.data ?: emptyList()
            }

            val categoryRes = api.getCategories()
            if (categoryRes.success) {
                categories = categoryRes.data ?: emptyList()
            }

        } catch (_: Exception) { }

        loading = false
        refreshing = false
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    // ================= MAIN UI =================

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editProduct = null
                    showSheet = true
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            SwipeRefresh(
                state = rememberSwipeRefreshState(refreshing),
                onRefresh = {
                    refreshing = true
                    scope.launch { loadData() }
                }
            ) {

                if (products.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Products Found")
                    }
                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        items(products) { product ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    AsyncImage(
                                        model = product.image_url,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(product.title)
                                        Text("₹${product.sell_price}")
                                        Text("Stock: ${product.stock_qty} ${product.unit}")
                                    }

                                    IconButton(
                                        onClick = {
                                            editProduct = product
                                            showSheet = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, null)
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                api.deleteProduct(product.id)
                                                loadData()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= ADD / EDIT SHEET =================

        if (showSheet) {

            ModalBottomSheet(
                onDismissRequest = { showSheet = false }
            ) {

                var title by remember(editProduct) {
                    mutableStateOf(editProduct?.title ?: "")
                }

                var basePrice by remember(editProduct) {
                    mutableStateOf(editProduct?.base_price?.toString() ?: "")
                }

                var sellPrice by remember(editProduct) {
                    mutableStateOf(editProduct?.sell_price?.toString() ?: "")
                }

                var stock by remember(editProduct) {
                    mutableStateOf(editProduct?.stock_qty?.toString() ?: "")
                }

                var description by remember(editProduct) {
                    mutableStateOf(editProduct?.description ?: "")
                }

                var selectedCategory by remember(editProduct) {
                    mutableStateOf(
                        categories.find { it.id == editProduct?.category_id }
                    )
                }

                var unit by remember(editProduct) {
                    mutableStateOf(editProduct?.unit ?: "pcs")
                }

                var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

                val units = listOf(
                    "kg","g","ltr","ml","pcs","pkt","box","dozen"
                )

                val imagePicker = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val stream = context.contentResolver.openInputStream(it)
                        imageBitmap = BitmapFactory.decodeStream(stream)
                    }
                }

                Column(Modifier.padding(16.dp)) {

                    Text(
                        if (editProduct == null) "Add Product" else "Edit Product",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Product Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // CATEGORY DROPDOWN
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {

                        OutlinedTextField(
                            value = selectedCategory?.name ?: "-- Select Category --",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {

                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // UNIT DROPDOWN
                    var unitExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded }
                    ) {

                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        unit = it
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it },
                        label = { Text("Base Price") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        label = { Text("Sell Price") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Image")
                    }

                    imageBitmap?.let {
                        Spacer(Modifier.height(8.dp))
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {

                            scope.launch {

                                val categoryBody = selectedCategory?.id
                                    ?.toString()
                                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                                val descriptionBody =
                                    description.toRequestBody("text/plain".toMediaTypeOrNull())

                                val compressedImage = imageBitmap?.let {
                                    val stream = ByteArrayOutputStream()
                                    it.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                                    val bytes = stream.toByteArray()
                                    MultipartBody.Part.createFormData(
                                        "image",
                                        "product.jpg",
                                        bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                    )
                                }

                                if (editProduct == null) {

                                    api.addProduct(
                                        title.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        categoryBody,
                                        descriptionBody,
                                        basePrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        sellPrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        stock.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        unit.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        compressedImage
                                    )

                                } else {

                                    api.updateProduct(
                                        editProduct!!.id,
                                        title.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        categoryBody,
                                        descriptionBody,
                                        basePrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        sellPrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        stock.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        unit.toRequestBody("text/plain".toMediaTypeOrNull()),
                                        compressedImage
                                    )
                                }


                            }
                        }
                    ) {
                        Text("Save")
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
