package com.jminnovatech.joymart.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.remote.api.RetrofitClient

import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        if (session.isLoggedIn()) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}



