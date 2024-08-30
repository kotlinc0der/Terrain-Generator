package org.kotlinc0der.app

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kotlinc0der.app.AppColors.DEEPER_WATER
import org.kotlinc0der.app.AppColors.DEEP_WATER
import org.kotlinc0der.app.AppColors.EVEN_DEEPER_WATER
import org.kotlinc0der.app.AppColors.GRASS
import org.kotlinc0der.app.AppColors.MOUNTAIN_BASE
import org.kotlinc0der.app.AppColors.MOUNTAIN_MID
import org.kotlinc0der.app.AppColors.ROCKY_SLOPES
import org.kotlinc0der.app.AppColors.SAND
import org.kotlinc0der.app.AppColors.SHALLOW_WATER
import org.kotlinc0der.app.AppColors.SNOWY_PEAK
import org.kotlinc0der.app.AppColors.SNOWY_SLOPES

class AppViewModel : ViewModel() {
    private val _gridSize = mutableStateOf(1000)
    val gridSize: State<Int> = _gridSize

    private val _octave = mutableStateOf(10)
    val octave: State<Int> = _octave

    private val _frequency = mutableStateOf(0.005)
    val frequency: State<Double> = _frequency

    private val _amplitude = mutableStateOf(1.0)
    val amplitude: State<Double> = _amplitude

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _bitmap = mutableStateOf(ImageBitmap(1, 1))
    val bitmap: State<ImageBitmap> = _bitmap

    fun updateGridSize(newSize: Int) {
        _gridSize.value = newSize
    }

    fun updateOctave(newOctave: Int) {
        _octave.value = newOctave
    }

    fun updateFrequency(newFrequency: Double) {
        _frequency.value = newFrequency
    }

    fun updateAmplitude(newAmplitude: Double) {
        _amplitude.value = newAmplitude
    }

    fun generateTerrain() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            Perlin.shufflePermutations()
            val colors =
                updateColorsMap(gridSize.value, octave.value, frequency.value, amplitude.value)
            _bitmap.value = colors.imageBitmap()
            _isLoading.value = false
        }
    }

    private fun updateColorsMap(gridSize: Int, octave: Int, frequency: Double, amplitude: Double) =
        List(gridSize) { rowIndex ->
            List(gridSize) { colIndex ->
                val value = Perlin.fractalBrownianMotion(
                    colIndex,
                    rowIndex,
                    octaves = octave,
                    initialAmplitude = amplitude,
                    initialFrequency = frequency
                )
                val mappedColor = when {
                    value < 25 -> EVEN_DEEPER_WATER // Deep water (dark blue)
                    value < 35 -> DEEPER_WATER // Deeper water (medium blue)
                    value < 38 -> DEEP_WATER // Even deeper water (bright blue)
                    value < 42 -> SHALLOW_WATER // Shallow water (light blue)
                    value < 43 -> SAND // Sand (beige)
                    value < 80 -> GRASS // Grass (green)
                    value < 85 -> MOUNTAIN_BASE // Base layer of mountains (brown)
                    value < 90 -> MOUNTAIN_MID // Mid layer of mountains (darker gray)
                    value < 95 -> ROCKY_SLOPES // Rocky slopes (light gray)
                    value < 97 -> SNOWY_SLOPES // Snowy slopes (lighter gray)
                    else -> SNOWY_PEAK // Snowy peaks (white)
                }
                mappedColor
            }
        }
}