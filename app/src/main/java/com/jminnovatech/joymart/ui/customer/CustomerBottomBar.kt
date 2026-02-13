package com.jminnovatech.joymart.ui.customer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

import androidx.navigation.NavHostController

@Composable
fun CustomerBottomBar(nav: NavController, cartCount: Int) {

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = { nav.navigate("home") },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { nav.navigate("orders") },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Orders") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { nav.navigate("cart") },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge { Text(cartCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, null)
                }
            },
            label = { Text("Cart") }
        )
    }
}
