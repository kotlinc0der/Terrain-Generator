package org.kotinc0der.app

import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


actual fun getImageBitmap(colors: List<List<Color>>): ImageBitmap {
    val intColors = colors
        .flatten()
        .map { it.toArgb() }
        .toIntArray()

    val width = colors.firstOrNull()?.size ?: 0
    val height = colors.size

    return Bitmap.createBitmap(
        intColors,
        width,
        height,
        Bitmap.Config.ARGB_8888
    ).asImageBitmap()
}

private const val PERMISSION_DENIED = "Permission denied. Grant permissions and try again."
private const val IMAGE_SAVED_MSG = "Image saved to Downloads folder"

actual fun saveImageBitmap(imageBitmap: ImageBitmap, filename: String) {
    val context = ContextUtils.instance.context
    val fileFormat = "image/png"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, fileFormat)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }
    val contentResolver = context.contentResolver
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentResolver.let { resolver ->
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                resolver.openOutputStream(uri)?.use { out ->
                    imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                    Toast.makeText(context, IMAGE_SAVED_MSG, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    } else {
        // For older Android versions (below API 29),
        // you need to request storage permissions
        // and handle file saving in the app's private external storage.
        val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                writePermission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(writePermission),
                1
            )

            Toast.makeText(
                context,
                PERMISSION_DENIED,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?.let { uri ->
                println(uri)
                contentResolver.openOutputStream(uri).use { out ->
                    imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out!!)
                    Toast.makeText(context, IMAGE_SAVED_MSG, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}