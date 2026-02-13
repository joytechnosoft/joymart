package com.jminnovatech.joymart.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.remote.api.RetrofitClient
import com.jminnovatech.joymart.data.model.auth.LoginRequest



import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val session = SessionManager(context)
    val scope = rememberCoroutineScope()

    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            onClick = {
                loading = true
                error = null

                scope.launch {
                    try {
                        val res = RetrofitClient.authApi.login(
                            LoginRequest(
                                login = mobile,
                                password = password
                            )
                        )

                        if (res.success == true) {
                            session.saveSession(
                                userId = res.user!!.id,
                                token = res.token!!,
                                role = res.role!!
                            )

                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        } else {
                            error = res.message ?: "Invalid credentials"
                        }

                    } catch (e: Exception) {
                        error = "Wrong mobile number or password"
                    } finally {
                        loading = false
                    }
                }
            }
        ) {
            Text(if (loading) "Please wait..." else "Login")
        }
    }
}

