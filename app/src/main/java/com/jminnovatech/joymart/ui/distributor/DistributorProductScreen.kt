package com.jminnovatech.joymart.ui.distributor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorProductScreen(refreshTrigger: Int) {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var downloading by remember { mutableStateOf(false) }
    var downloadMessage by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<DistributorProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<DistributorCategory>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUnitFilter by remember { mutableStateOf<String?>(null) }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<Int?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<DistributorProduct?>(null) }
    var deleteProduct by remember { mutableStateOf<DistributorProduct?>(null) }
// 🔍 FILTER + PAGINATION
    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 10
    var showNewCategory by remember { mutableStateOf(false) }
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
    val filteredProducts = products.filter { product ->

        val matchesSearch =
            product.title.contains(searchQuery, ignoreCase = true)

        val matchesUnit =
            selectedUnitFilter == null || product.unit == selectedUnitFilter

        val matchesLowStock =
            !showLowStockOnly || product.stock_qty <= 5

        val matchesCategory =
            selectedCategoryFilter == null ||
                    product.category_id == selectedCategoryFilter

        matchesSearch && matchesUnit && matchesLowStock && matchesCategory
    }

    val totalPages = (filteredProducts.size / itemsPerPage) +
            if (filteredProducts.size % itemsPerPage == 0) 0 else 1

    val paginatedProducts =
        filteredProducts.drop((currentPage - 1) * itemsPerPage)
            .take(itemsPerPage)

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

            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FAFF),
                                Color(0xFFEAF2FF)
                            )
                        )
                    )
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2962FF),
                                contentColor = Color.White
                            ),
                            onClick = {
                                scope.launch {
                                    try {
                                        downloading = true
                                        downloadMessage = "Preparing Products PDF..."

                                        val body = api.exportProductsPdf()
                                        val file = savePdfFile(context, body, "products.pdf")

                                        downloading = false
                                        openPdf(context, file)

                                    } catch (e: Exception) {
                                        downloading = false
                                        snackbar.showSnackbar("Download Failed")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(6.dp))
                            Text("")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            onClick = {
                                scope.launch {
                                    try {
                                        downloading = true
                                        downloadMessage = "Preparing Barcode PDF..."

                                        val body = api.exportBarcodePdf()
                                        val file = savePdfFile(context, body, "barcodes.pdf")

                                        downloading = false
                                        openPdf(context, file)

                                    } catch (e: Exception) {
                                        downloading = false
                                        snackbar.showSnackbar("Download Failed")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.QrCode, null)
                            Spacer(Modifier.width(6.dp))
                            Text("")
                        }


                            IconButton(
                                onClick = {
                                    scope.launch {
                                        refreshing = true
                                        loadData()
                                        snackbar.showSnackbar("Updated")
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp) // 👈 small size

                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color(0xFF1565C0)
                                )
                            }

                    }
                }
                // 🔍 SEARCH BAR
                Card(
                    shape = RoundedCornerShape(50),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search product...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }



                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = showLowStockOnly,
                            onClick = { showLowStockOnly = !showLowStockOnly },
                            label = { Text("Low Stock") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFC107), // Yellow when selected
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                    // All Button
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1565C0),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Dynamic Category Buttons
                    items(categories) { category ->

                        val isSelected = selectedCategoryFilter == category.id

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter =
                                    if (isSelected) null else category.id
                            },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1565C0),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                LazyColumn {

                    items(
                        paginatedProducts,
                        key = { it.id }
                    ) { product ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .animateContentSize(),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                // ================= MAIN CONTENT =================

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {

                                    AsyncImage(
                                        model = product.image_url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFF4F6FA))
                                    )

                                    Spacer(Modifier.width(18.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            product.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D47A1)
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {

                                            Text(
                                                "₹${product.sell_price}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0)
                                            )

                                            Spacer(Modifier.width(8.dp))

                                            if ((product.mrp ?: 0.0) > (product.sell_price ?: 0.0)) {
                                                Text(
                                                    "₹${product.mrp}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // ===== PROFIT (UNCHANGED LOGIC) =====
                                        val base = product.base_price ?: 0.0
                                        val sell = product.sell_price ?: 0.0
                                        val perUnitProfit = sell - base

                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFE8F5E9)
                                            )
                                        ) {
                                            Text(
                                                "Profit ₹${"%.0f".format(perUnitProfit)}",
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        Text(
                                            "Stock: ${product.stock_qty} ${product.unit}",
                                            fontWeight = FontWeight.Medium,
                                            color = when {
                                                product.stock_qty > 10 -> Color(0xFF2E7D32)
                                                product.stock_qty > 5 -> Color(0xFFF57C00)
                                                else -> Color(0xFFD32F2F)
                                            }
                                        )

                                        if (product.stock_qty <= 5) {
                                            Spacer(Modifier.height(6.dp))
                                            AssistChip(
                                                onClick = {},
                                                label = { Text("Low Stock") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = Color(0xFFFFEBEE),
                                                    labelColor = Color(0xFFD32F2F)
                                                )
                                            )
                                        }
                                    }

                                    Column {

                                        IconButton(onClick = {
                                            editProduct = product
                                            showForm = true
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF1565C0)
                                            )
                                        }

                                        IconButton(onClick = {
                                            deleteProduct = product
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFD32F2F)
                                            )
                                        }
                                    }
                                }

                                // ================= DISCOUNT BADGE (TRUE OVERLAY FIX) =================

                                if ((product.discount_percent ?: 0.0) > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .offset(x = 8.dp, y = 8.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF00C853), Color(0xFF2E7D32))
                                                ),
                                                shape = RoundedCornerShape(
                                                    topStart = 26.dp,
                                                    bottomEnd = 18.dp
                                                )
                                            )
                                    ) {
                                        Text(
                                            "${product.discount_percent?.roundToInt()}% OFF",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (totalPages > 1) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Button(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1
                        ) { Text("Prev") }

                        Text("Page $currentPage / $totalPages")

                        Button(
                            onClick = { if (currentPage < totalPages) currentPage++ },
                            enabled = currentPage < totalPages
                        ) { Text("Next") }
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

        var expanded by remember { mutableStateOf(false) }
        val unitOptions = listOf("kg","g","ltr","ml","pcs","pkt","box","dozen")

        var uploading by remember { mutableStateOf(false) }
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var selectedCategoryId by remember {
            mutableStateOf(editProduct?.category_id)
        }
        var showCategoryModal by remember { mutableStateOf(false) }
        var newCategoryName by remember { mutableStateOf("") }
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
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = Color.White
            ) {

                Box {

                    // 🔴 Close Button (Top Right)
                    IconButton(
                        onClick = { if (!uploading) showForm = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 36.dp)   // close button space
                            .padding(20.dp)
                    ) {

                        // 👉 তোমার সব item { } এখানেই থাকবে

                    item {
                        Text(
                            if (editProduct == null) "Add Product" else "Update Product",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                    item {

                        var categoryExpanded by remember { mutableStateOf(false) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded },
                                modifier = Modifier.weight(1f)
                            ) {

                                OutlinedTextField(
                                    value = categories
                                        .find { it.id == selectedCategoryId }
                                        ?.name ?: "-- None --",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {

                                    DropdownMenuItem(
                                        text = { Text("-- Select --") },
                                        onClick = {
                                            selectedCategoryId = null
                                            categoryExpanded = false
                                        }
                                    )

                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name) },
                                            onClick = {
                                                selectedCategoryId = cat.id
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = { showCategoryModal = true }
                            ) {
                                Text("+ New")
                            }
                        }
                    }



                    item {
                        OutlinedTextField(title, { title = it }, label = { Text("Title") })
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        OutlinedTextField(basePrice, { basePrice = it },
                            label = { Text("Base Price (₹ per $unit)") })
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        OutlinedTextField(mrp, { mrp = it },
                            label = { Text("MRP (₹ per $unit)") })
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        OutlinedTextField(sellPrice, { sellPrice = it },
                            label = { Text("Sell Price (₹ per $unit)") })
                    }

                    item {
                        Text(
                            "Discount: ${discount.roundToInt()}%",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        OutlinedTextField(stock, { stock = it },
                            label = { Text("Stock ($unit)") })
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    // 🔥 UNIT DROPDOWN (ADDED ONLY THIS PART)

                    item {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = unit,   // ✅ এখানে unit হবে
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                unitOptions.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            unit = it    // ✅ এখানে unit set হবে
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        OutlinedTextField(description, { description = it },
                            label = { Text("Description") })
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

                    item { Spacer(Modifier.height(8.dp)) }

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
                                            selectedCategoryId
                                                ?.toString()
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
                                            selectedCategoryId
                                                ?.toString()
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

                                    uploading = false
                                    showForm = false
                                    loadData()
                                    snackbar.showSnackbar("Saved Successfully")
                                }
                            }
                        ) {
                            Text(if (uploading) "Uploading..." else "Save")
                        }
                    }

                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
        if (showCategoryModal) {

            var newCategoryName by remember { mutableStateOf("") }
            var saving by remember { mutableStateOf(false) }

            Dialog(onDismissRequest = { if (!saving) showCategoryModal = false }) {

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        // 🔹 Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                "Add Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    if (!saving) showCategoryModal = false
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                scope.launch {

                                    if (newCategoryName.isNotBlank()) {

                                        saving = true

                                        val response = api.addCategory(
                                            newCategoryName.toRequestBody(
                                                "text/plain".toMediaTypeOrNull()
                                            )
                                        )

                                        if (response.success) {

                                            // 🔥 refresh categories
                                            val c = api.getCategories()
                                            if (c.success) {
                                                categories = c.data ?: emptyList()
                                            }

                                            selectedCategoryId = response.data?.id

                                            showCategoryModal = false
                                            snackbar.showSnackbar("Category Added")
                                        }

                                        saving = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (saving) "Saving..." else "Save")
                        }
                    }
                }
            }
        }

}
    if (downloading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        downloadMessage,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

}



private fun savePdfFile(
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

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_LONG).show()
    }
}