package org.kotinc0der.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RangeSlider(
    label: String,
    modifier: Modifier = Modifier,
    min: Double,
    max: Double,
    scale: Int = 1,
    initial: Double = min,
    onValueChange: (Double) -> Unit
) {
    var sliderValue by remember { mutableStateOf(initial.coerceIn(min, max)) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$label: ${sliderValue.toString(scale)}",
            style = MaterialTheme.typography.button
        )

        Slider(
            value = sliderValue.toFloat(),
            onValueChange = { newValue ->
                sliderValue = newValue.toDouble().coerceIn(min, max)
                onValueChange(sliderValue)
            },
            valueRange = min.toFloat()..max.toFloat(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

fun Double.toString(scale: Int): String {
    var dotAt = 1

    repeat(scale) {
        dotAt *= 10
    }
    val rounded = (this * dotAt).toInt() / dotAt.toDouble()
    return "$rounded"
}
