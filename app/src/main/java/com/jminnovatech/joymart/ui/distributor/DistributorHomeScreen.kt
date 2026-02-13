package com.jminnovatech.joymart.ui.distributor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.jminnovatech.joymart.core.session.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributorHomeScreen(
    onLogout: () -> Unit
) {

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val userName = session.getUserName() ?: "Distributor"

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DistributorDrawer(
                currentRoute = currentRoute,
                userName = userName,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },
                onLogout = onLogout
            )
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Distributor Panel") },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->

            NavHost(
                navController = navController,
                startDestination = "products",
                modifier = Modifier.padding(padding)
            ) {

                composable("products") {
                    DistributorProductScreen()
                }

                composable("orders") {
                    DistributorOrdersScreen()
                }

                composable("profile") {
                    DistributorProfileScreen()
                }
            }
        }
    }
}
