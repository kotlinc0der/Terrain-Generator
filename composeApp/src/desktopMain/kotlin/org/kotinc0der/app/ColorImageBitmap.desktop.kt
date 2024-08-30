package org.kotinc0der.app

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
    colors.forEachIndexed { x, row ->
        row.forEachIndexed { y, color ->
            with(color.convert(ColorSpaces.Srgb).value) {
                repeat(bytesPerPixel) {
                    bytes[x * width * bytesPerPixel + y * bytesPerPixel + it] =
                        shr(32 + it * 8).toByte()
                }
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
            // Handle exceptions (e.g., show an error message)
        }
    }
}