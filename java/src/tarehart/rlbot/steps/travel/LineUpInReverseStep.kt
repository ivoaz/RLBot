package tarehart.rlbot.steps.travel

import tarehart.rlbot.AgentInput
import tarehart.rlbot.AgentOutput
import tarehart.rlbot.math.BotMath
import tarehart.rlbot.math.vector.Vector2
import tarehart.rlbot.steps.Step

import java.awt.*
import java.util.Optional

class LineUpInReverseStep(private val waypoint: Vector2) : Step {
    private var correctionDirection: Int? = null

    override val situation: String
        get() = "Lining up in reverse"

    override fun getOutput(input: AgentInput): AgentOutput? {

        val car = input.myCarData

        val waypointToCar = car.position.flatten().minus(waypoint)

        val correctionRadians = car.orientation.noseVector.flatten().correctionAngle(waypointToCar)

        if (correctionDirection == null) {
            correctionDirection = BotMath.nonZeroSignum(correctionRadians)
        }

        val futureRadians = correctionRadians + car.spin.yawRate * .3

        return if (futureRadians * correctionDirection!! < 0 && Math.abs(futureRadians) < Math.PI / 4) {
            null // Done orienting.
        } else AgentOutput().withThrottle(-1.0).withSteer(correctionDirection!!.toDouble())

    }

    override fun canInterrupt(): Boolean {
        return true
    }

    override fun drawDebugInfo(graphics: Graphics2D) {}
}
