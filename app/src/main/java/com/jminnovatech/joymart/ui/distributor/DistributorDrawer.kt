package com.jminnovatech.joymart.ui.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jminnovatech.joymart.ui.customer.drawer.DrawerItem

@Composable
fun DistributorDrawer(
    currentRoute: String?,
    userName: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {

        Text("👋 Hello $userName", fontWeight = FontWeight.Bold)
        Text("joymart Distributor", color = Color.Gray)

        Spacer(Modifier.height(24.dp))
        Divider()

        DrawerItem(
            icon = "📦",
            title = "Products",
            selected = currentRoute == "products",
            onClick = { onNavigate("products") }
        )

        DrawerItem(
            icon = "🧾",
            title = "Orders",
            selected = currentRoute == "orders",
            onClick = { onNavigate("orders") }
        )

        DrawerItem(
            icon = "👤",
            title = "Profile",
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") }
        )

        Spacer(Modifier.weight(1f))
        Divider()

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = Color.Red)
        }
    }
}
