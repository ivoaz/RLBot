package tarehart.rlbot.steps.strikes

import tarehart.rlbot.AgentInput
import tarehart.rlbot.AgentOutput
import tarehart.rlbot.intercept.AerialMath
import tarehart.rlbot.intercept.InterceptCalculator
import tarehart.rlbot.math.SpaceTime
import tarehart.rlbot.math.VectorUtil
import tarehart.rlbot.math.vector.Vector2
import tarehart.rlbot.math.vector.Vector3
import tarehart.rlbot.physics.ArenaModel
import tarehart.rlbot.planning.GoalUtil
import tarehart.rlbot.planning.Plan
import tarehart.rlbot.planning.SteerUtil
import tarehart.rlbot.planning.cancellation.InterceptDisruptionMeter
import tarehart.rlbot.steps.BlindStep
import tarehart.rlbot.steps.NestedPlanStep
import tarehart.rlbot.steps.TapStep
import tarehart.rlbot.steps.rotation.PitchToPlaneStep
import tarehart.rlbot.steps.rotation.RollToPlaneStep
import tarehart.rlbot.steps.rotation.YawToPlaneStep
import tarehart.rlbot.time.Duration
import tarehart.rlbot.time.GameTime
import tarehart.rlbot.tuning.BotLog
import tarehart.rlbot.ui.ArenaDisplay

import java.awt.*
import java.util.Optional

class MidairStrikeStep(private val timeInAirAtStart: Duration) : NestedPlanStep() {

    private var confusionCount = 0
    private lateinit var lastMomentForDodge: GameTime
    private lateinit var beginningOfStep: GameTime
    private var intercept: SpaceTime? = null
    private val disruptionMeter = InterceptDisruptionMeter(distanceThreshold = 30.0)

    override fun getLocalSituation(): String {
        return "Midair Strike"
    }

    override fun shouldCancelPlanAndAbort(input: AgentInput): Boolean {
        return input.myCarData.hasWheelContact
    }

    override fun canAbortPlanInternally(): Boolean {
        return true
    }

