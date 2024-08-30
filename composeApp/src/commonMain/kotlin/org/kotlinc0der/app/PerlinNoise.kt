package org.kotlinc0der.app

import kotlin.math.floor
import kotlin.math.round

data class Vector(val x: Double, val y: Double) {
    operator fun plus(other: Vector): Vector {
        return Vector(x + other.x, y + other.y)
    }

    infix fun dot(other: Vector): Double {
        return x * other.x + y * other.y
    }
}

object Perlin {
    // Permutation table used for generating pseudo-random gradient vectors
    private val permutation = intArrayOf(
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
        140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
        247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
        57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
        74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
        60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
        65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
        200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
        52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
        207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
        119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
        218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
        81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
        184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
        222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    )

    // Doubled permutation array for efficient lookup
    private var p = IntArray(512) {
        if (it < 256) permutation[it] else permutation[it - 256]
    }

    private fun noise(x: Double, y: Double): Double {
        // Calculate grid cell coordinates
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255

        // Find relative coordinates within the grid cell
        val xx = x - floor(x)
        val yy = y - floor(y)

        // Create vectors representing the corners of the grid cell
        val topRight = Vector(xx - 1.0, yy - 1.0)
        val topLeft = Vector(xx, yy - 1.0)
        val bottomRight = Vector(xx - 1.0, yy)
        val bottomLeft = Vector(xx, yy)

        // Get pseudo-random gradient vectors for each corner from the permutation table
        val topRightGradient = getConstantVector(p[p[xi + 1] + yi + 1])
        val topLeftGradient = getConstantVector(p[p[xi] + yi + 1])
        val bottomRightGradient = getConstantVector(p[p[xi + 1] + yi])
        val bottomLeftGradient = getConstantVector(p[p[xi] + yi])

        // Calculate dot products between corner vectors and gradient vectors
        val topRightDot = topRight dot topRightGradient
        val topLeftDot = topLeft dot topLeftGradient
        val bottomRightDot = bottomRight dot bottomRightGradient
        val bottomLeftDot = bottomLeft dot bottomLeftGradient

        // Apply fade function to smooth the interpolation
        val u = fade(xx)
        val v = fade(yy)

        // Perform bilinear interpolation to get the final noise value
        val result = linearInterpolation(
            u,
            linearInterpolation(v, bottomLeftDot, topLeftDot),
            linearInterpolation(v, bottomRightDot, topRightDot)
        )

        return result
    }

    // Fade function for smooth interpolation (Perlin's improved fade function)
    private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)

    private fun linearInterpolation(percent: Double, a: Double, b: Double) = a + percent * (b - a)

    // Retrieves a constant gradient vector based on a permutation value
    private fun getConstantVector(perm: Int): Vector {
        val h = perm and 3

        return when (h) {
            0 -> Vector(1.0, 1.0)
            1 -> Vector(-1.0, 1.0)
            2 -> Vector(-1.0, -1.0)
            else -> Vector(1.0, -1.0)
        }
    }

    // Generates fractal Brownian motion (fBm) noise using multiple octaves of Perlin noise
    fun fractalBrownianMotion(
        x: Int,
        y: Int,
        octaves: Int,
        initialAmplitude: Double,
        initialFrequency: Double
    ): Double {
        var result = 0.0
        var amplitude = initialAmplitude
        var frequency = initialFrequency

        // Accumulate noise values from multiple octaves
        for (octave in 0 until octaves) {
            val n = amplitude * noise(x * frequency, y * frequency)
            result += n

            // Adjust amplitude and frequency for each octave
            amplitude *= 0.5
            frequency *= 2.0
        }

        // Round and clamp the noise value
        val oldMin = -1.0
        val oldMax = 1.0
        val roundedNoise = (round(result * 100) / 100).coerceIn(oldMin, oldMax)

        val newMin = 0
        val newMax = 100

        // Map the noise value to a desired range (0 to 100 in this case)
        val oldRange = (oldMax - oldMin)
        val newRange = (newMax - newMin)
        val mappedNoise = (((roundedNoise - oldMin) * newRange) / oldRange) + newMin
        return mappedNoise
    }

    // Shuffles the permutation table to generate different noise patterns
    fun shufflePermutations() {
        permutation.shuffle()
        p = IntArray(512) {
            if (it < 256) permutation[it] else permutation[it - 256]
        }
    }
}
