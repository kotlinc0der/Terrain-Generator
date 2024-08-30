package org.kotlinc0der.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this

        setContent {
            App()
        }
    }

    companion object {
        lateinit var appContext: Context
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}