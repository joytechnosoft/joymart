package com.jminnovatech.joymart.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.jminnovatech.joymart.ui.theme.Background
import com.jminnovatech.joymart.ui.theme.Primary

@Composable
fun JoyMartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary,
            background = Background
        ),
        content = content
    )
}
