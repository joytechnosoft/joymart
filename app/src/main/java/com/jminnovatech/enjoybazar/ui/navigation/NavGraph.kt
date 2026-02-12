package com.jminnovatech.joymart.ui.navigation

import OrderSuccessScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.model.auth.UserRole
import com.jminnovatech.joymart.ui.auth.LoginScreen

import com.jminnovatech.joymart.ui.home.HomeRouter
import com.jminnovatech.joymart.ui.splash.SplashScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.HOME) {

            var role by remember { mutableStateOf<UserRole?>(null) }

            LaunchedEffect(Unit) {
                val roleString = sessionManager.getRole()
                role = roleString?.let { UserRole.valueOf(it.uppercase()) }
            }

            role?.let { safeRole ->
                HomeRouter(
                    role = safeRole,
                    navController = navController,
                    sessionManager = sessionManager
                )
            }
        }

        composable("success") {
            OrderSuccessScreen {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            }
        }
    }
}
