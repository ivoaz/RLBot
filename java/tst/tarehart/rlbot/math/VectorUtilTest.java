package tarehart.rlbot.math;

import org.junit.Assert;
import org.junit.Test;
import tarehart.rlbot.math.vector.Vector3;

public class VectorUtilTest {
    @Test
    public void getCorrectionAngle() throws Exception {

        // Car on ground, pointed positive X. Wants to turn toward positive Y. That's a left turn.
        Assert.assertEquals(Math.PI / 2, VectorUtil.INSTANCE.getCorrectionAngle(
                new Vector3(1, 0, 0), new Vector3(0, 1, 0), new Vector3(0, 0, 1)), 0.001);

        // Car on side wall, negative X side, pointed straight up. Wants to turn toward positive Y. That's a right turn.
        Assert.assertEquals(-Math.PI / 2, VectorUtil.INSTANCE.getCorrectionAngle(
                new Vector3(0, 0, 1), new Vector3(0, 1, 0), new Vector3(1, 0, 0)), 0.001);

        // Car on side wall, positive X side, pointed straight up. Wants to turn toward positive Y. That's a left turn.
        Assert.assertEquals(Math.PI / 2, VectorUtil.INSTANCE.getCorrectionAngle(
                new Vector3(0, 0, 1), new Vector3(0, 1, 0), new Vector3(-1, 0, 0)), 0.001);

    }

}