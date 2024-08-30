package org.kotlinc0der.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

expect fun getImageBitmap(colors: List<List<Color>>) : ImageBitmap

expect fun saveImageBitmap(imageBitmap: ImageBitmap, filename: String)