                                package com.jminnovatech.joymart.ui.distributor

                                import androidx.activity.compose.BackHandler
                                import androidx.compose.foundation.layout.padding
                                import androidx.compose.material.icons.Icons
                                import androidx.compose.material.icons.filled.Menu
                                import androidx.compose.material.icons.filled.Refresh
                                import androidx.compose.material3.*
                                import androidx.compose.runtime.*
                                import androidx.compose.ui.Modifier
                                import androidx.compose.ui.platform.LocalContext
                                import androidx.navigation.compose.*
                                import com.jminnovatech.joymart.core.session.SessionManager
                                import com.jminnovatech.joymart.ui.distributor.modal.ProductModal
                                import kotlinx.coroutines.launch

                                @OptIn(ExperimentalMaterial3Api::class)
                                @Composable
                                fun DistributorRoot(
                                    onLogout: () -> Unit
                                ) {
                                    var showProductModal by remember { mutableStateOf(false) }
                                    val navController = rememberNavController()
                                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                                    val scope = rememberCoroutineScope()

                                    val context = LocalContext.current
                                    val session = remember { SessionManager(context) }
                                    val userName = session.getUserName() ?: "Distributor"
                                    var refreshTrigger by remember { mutableStateOf(0) }

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

                                                    when (route) {

                                                        "barcode_pdf",
                                                        "export_pdf",
                                                        "export_csv" -> {
                                                            showProductModal = true
                                                        }

                                                        else -> {
                                                            navController.navigate(route) {
                                                                launchSingleTop = true
                                                                restoreState = true
                                                            }
                                                        }
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
                                                    title = { Text("Distributor") },
                                                    navigationIcon = {
                                                        IconButton(
                                                            onClick = { scope.launch { drawerState.open() } }
                                                        ) {
                                                            Icon(Icons.Default.Menu, contentDescription = null)
                                                        }
                                                    },
                                                    actions = {
                                                        IconButton(
                                                            onClick = {
                                                                refreshTrigger++   // 🔥 trigger reload
                                                            }
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Refresh,
                                                                contentDescription = "Refresh"
                                                            )
                                                        }
                                                    }
                                                )
                                            }

                                        )
                                        { padding ->

                                            NavHost(
                                                navController = navController,
                                                startDestination = "dashboard",
                                                modifier = Modifier.padding(padding)
                                            ) {

                                                composable("dashboard") {
                                                    DistributorDashboardScreen()
                                                }

                                                composable("products") {
                                                    DistributorProductScreen(refreshTrigger)
                                                }

                                                composable("orders") {
                                                    DistributorOrdersScreen()
                                                }

                                                composable("profile") {
                                                    DistributorProfileScreen()
                                                }
                                            }
                                        }
                                        if (showProductModal) {
                                            ProductModal {
                                                showProductModal = false
                                            }
                                        }
                                    }
                                }
