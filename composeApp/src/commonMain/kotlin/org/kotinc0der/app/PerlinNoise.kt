package org.kotinc0der.app

import kotlin.math.floor
import kotlin.math.round

data class Vector2(val x: Double, val y: Double) {
    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }

    infix fun dot(other: Vector2): Double {
        return x * other.x + y * other.y
    }
}

object Perlin {

    private val permutation = intArrayOf(
        151, 160, 137,  91,  90,  15, 131,  13, 201,  95,  96,  53, 194, 233,   7, 225,
        140,  36, 103,  30,  69, 142,   8,  99,  37, 240,  21,  10,  23, 190,   6, 148,
        247, 120, 234,  75,   0,  26, 197,  62,  94, 252, 219, 203, 117,  35,  11,  32,
        57, 177,  33,  88, 237, 149,  56,  87, 174,  20, 125, 136, 171, 168,  68, 175,
        74, 165,  71, 134, 139,  48,  27, 166,  77, 146, 158, 231,  83, 111, 229, 122,
        60, 211, 133, 230, 220, 105,  92,  41,  55,  46, 245,  40, 244, 102, 143,  54,
        65,  25,  63, 161,   1, 216,  80,  73, 209,  76, 132, 187, 208,  89,  18, 169,
        200, 196, 135, 130, 116, 188, 159,  86, 164, 100, 109, 198, 173, 186,   3,  64,
        52, 217, 226, 250, 124, 123,   5, 202,  38, 147, 118, 126, 255,  82,  85, 212,
        207, 206,  59, 227,  47,  16,  58,  17, 182, 189,  28,  42, 223, 183, 170, 213,
        119, 248, 152,   2,  44, 154, 163,  70, 221, 153, 101, 155, 167,  43, 172,   9,
        129,  22,  39, 253,  19,  98, 108, 110,  79, 113, 224, 232, 178, 185, 112, 104,
        218, 246,  97, 228, 251,  34, 242, 193, 238, 210, 144,  12, 191, 179, 162, 241,
        81,  51, 145, 235, 249,  14, 239, 107,  49, 192, 214,  31, 181, 199, 106, 157,
        184,  84, 204, 176, 115, 121,  50,  45, 127,   4, 150, 254, 138, 236, 205,  93,
        222, 114,  67,  29,  24,  72, 243, 141, 128, 195,  78,  66, 215,  61, 156, 180
    )

    private var p = IntArray(512) {
        if (it < 256) permutation[it] else permutation[it - 256]
    }

    private fun noise(x: Double, y: Double): Double {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255

        // Find relative x, y of point in square
        val xx = x - floor(x)
        val yy = y - floor(y)

        val topRight = Vector2(xx-1.0, yy-1.0)
        val topLeft = Vector2(xx, yy-1.0)
        val bottomRight = Vector2(xx-1.0, yy)
        val bottomLeft = Vector2(xx, yy)

        // Select a value from the permutation array for each of the 4 corners
        val topRightValue = p[p[xi+1] + yi+1]
        val topLeftValue = p[p[xi] + yi+1]
        val bottomRightValue = p[p[xi+1] + yi]
        val bottomLeftValue = p[p[xi] + yi]

        val topRightDot = topRight.dot(getConstantVector(topRightValue))
        val topLeftDot = topLeft.dot(getConstantVector(topLeftValue))
        val bottomRightDot = bottomRight.dot(getConstantVector(bottomRightValue))
        val bottomLeftDot = bottomLeft.dot(getConstantVector(bottomLeftValue))

        val u = fade(xx)
        val v = fade(yy)

        val result = linearInterpolation(
            u,
            linearInterpolation(v, bottomLeftDot, topLeftDot),
            linearInterpolation(v, bottomRightDot, topRightDot)
        )

        return result
    }

    private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)

    private fun linearInterpolation(percent: Double, a: Double, b: Double) = a + percent * (b - a)

    private fun getConstantVector(perm: Int): Vector2 {
        val h = perm and 3

        return when(h) {
            0 -> Vector2(1.0, 1.0)
            1 -> Vector2(-1.0, 1.0)
            2 -> Vector2(-1.0, -1.0)
            else -> Vector2(1.0, -1.0)
        }
    }

    fun fractalBrownianMotion(
        x: Int,
        y: Int,
        octaves: Int,
        initialAmplitude: Double,
        initialFrequency: Double
    ) : Double {
        var result = 0.0
        var amplitude = initialAmplitude
        var frequency = initialFrequency

        for (octave in 0 until octaves) {
            val n = amplitude * noise(x * frequency, y * frequency)
            result += n

            amplitude *= 0.5
            frequency *= 2.0
        }

        val oldMin = -1.0
        val oldMax = 1.0
        val roundedNoise = (round(result * 100) / 100).coerceIn(oldMin, oldMax)

        val newMin = 0
        val newMax = 100

        val oldRange = (oldMax - oldMin)
        val newRange = (newMax - newMin)
        val mappedNoise = (((roundedNoise - oldMin) * newRange) / oldRange) + newMin
        return mappedNoise
    }

    fun shufflePermutations() {
        permutation.shuffle()
        p = IntArray(512) {
            if (it < 256) permutation[it] else permutation[it - 256]
        }
    }
}
