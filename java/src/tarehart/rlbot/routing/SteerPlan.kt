package tarehart.rlbot.routing

import tarehart.rlbot.AgentOutput
import tarehart.rlbot.input.CarData
import tarehart.rlbot.math.Circle
import tarehart.rlbot.math.vector.Vector2
import tarehart.rlbot.math.vector.Vector3
import tarehart.rlbot.planning.SteerUtil
import tarehart.rlbot.routing.PositionFacing
import tarehart.rlbot.time.GameTime

import java.awt.*
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D

class SteerPlan(val immediateSteer: AgentOutput, val route: Route) {

    val waypoint: Vector2
        get() {
            for (part: RoutePart in route.parts) {
                part.waypoint?.let { return it }
            }
            throw NoSuchElementException("No route parts with a waypoint!")
        }

    fun drawDebugInfo(graphics: Graphics2D, car: CarData) {
        route.drawDebugInfo(graphics)
    }
}
