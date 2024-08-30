package org.kotlinc0der.app

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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

private const val PERMISSION_DENIED_MSG = "Permission denied. Grant permissions and try again."
private const val IMAGE_SAVED_MSG = "Image saved to Downloads folder"

actual fun saveImageBitmap(imageBitmap: ImageBitmap, filename: String) {
    val context = MainActivity.appContext
    val contentResolver = context.contentResolver
    val contentValues = getContentValues(filename)
    val isRecentBuild = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val mediaUri =
        if (isRecentBuild)
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    if (isRecentBuild) {
        saveBitMapToExternalStorage(contentResolver, mediaUri, contentValues, imageBitmap, context)
    } else {
        // For older Android versions (below API 29),
        // you need to request storage permissions
        // and handle file saving in the app's private external storage.
        val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val isPermissionNotGranted = ContextCompat.checkSelfPermission(context, writePermission) == PackageManager.PERMISSION_GRANTED
        if (!isPermissionNotGranted) {
            // request permission
            ActivityCompat.requestPermissions(context as Activity, arrayOf(writePermission), 1)

            // notify user of permission denied
            Toast.makeText(context, PERMISSION_DENIED_MSG, Toast.LENGTH_LONG).show()
            return
        }
        // permission is granted, save the image
        saveBitMapToExternalStorage(contentResolver, mediaUri, contentValues, imageBitmap, context)
    }
}

private fun saveBitMapToExternalStorage(
    contentResolver: ContentResolver?,
    mediaUri: Uri,
    contentValues: ContentValues,
    imageBitmap: ImageBitmap,
    context: Context
) {
    contentResolver.let { resolver ->
        resolver?.insert(mediaUri, contentValues)?.let { uri ->
            resolver.openOutputStream(uri)?.use { out ->
                imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                Toast.makeText(context, IMAGE_SAVED_MSG, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}

private fun getContentValues(filename: String, fileFormat: String = "image/png"): ContentValues {
    return ContentValues().apply {
        val now = System.currentTimeMillis()/1000
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, fileFormat)
        put(MediaStore.MediaColumns.DATE_TAKEN, now)
        put(MediaStore.MediaColumns.DATE_ADDED, now)
        put(MediaStore.MediaColumns.DATE_MODIFIED, now)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }
}