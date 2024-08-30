package org.kotlinc0der.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

actual fun getImageBitmap(colors: List<List<Color>>): ImageBitmap {
    val bytesPerPixel = 4 // Alpha, Red, Green, Blue

    val width = colors.firstOrNull()?.size ?: 0
    val height = colors.size

    val bytes = ByteArray(width * height * bytesPerPixel)

    // Color value is represented by ULong (8 bytes) wheres the pixel is represented by 4 bytes
    // so offset is 4 bytes * 8 = 32 bits
    val bitOffset = 32

    colors.flatten().forEachIndexed { colorIndex: Int, color: Color ->
        with(color.convert(ColorSpaces.Srgb).value) {
            // Extract the color components into separate bytes
            // 4 bytes per pixel (Alpha, Red, Green, Blue)
            // at byteIndex = 0; shift by offset (padded 0s) and collect Alpha using .toByte()
            // at byteIndex = 1; shift by Offset + 8 bits and collect Red byte
            // ...
            repeat(bytesPerPixel) { byteIndex ->
                bytes[bytesPerPixel * colorIndex + byteIndex] =
                    shr(bitOffset + byteIndex * 8).toByte()
            }
        }
    }

    val image: Image = Image.makeRaster(
        imageInfo = ImageInfo.makeN32Premul(width, height),
        bytes = bytes,
        rowBytes = width * 4,
    )

    return image.toComposeImageBitmap()
}

actual fun saveImageBitmap(imageBitmap: ImageBitmap, filename: String) {
    val bufferedImage: BufferedImage = imageBitmap.toAwtImage()

    val fileChooser = java.awt.FileDialog(null as java.awt.Frame?, "Save Image", java.awt.FileDialog.SAVE)
    fileChooser.file = filename
    fileChooser.isVisible = true

    val selectedFile = if (fileChooser.file != null) {
        File(fileChooser.directory, fileChooser.file)
    } else {
        null // User canceled the file selection
    }

    selectedFile?.let { file ->
        try {
            ImageIO.write(bufferedImage, "png", file)
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}