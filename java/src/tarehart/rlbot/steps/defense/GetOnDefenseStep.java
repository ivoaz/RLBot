package tarehart.rlbot.steps.defense;

import mikera.vectorz.Vector3;
import tarehart.rlbot.AgentInput;
import tarehart.rlbot.AgentOutput;
import tarehart.rlbot.input.CarData;
import tarehart.rlbot.math.SplineHandle;
import tarehart.rlbot.math.VectorUtil;
import tarehart.rlbot.planning.GoalUtil;
import tarehart.rlbot.planning.Plan;
import tarehart.rlbot.planning.SteerUtil;
import tarehart.rlbot.steps.Step;
import tarehart.rlbot.tuning.BotLog;

import java.util.Optional;

public class GetOnDefenseStep implements Step {
    private SplineHandle targetLocation = null;

    private Plan plan;

    public Optional<AgentOutput> getOutput(AgentInput input) {

        if (targetLocation == null) {
            init(input);
        }

        if (plan != null && !plan.isComplete()) {
            return plan.getOutput(input);
        }

        CarData car = input.getMyCarData();

        double distance = SteerUtil.getDistanceFromCar(car, targetLocation.getLocation());
        double secondsRemaining = distance / car.velocity.magnitude();

        if (!needDefense(input) || secondsRemaining < 1.5) {
            return Optional.empty();
        }

        Vector3 target;
        if (targetLocation.isWithinHandleRange(car.position)) {
            target = targetLocation.getLocation();
        } else {
            target = targetLocation.getFarthestHandle(input.ballPosition);
        }

        Optional<Plan> sensibleFlip = SteerUtil.getSensibleFlip(car, target);
        if (sensibleFlip.isPresent()) {
            BotLog.println("Front flip for defense", input.team);
            plan = sensibleFlip.get();
            plan.begin();
            return plan.getOutput(input);
        } else {
            return Optional.of(SteerUtil.steerTowardGroundPosition(car, target));
        }
    }

    private void init(AgentInput input) {
        targetLocation = GoalUtil.getOwnGoal(input.team).navigationSpline;
    }

    @Override
    public boolean isBlindlyComplete() {
        return false;
    }

    @Override
    public void begin() {
    }

    @Override
    public boolean canInterrupt() {
        return plan == null || plan.canInterrupt();
    }

    public static boolean needDefense(AgentInput input) {

        CarData car = input.getMyCarData();
        SplineHandle myGoal = GoalUtil.getOwnGoal(input.team).navigationSpline;

        boolean alreadyOnDefense = Math.abs(myGoal.getLocation().y - car.position.y) < 10;
        if (alreadyOnDefense) {
            return false;
        }

        Vector3 ballToGoal = (Vector3) myGoal.getLocation().subCopy(input.ballPosition);
        Vector3 ballVelocityTowardGoal = VectorUtil.project(input.ballVelocity, ballToGoal);

        double ballSpeedTowardGoal = ballVelocityTowardGoal.magnitude() * Math.signum(ballVelocityTowardGoal.dotProduct(ballToGoal));
        double wrongSidedness = getWrongSidedness(input);

        boolean needDefense = ballSpeedTowardGoal > 30 && wrongSidedness > 0 || ballSpeedTowardGoal > 10 && wrongSidedness > 10;
        return needDefense;

    }

    public static double getWrongSidedness(AgentInput input) {
        SplineHandle myGoal = GoalUtil.getOwnGoal(input.team).navigationSpline;
        double playerToBallY = input.ballPosition.y - input.getMyCarData().position.y;
        return playerToBallY * Math.signum(myGoal.getLocation().y);
    }

    @Override
    public String getSituation() {
        return "Getting on defense";
    }

}
