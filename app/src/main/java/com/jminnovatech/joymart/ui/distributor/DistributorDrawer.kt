package com.jminnovatech.joymart.ui.distributor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {

        Text(
            text = "JoyMart",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))
        Text("MENU", color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // Dashboard
        DrawerItem(
            icon = Icons.Default.Dashboard,
            title = "Dashboard",
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )

        // Products
        ExpandableDrawerItem(
            icon = Icons.Default.Inventory2,
            title = "Products",
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            children = listOf(
                Triple("All Products", "products", Icons.Default.List),
                Triple("Barcode PDF", "barcode_pdf", Icons.Default.QrCode),
                Triple("Export CSV", "export_csv", Icons.Default.TableChart),
                Triple("Export PDF", "export_pdf", Icons.Default.PictureAsPdf)
            )
        )

        // Billing
        ExpandableDrawerItem(
            icon = Icons.Default.Receipt,
            title = "Billing",
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            children = listOf(
                Triple("Create Bill (POS)", "create_bill", Icons.Default.PointOfSale),
                Triple("Bill Payments", "bill_payments", Icons.Default.Payments)
            )
        )

        // Orders
        ExpandableDrawerItem(
            icon = Icons.Default.ShoppingCart,
            title = "Orders",
            badgeCount = 3,
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            children = listOf(
                Triple("Order Status", "orders", Icons.Default.Info),
                Triple("Customer Orders", "customer_orders", Icons.Default.People)
            )
        )

        // Payment Verification
        DrawerItem(
            icon = Icons.Default.VerifiedUser,
            title = "Payment Verification",
            selected = currentRoute == "payment_verification",
            badgeCount = 1,
            onClick = { onNavigate("payment_verification") }
        )

        // Reports
        ExpandableDrawerItem(
            icon = Icons.Default.BarChart,
            title = "Reports",
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            children = listOf(
                Triple("Payment Report", "payment_report", Icons.Default.Payments),
                Triple("Sales Report", "sales_report", Icons.Default.ShowChart),
                Triple("Product Sales Report", "product_sales_report", Icons.Default.TrendingUp)
            )
        )

        // Wallet
        DrawerItem(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Wallets",
            selected = currentRoute == "wallets",
            onClick = { onNavigate("wallets") }
        )

        Spacer(Modifier.weight(1f))
        Divider()

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout", color = MaterialTheme.colorScheme.error)
        }
    }
}


/* -------------------------------------------------------
   DrawerItem Component (Professional Version)
-------------------------------------------------------- */
@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    selected: Boolean,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title)

                if (badgeCount != null && badgeCount > 0) {
                    Badge { Text(badgeCount.toString()) }
                }
            }
        },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = title) }
    )
}

@Composable
fun ExpandableDrawerItem(
    icon: ImageVector,
    title: String,
    badgeCount: Int? = null,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    children: List<Triple<String, String, ImageVector>>
) {

    var expanded by remember { mutableStateOf(false) }

    Column {

        NavigationDrawerItem(
            label = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row {
                        Text(title)

                        if (badgeCount != null && badgeCount > 0) {
                            Spacer(Modifier.width(6.dp))
                            Badge { Text(badgeCount.toString()) }
                        }
                    }

                    Icon(
                        if (expanded) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            },
            selected = false,
            onClick = { expanded = !expanded },
            icon = { Icon(icon, contentDescription = title) }
        )

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 36.dp)) {

                children.forEach { (childTitle, route, childIcon) ->

                    NavigationDrawerItem(
                        label = { Text(childTitle) },
                        selected = currentRoute == route,
                        onClick = { onNavigate(route) },
                        icon = {
                            Icon(
                                childIcon,
                                contentDescription = childTitle,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}