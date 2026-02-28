package com.jminnovatech.joymart.ui.distributor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorProductScreen() {

    val api = RetrofitClient.distributorApi
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var products by remember { mutableStateOf<List<DistributorProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<DistributorCategory>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUnitFilter by remember { mutableStateOf<String?>(null) }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var showForm by remember { mutableStateOf(false) }
    var editProduct by remember { mutableStateOf<DistributorProduct?>(null) }
    var deleteProduct by remember { mutableStateOf<DistributorProduct?>(null) }
// 🔍 FILTER + PAGINATION
    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 10
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

        matchesSearch && matchesUnit && matchesLowStock
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

            Column(Modifier.padding(padding)) {

                // 🔍 SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    placeholder = { Text("Search product...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp)
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = showLowStockOnly,
                        onClick = { showLowStockOnly = !showLowStockOnly },
                        label = { Text("Low Stock") }
                    )
                }

                LazyColumn {

                    items(
                        paginatedProducts,
                        key = { it.id }
                    ) { product ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .animateContentSize(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {

                            Box {

                                // 🔥 Discount Ribbon
                                if ((product.discount_percent ?: 0.0) > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF00C853),
                                                shape = RoundedCornerShape(
                                                    topStart = 20.dp,
                                                    bottomEnd = 20.dp
                                                )
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${product.discount_percent?.roundToInt()}% OFF",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {

                                    // 🖼 Image
                                    AsyncImage(
                                        model = product.image_url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(95.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(
                                                1.dp,
                                                Color.LightGray,
                                                RoundedCornerShape(16.dp)
                                            )
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Column(
                                        Modifier.weight(1f)
                                    ) {

                                        // 🏷 Title
                                        Text(
                                            product.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        // 💰 Price Row
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

                                        Spacer(Modifier.height(6.dp))
// 💰 PER PRODUCT PROFIT (per unit only)

                                        val base = product.base_price ?: 0.0
                                        val sell = product.sell_price ?: 0.0
                                        val perUnitProfit = sell - base

                                        Spacer(Modifier.height(4.dp))

                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text("Profit ₹${"%.0f".format(perUnitProfit)}")
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = Color(0xFFE8F5E9),
                                                labelColor = Color(0xFF2E7D32)
                                            )
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        // 📦 Stock + Unit
                                        Text(
                                            "Stock: ${product.stock_qty} ${product.unit}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        // ⚠ Low stock chip
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

                                    // ✏️ Edit/Delete
                                    Column {

                                        IconButton(onClick = {
                                            editProduct = product
                                            showForm = true
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF1976D2)
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

            Surface(shape = RoundedCornerShape(20.dp)) {

                LazyColumn(Modifier.padding(20.dp)) {

                    item {
                        Text(
                            if (editProduct == null) "Add Product" else "Update Product",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                    item {

                        var categoryExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
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
                                    text = { Text("-- None --") },
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
                    }
                    item {

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F7FA)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {

                            Column(Modifier.padding(16.dp)) {

                                Text(
                                    "Add New Category",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1565C0)
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    OutlinedTextField(
                                        value = newCategoryName,
                                        onValueChange = { newCategoryName = it },
                                        placeholder = { Text("Enter category name") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ElevatedButton(
                                        onClick = {
                                            scope.launch {
                                                if (newCategoryName.isNotBlank()) {

                                                    val response = api.addCategory(
                                                        newCategoryName
                                                            .toRequestBody("text/plain".toMediaTypeOrNull())
                                                    )

                                                    if (response.success) {
                                                        loadData()
                                                        selectedCategoryId = response.data?.id
                                                        newCategoryName = ""
                                                        snackbar.showSnackbar("Category Added")
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add")
                                    }
                                }
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
}