package com.jminnovatech.joymart.ui.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch
import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import java.util.Locale
import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorProfileScreen() {

    val api = RetrofitClient.distributorApi

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isEditMode by remember {
        mutableStateOf(false)
    }

    var locationName by remember {
        mutableStateOf("")
    }

    var fetchingLocation by remember {
        mutableStateOf(false)
    }
    var pendingLocationFetch by remember {
        mutableStateOf(false)
    }
    val fusedLocationClient =
        remember {
            LocationServices
                .getFusedLocationProviderClient(context)
        }


    var loading by remember { mutableStateOf(true) }

    var saving by remember { mutableStateOf(false) }

    var successMessage by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    // ================= DATA =================

    var name by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }

    var address by remember { mutableStateOf("") }

    var role by remember { mutableStateOf("") }

    var upiId by remember { mutableStateOf("") }

    var consignorName by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf("") }

    var longitude by remember { mutableStateOf("") }

    var walletBalance by remember { mutableStateOf("0") }

    var shopLogo by remember { mutableStateOf("") }

    var packageActive by remember {
        mutableStateOf(0)
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                fetchingLocation = true

                fetchCurrentLocation(

                    context = context,

                    fusedLocationClient = fusedLocationClient,

                    onLocationFetched = { lat, lng, address ->

                        latitude = lat

                        longitude = lng

                        locationName = address

                        fetchingLocation = false
                    },

                    onError = {

                        errorMessage = it

                        fetchingLocation = false
                    }
                )

            } else {

                errorMessage =
                    "Location permission denied"
            }
        }

    suspend fun loadProfile() {

        loading = true

        try {

            val res = api.getDistributorProfile()

            if (res.success) {

                val d = res.data

                name = d.name

                email = d.email

                phone = d.phone

                address = d.address

                role = d.role

                upiId = d.upi_id ?: ""

                consignorName = d.consignor_name ?: ""

                latitude = d.latitude?.toString() ?: ""

                longitude = d.longitude?.toString() ?: ""

                walletBalance = d.wallet_balance.toString()

                shopLogo = d.shop_logo ?: ""

                packageActive = d.package_active
            }

        } catch (e: Exception) {

            errorMessage = "Failed to load profile"
        }

        loading = false
    }

    LaunchedEffect(Unit) {

        loadProfile()
    }
    LaunchedEffect(pendingLocationFetch) {

        if (pendingLocationFetch) {

            pendingLocationFetch = false

            locationPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF5F7FA),
                        Color.White
                    )
                )
            )
    ) {

        if (loading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }

        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                // ================= TOP CARD =================

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1565C0)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AsyncImage(
                            model = if (shopLogo.isNotEmpty()) shopLogo else null,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            role.uppercase(),
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {

                            Text(
                                "Wallet ₹$walletBalance",
                                modifier = Modifier.padding(
                                    horizontal = 18.dp,
                                    vertical = 8.dp
                                ),
                                color = Color(0xFF1565C0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ================= PACKAGE =================

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            if (packageActive == 1)
                                Color(0xFFE8F5E9)
                            else
                                Color(0xFFFFEBEE)
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            if (packageActive == 1)
                                Icons.Default.Verified
                            else
                                Icons.Default.Warning,

                            contentDescription = null,

                            tint =
                                if (packageActive == 1)
                                    Color(0xFF2E7D32)
                                else
                                    Color.Red
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            if (packageActive == 1)
                                "Package Active"
                            else
                                "Package Inactive",

                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ================= FORM =================

                PremiumField(

                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = email,
                    onValueChange = {},
                    label = "Email",
                    icon = Icons.Default.Email,
                    readOnly = true,

                )

                PremiumField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    icon = Icons.Default.Phone,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    icon = Icons.Default.LocationOn,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = consignorName,
                    onValueChange = { consignorName = it },
                    label = "Consignor Name",
                    icon = Icons.Default.Store,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = "UPI ID",
                    icon = Icons.Default.Payments,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = "Latitude",
                    icon = Icons.Default.MyLocation,
                    enabled = isEditMode,
                )

                PremiumField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = "Longitude",
                    icon = Icons.Default.LocationSearching,
                    enabled = isEditMode,
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),

                    shape = RoundedCornerShape(20.dp),

                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            "Current Location",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        if (locationName.isNotEmpty()) {

                            Text(
                                locationName,
                                color = Color.Gray
                            )

                            Spacer(Modifier.height(10.dp))
                        }

                        Button(

                            onClick = {

                                fetchingLocation = true

                                val activity =
                                    context as Activity

                                checkLocationSettings(

                                    activity = activity,

                                    onEnabled = {

                                        pendingLocationFetch = true
                                    },

                                    onFailed = {

                                        fetchingLocation = false

                                        errorMessage = it
                                    }
                                )
                            }
                        ){

                            if (fetchingLocation) {

                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )

                            } else {

                                Icon(
                                    Icons.Default.MyLocation,
                                    null
                                )

                                Spacer(Modifier.width(8.dp))

                                Text("Get Current Location")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))

                Spacer(Modifier.height(10.dp))

                Button(

                    onClick = {

                        isEditMode = !isEditMode
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(

                        containerColor =
                            if (isEditMode)
                                Color(0xFFD32F2F)
                            else
                                Color(0xFF2E7D32)
                    )
                ) {

                    Icon(
                        if (isEditMode)
                            Icons.Default.Close
                        else
                            Icons.Default.Edit,
                        null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(

                        if (isEditMode)
                            "Cancel Edit"
                        else
                            "Enable Edit",

                        fontWeight = FontWeight.Bold
                    )
                }
                // ================= SAVE =================

                Button(

                    onClick = {

                        scope.launch {

                            saving = true

                            try {

                                val res =
                                    api.updateDistributorProfile(

                                        name = name,

                                        phone = phone,

                                        address = address,

                                        upiId = upiId,

                                        consignorName = consignorName,

                                        latitude = latitude,

                                        longitude = longitude,

                                        upiQrUrl = ""
                                    )

                                if (res.success) {

                                    successMessage =
                                        "Profile Updated Successfully"

                                } else {

                                    errorMessage =
                                        "Update Failed"
                                }

                            } catch (e: Exception) {

                                errorMessage =
                                    "Something went wrong"
                            }

                            saving = false
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = isEditMode && !saving,

                    shape = RoundedCornerShape(18.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0)
                    )
                ) {

                    if (saving) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Icon(Icons.Default.Save, null)

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Update Profile",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        // ================= SUCCESS =================

        if (successMessage.isNotEmpty()) {

            AlertDialog(

                onDismissRequest = {
                    successMessage = ""
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            successMessage = ""
                        }
                    ) {
                        Text("OK")
                    }
                },

                title = {
                    Text("Success")
                },

                text = {
                    Text(successMessage)
                }
            )
        }

        // ================= ERROR =================

        if (errorMessage.isNotEmpty()) {

            AlertDialog(

                onDismissRequest = {
                    errorMessage = ""
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            errorMessage = ""
                        }
                    ) {
                        Text("OK")
                    }
                },

                title = {
                    Text("Error")
                },

                text = {
                    Text(errorMessage)
                }
            )
        }
    }
}

