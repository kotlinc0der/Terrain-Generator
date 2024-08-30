package org.kotinc0der.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreateCopyWithColorSpace
import platform.CoreGraphics.CGImageGetAlphaInfo
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.UIKit.UIImage

fun ByteArray.toImageBitmap(): ImageBitmap =
    Image.makeFromEncoded(this).toComposeImageBitmap()


actual fun getImageBitmap(colors: List<List<Color>>): ImageBitmap {
    let width = colors[0].count
    let height = colors.count

    let colorSpace = CGColorSpaceCreateDeviceRGB()
    let bitmapInfo = CGBitmapInfo(rawValue: CGImageAlphaInfo.premultipliedLast.rawValue)
    let bitsPerComponent = 8
    let bytesPerRow = width * 4

    guard let context = CGContext(
        data: nil,
        width: width,
        height: height,
        bitsPerComponent: bitsPerComponent,
        bytesPerRow: bytesPerRow,
        space: colorSpace,
        bitmapInfo: bitmapInfo.rawValue
    ) else {
        fatalError("Failed to create CGContext")
    }

    for y in 0..<height {
        for x in 0..<width {
            let color = colors[y][x]
            let cgColor = UIColor (
                red: CGFloat(color.red),
                green: CGFloat(color.green),
                blue: CGFloat(color.blue),
                alpha: CGFloat(color.alpha)
            ).cgColor

            context.setFillColor(cgColor)
            context.fill(CGRect(x: x, y: y, width: 1, height: 1))
        }
    }

    guard let cgImage = context.makeImage() else {
        fatalError("Failed to create CGImage")
    }

    let uiImage = UIImage (cgImage: cgImage)
    return uiImage.toSkiaImage().toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toSkiaImage(): Image {
    val imageRef = CGImageCreateCopyWithColorSpace(
        this.CGImage,
        CGColorSpaceCreateDeviceRGB()
    )

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)

    val alphaType = when (CGImageGetAlphaInfo(imageRef)) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst, CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> ColorAlphaType.PREMUL
        CGImageAlphaInfo.kCGImageAlphaFirst, CGImageAlphaInfo.kCGImageAlphaLast -> ColorAlphaType.UNPREMUL
        CGImageAlphaInfo.kCGImageAlphaNone, CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst, CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> ColorAlphaType.OPAQUE
        else -> ColorAlphaType.UNKNOWN
    }

    val byteArray = ByteArray(length.toInt()) { index ->
        bytePointer[index].toByte()
    }
    CFRelease(data)
    CFRelease(imageRef)

    return Image.makeRaster(
        imageInfo = ImageInfo(
            width = width,
            height = height,
            colorType = ColorType.RGBA_8888,
            alphaType = alphaType
        ),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}

actual fun saveImageBitmap(imageBitmap: ImageBitmap, filename: String) {
    // 1. Convert ImageBitmap to UIImage (you'll likely need to write a helper function for this conversion)
    let uiImage = imageBitmap.toUIImage() // Replace with your conversion logic

    // 2. Get a suitable file path (consider using iOS's file system APIs)
    let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    let fileURL = documentsDirectory.appendingPathComponent(filename)

    // 3. Save the UIImage to the file
    if let data = uiImage.pngData() {
        do {
            try data.write(to: fileURL)
                // Optionally, show a success message or notification
            } catch {
                // Handle exceptions (e.g., show an error message)
            }
        }
}