    override fun doComputationInLieuOfPlan(input: AgentInput): AgentOutput? {

        if (! ::lastMomentForDodge.isInitialized) {
            lastMomentForDodge = input.time.plus(MAX_TIME_FOR_AIR_DODGE).minus(timeInAirAtStart)
            beginningOfStep = input.time
        }

        // We hold down the jump button during the aerial for extra upward acceleration, but it wears off.
        val timeSinceLaunch = Duration.between(beginningOfStep, input.time)

        val ballPath = ArenaModel.predictBallPath(input)
        val car = input.myCarData
        var offset = GoalUtil.getOwnGoal(car.team).center.scaledToMagnitude(3.0).minus(Vector3(0.0, 0.0, .6))

        intercept?.let {
            val goalToBall = it.space.minus(GoalUtil.getEnemyGoal(car.team).getNearestEntrance(it.space, 4.0))
            offset = goalToBall.scaledToMagnitude(3.0)
            if (goalToBall.magnitude() > 110) {
                offset = Vector3(offset.x, offset.y, -.2)
            }
        }

        val latestIntercept = InterceptCalculator.getAerialIntercept(car, ballPath, offset, beginningOfStep)
        if (latestIntercept == null || disruptionMeter.isDisrupted(latestIntercept.ballSlice)) {
            if (input.latestBallTouch.isPresent) {
                val latestTouch = input.latestBallTouch.get()
                if (latestTouch.playerIndex == car.playerIndex && Duration.between(latestTouch.time, car.time).seconds < 0.5) {
                    // We successfully hit the ball. Let's go for a double touch by resetting the disruption meter.
                    latestIntercept ?.let {
                        disruptionMeter.reset(it.ballSlice)
                    }
                }
            }
            confusionCount++
            if (confusionCount > 3) {
                BotLog.println("Intercept has unacceptable disruption, quitting midair strike.", car.playerIndex)
                return null
            }
            return AgentOutput().withBoost()
        }

        intercept = latestIntercept.toSpaceTime()
        val carToIntercept = latestIntercept.space.minus(car.position)
        val millisTillIntercept = Duration.between(input.time, latestIntercept.time).millis
        val distance = car.position.distance(input.ballPosition)

        val correctionAngleRad = SteerUtil.getCorrectionAngleRad(car, latestIntercept.space)

        if (input.time.isBefore(lastMomentForDodge) && distance < DODGE_TIME.seconds * car.velocity.magnitude() && latestIntercept.space.z - car.position.z < 1.5) {
            // Let's flip into the ball!
            if (Math.abs(correctionAngleRad) <= SIDE_DODGE_THRESHOLD) {
                BotLog.println("Front flip strike", input.playerIndex)
                startPlan(Plan()
                        .withStep(BlindStep(Duration.ofMillis(30), AgentOutput()))
                        .withStep(BlindStep(Duration.ofSeconds(1.0), AgentOutput().withPitch(-1.0).withJump())),
                        input)
            } else {
                // Dodge to the side
                BotLog.println("Side flip strike", input.playerIndex)
                startPlan(Plan()
                        .withStep(BlindStep(Duration.ofMillis(30), AgentOutput()))
                        .withStep(BlindStep(Duration.ofMillis(30), AgentOutput().withYaw((if (correctionAngleRad < 0) 1 else -1).toDouble()).withJump())),
                        input)
            }
        }

        val leftRightDivergence = Math.sin(Vector2.angle(car.velocity.flatten(), carToIntercept.flatten())) * carToIntercept.magnitude()
        val secondsSoFar = Duration.between(beginningOfStep, input.time).seconds

        if (millisTillIntercept > DODGE_TIME.millis && secondsSoFar > 2 && leftRightDivergence > 20) {
            BotLog.println("Failed aerial on bad angle", input.playerIndex)
            return null
        }

        val projectedHeight = AerialMath.getProjectedHeight(
                car, Duration.between(car.time, latestIntercept.time).seconds,
                timeSinceLaunch.seconds
        )

        val heightError = projectedHeight - latestIntercept.space.z

        val flatToIntercept = carToIntercept.flatten()

        val currentFlatVelocity = car.velocity.flatten()

        val leftRightCorrectionAngle = currentFlatVelocity.correctionAngle(flatToIntercept)
        val desiredFlatOrientation = VectorUtil
                .rotateVector(currentFlatVelocity, leftRightCorrectionAngle + Math.signum(leftRightCorrectionAngle) * YAW_OVERCORRECT)
                .normalized()

        val desiredNoseVector: Vector3

        val finesseMode = millisTillIntercept < NOSE_FINESSE_TIME.millis && latestIntercept.time.isAfter(lastMomentForDodge) && heightError > 0 && offset.z > 0

        if (finesseMode) {
            // Nose into the ball
            desiredNoseVector = offset.scaled(-1.0).normaliseCopy()
        } else {
            // Fly toward intercept
            val extraHeight = if (offset.z > 0) 1.6 else 0.0
            val desiredZ = AerialMath.getDesiredZComponentBasedOnAccel(
                    latestIntercept.space.z + extraHeight,
                    Duration.between(car.time, latestIntercept.time),
                    timeSinceLaunch,
                    car)
            desiredNoseVector = convertToVector3WithPitch(desiredFlatOrientation, desiredZ)
        }


        val pitchPlaneNormal = car.orientation.rightVector.crossProduct(desiredNoseVector)
        val yawPlaneNormal = VectorUtil.rotateVector(desiredFlatOrientation, -Math.PI / 2).toVector3().normaliseCopy()

        val pitchOutput = PitchToPlaneStep(pitchPlaneNormal).getOutput(input)
        val yawOutput = YawToPlaneStep({yawPlaneNormal}, false).getOutput(input)
        val rollOutput = RollToPlaneStep(Vector3(0.0, 0.0, 1.0), false).getOutput(input)

        return mergeOrientationOutputs(pitchOutput, yawOutput, rollOutput)
                .withBoost(finesseMode || desiredNoseVector.dotProduct(car.orientation.noseVector) > .5)
                .withJump()
    }


    private fun mergeOrientationOutputs(pitchOutput: AgentOutput?, yawOutput: AgentOutput?, rollOutput: AgentOutput?): AgentOutput {
        val output = AgentOutput()
        pitchOutput?.let { output.withPitch(it.pitch) }
        yawOutput?.let { output.withYaw(it.yaw) }
        rollOutput?.let { output.withRoll(it.roll) }

        return output
    }

    override fun canInterrupt(): Boolean {
        return false
    }

    override fun drawDebugInfo(graphics: Graphics2D) {
        super.drawDebugInfo(graphics)

        intercept?.let {
            ArenaDisplay.drawBall(it.space, graphics, Color(23, 194, 8))
        }
    }

    companion object {
        private val SIDE_DODGE_THRESHOLD = Math.PI / 4
        private val DODGE_TIME = Duration.ofMillis(400)
        //private static final double DODGE_DISTANCE = 6;
        val MAX_TIME_FOR_AIR_DODGE = Duration.ofSeconds(1.4)
        private val YAW_OVERCORRECT = .1
        private val NOSE_FINESSE_TIME = Duration.ofMillis(800)

        /**
         * Return a unit vector with the given z component, and the same flat angle as flatDirection.
         */
        private fun convertToVector3WithPitch(flat: Vector2, zComponent: Double): Vector3 {
            val xyScaler = Math.sqrt((1 - zComponent * zComponent) / (flat.x * flat.x + flat.y * flat.y))
            return Vector3(flat.x * xyScaler, flat.y * xyScaler, zComponent)
        }
    }
}