@Composable
fun PremiumField(

    value: String,

    onValueChange: (String) -> Unit,

    label: String,

    icon: androidx.compose.ui.graphics.vector.ImageVector,

    readOnly: Boolean = false,

    enabled: Boolean = true
) {

    Column {

        OutlinedTextField(

            value = value,

            onValueChange = onValueChange,

            readOnly = readOnly,

            enabled = enabled,

            label = {
                Text(label)
            },

            leadingIcon = {
                Icon(icon, null)
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(18.dp),

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF1565C0),

                unfocusedBorderColor =
                    Color.LightGray,

                disabledBorderColor =
                    Color(0xFFE0E0E0),

                disabledTextColor =
                    Color.Black
            )
        )

        Spacer(Modifier.height(14.dp))
    }
}
@SuppressLint("MissingPermission")
fun fetchCurrentLocation(

    context: android.content.Context,

    fusedLocationClient: FusedLocationProviderClient,

    onLocationFetched: (
        String,
        String,
        String
    ) -> Unit,

    onError: (String) -> Unit
) {

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->

            if (location != null) {

                val lat =
                    location.latitude.toString()

                val lng =
                    location.longitude.toString()

                try {

                    val geocoder =
                        Geocoder(
                            context,
                            Locale.getDefault()
                        )

                    val addresses =
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )

                    val address =
                        addresses?.firstOrNull()
                            ?.getAddressLine(0)
                            ?: "Unknown location"

                    onLocationFetched(
                        lat,
                        lng,
                        address
                    )

                } catch (e: Exception) {

                    onLocationFetched(
                        lat,
                        lng,
                        "Location found"
                    )
                }

            } else {

                onError(
                    "Unable to fetch location"
                )
            }
        }
}
fun checkLocationSettings(

    activity: Activity,

    onEnabled: () -> Unit,

    onFailed: (String) -> Unit
) {

    val locationRequest =
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

    val builder =
        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

    val client =
        LocationServices.getSettingsClient(activity)

    val task =
        client.checkLocationSettings(
            builder.build()
        )

    task.addOnSuccessListener {

        onEnabled()
    }

    task.addOnFailureListener { exception ->

        if (
            exception is ResolvableApiException
        ) {

            try {

                exception.startResolutionForResult(
                    activity,
                    5001
                )

            } catch (e: IntentSender.SendIntentException) {

                onFailed(
                    "Unable to open location settings"
                )
            }

        } else {

            onFailed(
                "Location service disabled"
            )
        }
    }
}