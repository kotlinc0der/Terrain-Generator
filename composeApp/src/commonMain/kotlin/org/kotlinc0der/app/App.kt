package org.kotlinc0der.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val viewModel by remember { mutableStateOf(AppViewModel()) }

    MaterialTheme {
        BoxWithConstraints {
            // check if is in Portrait mode
            if (maxWidth < maxHeight) {
                Column(Modifier.fillMaxSize()) {
                    ColorGridBitMap(
                        viewModel.bitmap.value,
                        modifier = Modifier.fillMaxWidth().weight(0.5f)
                    )

                    ControlSection(
                        Modifier.fillMaxWidth().weight(0.5f),
                        viewModel
                    )
                }
            } else {
                Row(Modifier.fillMaxSize()) {
                    ColorGridBitMap(
                        viewModel.bitmap.value,
                        modifier = Modifier.fillMaxHeight().weight(0.65f)
                    )

                    ControlSection(
                        Modifier.fillMaxHeight().weight(0.35f),
                        viewModel,
                    )
                }
            }

            // Generate Terrain the first time
            // without impacting the loading of the app
            LaunchedEffect(Unit) {
                viewModel.generateTerrain()
            }
        }
    }
}

@Composable
private fun ControlSection(
    modifier: Modifier,
    viewModel: AppViewModel
) {
    val paddingValue = 16.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(paddingValue, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ControlPanel(viewModel)

        Row (
            horizontalArrangement = Arrangement.spacedBy(paddingValue, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(paddingValue)
        ) {
            Button(onClick = {
                viewModel.generateTerrain()
            }) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(paddingValue), // Adjust size as needed
                        strokeWidth = 2.dp, // Adjust stroke width as needed
                        color = MaterialTheme.colors.onPrimary
                    )
                } else {
                    Text("Generate Terrain")
                }
            }

            Button(onClick = {
                saveImageBitmap(viewModel.bitmap.value, "terrain.png")
            },
                enabled = !viewModel.isLoading.value) {
                Text("Save Image")
            }
        }
    }
}

@Composable
@Stable
fun ColorGridBitMap(
    bitmap: ImageBitmap,
    modifier: Modifier
) {
    Canvas(
        modifier = modifier
            .clipToBounds()
            .fillMaxSize()
            .padding(16.dp)
    ) {
        drawImage(
            image = bitmap,
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
    }
}

fun List<List<Color>>.imageBitmap(): ImageBitmap {
    return getImageBitmap(this)
}