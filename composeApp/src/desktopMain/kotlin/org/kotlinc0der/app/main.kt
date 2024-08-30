package org.kotlinc0der.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Terrain Generator",
    ) {
        window.minimumSize = Dimension(1200, 800)
        App()
    }
}