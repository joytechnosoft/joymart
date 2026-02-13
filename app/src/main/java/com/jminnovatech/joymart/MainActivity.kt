package com.jminnovatech.joymart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jminnovatech.joymart.core.ui.theme.JoyMartTheme

import com.jminnovatech.joymart.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JoyMartTheme {
                AppNavGraph()
            }
        }
    }
}
