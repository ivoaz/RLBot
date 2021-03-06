package tarehart.rlbot.math.vector

data class Vector2(val x: Double, val y: Double) {

    val isZero: Boolean
        get() = x == 0.0 && y == 0.0

    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }

    operator fun minus(other: Vector2): Vector2 {
        return Vector2(x - other.x, y - other.y)
    }

    fun scaled(scale: Double): Vector2 {
        return Vector2(x * scale, y * scale)
    }

    /**
     * If magnitude is negative, we will return a vector facing the opposite direction.
     */
    fun scaledToMagnitude(magnitude: Double): Vector2 {
        if (isZero) {
            throw IllegalStateException("Cannot scale up a vector with length zero!")
        }
        val scaleRequired = magnitude / magnitude()
        return scaled(scaleRequired)
    }

    fun distance(other: Vector2): Double {
        val xDiff = x - other.x
        val yDiff = y - other.y
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff)
    }

    fun magnitude(): Double {
        return Math.sqrt(magnitudeSquared())
    }

    fun magnitudeSquared(): Double {
        return x * x + y * y
    }

    fun normalized(): Vector2 {

        if (isZero) {
            throw IllegalStateException("Cannot normalize a vector with length zero!")
        }
        return this.scaled(1 / magnitude())
    }

    fun dotProduct(other: Vector2): Double {
        return x * other.x + y * other.y
    }

    fun correctionAngle(ideal: Vector2): Double {
        var currentRad = Math.atan2(y, x)
        var idealRad = Math.atan2(ideal.y, ideal.x)

        if (Math.abs(currentRad - idealRad) > Math.PI) {
            if (currentRad < 0) {
                currentRad += Math.PI * 2
            }
            if (idealRad < 0) {
                idealRad += Math.PI * 2
            }
        }

        return idealRad - currentRad
    }

    fun correctionAngle(ideal: Vector2, clockwise: Boolean): Double {
        var currentRad = Math.atan2(y, x)
        var idealRad = Math.atan2(ideal.y, ideal.x)

        if ((idealRad - currentRad) > 0 && clockwise) {
            currentRad += Math.PI * 2
        }
        if ((idealRad - currentRad) < 0 && !clockwise) {
            idealRad += Math.PI * 2
        }

        return idealRad - currentRad
    }

    override fun toString(): String {
        return ("(" + String.format("%.2f", x)
                + ", " + String.format("%.2f", y)
                + ")")
    }

    fun toVector3(): Vector3 {
        return Vector3(x, y, 0.0)
    }

    companion object {

        /**
         * Will always return a positive value <= Math.PI
         */
        fun angle(a: Vector2, b: Vector2): Double {
            return Math.abs(a.correctionAngle(b))
        }
    }
}
