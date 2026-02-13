package com.jminnovatech.joymart.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jminnovatech.joymart.core.session.SessionManager
import com.jminnovatech.joymart.data.model.auth.UserRole
import com.jminnovatech.joymart.ui.customer.CustomerRoot
import com.jminnovatech.joymart.ui.distributor.DistributorHome
import com.jminnovatech.joymart.ui.distributor.DistributorRoot

@Composable
fun HomeRouter(
    role: UserRole,
    navController: NavController,
    sessionManager: SessionManager
) {
    when (role) {

        UserRole.CUSTOMER -> {
            CustomerRoot(
                onLogout = {
                    sessionManager.clear()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }


        UserRole.DISTRIBUTOR -> {
            DistributorRoot(
                onLogout = {
                    sessionManager.clear()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }


        UserRole.COMPANY -> {
            CompanyHome(

            )
        }

        UserRole.EXECUTIVE -> {
            ExecutiveHome(

            )
        }

        else -> {
            GuestUserHome()
        }
    }
}
