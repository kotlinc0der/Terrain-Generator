package org.kotlinc0der.app

import androidx.compose.runtime.Composable

@Composable
fun ControlPanel(
    viewModel: AppViewModel
) {
    RangeSlider(
        label = "Grid Size",
        initial = viewModel.gridSize.value.toDouble(),
        min = 100.0,
        max = 3000.0,
        scale = 0,
        onValueChange = {
            viewModel.updateGridSize(it.toInt())
        }
    )

    RangeSlider(
        label = "Frequency",
        initial = viewModel.frequency.value,
        min = 0.001,
        max = 0.009,
        scale = 4,
        onValueChange = {
            viewModel.updateFrequency(it)
        }
    )

    RangeSlider(
        label = "Octaves",
        initial = viewModel.octave.value.toDouble(),
        min = 1.0,
        max = 10.0,
        scale = 0,
        onValueChange = {
            viewModel.updateOctave(it.toInt())
        }
    )

    RangeSlider(
        label = "Amplitude",
        initial = viewModel.amplitude.value,
        min = 0.0,
        max = 1.0,
        onValueChange = {
            viewModel.updateAmplitude(it)
        }
    )
}