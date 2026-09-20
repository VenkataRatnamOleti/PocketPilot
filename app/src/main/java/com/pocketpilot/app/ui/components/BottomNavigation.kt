package com.pocketpilot.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class PocketPilotScreen {
    HOME,
    ADD_EXPENSE,
    SCAN,
    ASK
}

@Composable
fun PocketPilotNavigationBar(
    currentScreen: PocketPilotScreen,
    onScreenSelected: (PocketPilotScreen) -> Unit
) {

    NavigationBar {

        NavigationBarItem(
            selected = currentScreen == PocketPilotScreen.HOME,
            onClick = { onScreenSelected(PocketPilotScreen.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentScreen == PocketPilotScreen.SCAN,
            onClick = { onScreenSelected(PocketPilotScreen.SCAN) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan"
                )
            },
            label = { Text("Scan") }
        )

        NavigationBarItem(
            selected = currentScreen == PocketPilotScreen.ASK,
            onClick = { onScreenSelected(PocketPilotScreen.ASK) },
            icon = {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = "Ask"
                )
            },
            label = { Text("Ask AI") }
        )
    }
}
