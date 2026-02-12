package com.jminnovatech.joymart.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.ui.customer.drawer.CustomerDrawer
import com.jminnovatech.joymart.ui.customer.vm.CustomerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRoot(
    onLogout: () -> Unit
) {

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val userName = session.getUserName() ?: "Customer"

    val vm: CustomerViewModel = viewModel()

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomerDrawer(
                currentRoute = currentRoute,
                userName = userName,

                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },

                onLogout = {
                    onLogout() // 🔥 parent কে জানাও
                }


            )

        }
    )
    {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("joymart") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                CustomerBottomBar(navController, vm.cart.collectAsState().value.size)
            }
        ) { padding ->

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {

                composable("home") { CustomerHomeScreen(vm) }
                composable("orders") { CustomerOrdersScreen(vm) }
                composable("cart") { CustomerCart(vm) }
                composable("profile") { ProfileScreen(vm) }

            }
        }
    }
}
