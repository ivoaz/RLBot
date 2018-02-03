package tarehart.rlbot.steps.wall

import tarehart.rlbot.AgentInput
import tarehart.rlbot.AgentOutput
import tarehart.rlbot.physics.ArenaModel
import tarehart.rlbot.planning.SteerUtil
import tarehart.rlbot.steps.Step
import tarehart.rlbot.tuning.BotLog

import java.awt.*
import java.util.Optional

class MountWallStep : Step {

    override val situation = "Mounting the wall"

    override fun getOutput(input: AgentInput): AgentOutput? {

        val car = input.myCarData

        if (ArenaModel.isCarOnWall(car)) {
            // Successfully made it onto the wall
            return null
        }

        val ballPath = ArenaModel.predictBallPath(input)
        if (!WallTouchStep.hasWallTouchOpportunity(input, ballPath)) {
            // Failed to mount the wall in time.
            BotLog.println("Canceling wall mount because we lost the wall touch opportunity.", input.playerIndex)
            return null
        }

        val space = input.ballPosition
        val ballPositionExaggerated = space.scaled(1.1) // This assumes the ball is close to the wall

        return SteerUtil.steerTowardGroundPosition(car, ballPositionExaggerated)
    }

    override fun canInterrupt(): Boolean {
        return true
    }

    override fun drawDebugInfo(graphics: Graphics2D) {
        // Draw nothing.
    }
}
