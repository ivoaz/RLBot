package tarehart.rlbot.planning;

import tarehart.rlbot.math.Polygon;
import tarehart.rlbot.math.vector.Vector2;

public class ZoneDefinitions {

    // This zone is used to draw the full outline of the field
    public static final Polygon FULLFIELD = new Polygon(new Vector2[] {
        new Vector2(-81.93, -78.3),
        new Vector2(-58.36, -102.4),
        new Vector2(-17.27, -102.4),
        new Vector2(-17.27, -119.9),
        new Vector2(17.27, -119.9),
        new Vector2(17.27, -102.4),
        new Vector2(58.36, -102.4),
        new Vector2(81.93, -78.3),
        new Vector2(81.93, 78.3),
        new Vector2(58.36, 102.4),
        new Vector2(17.27, 102.4),
        new Vector2(17.27, 119.9),
        new Vector2(-17.27, 119.9),
        new Vector2(-17.27, 102.4),
        new Vector2(-58.36, 102.4),
        new Vector2(-81.93, 78.3),
        new Vector2(-81.93, -78.3)
    });

    //NOTE: Some Zone boundaries use some rounded values that go outside of the field since that won't
    // have any negative effect
    public static final Polygon ORANGE = new Polygon(new Vector2[]{
        new Vector2(-82, 34),
        new Vector2(82, 34),
        new Vector2(82, 120),
        new Vector2(-82, 120)
    });
    public static final Polygon MID = new Polygon(new Vector2[] {
        new Vector2(-82, -34),
        new Vector2(82, -34),
        new Vector2(82, 34),
        new Vector2(-82, 34)
    });
    public static final Polygon BLUE = new Polygon(new Vector2[] {
        new Vector2(-82, -34),
        new Vector2(82, -34),
        new Vector2(82, -120),
        new Vector2(-82, -120)
    });
    public static final Polygon TOP = new Polygon(new Vector2[] {
        new Vector2(0, -120),
        new Vector2(-82, -120),
        new Vector2(-82, 120),
        new Vector2(0, 120)
    });
    public static final Polygon BOTTOM = new Polygon(new Vector2[] {
        new Vector2(0, -120),
        new Vector2(82, -120),
        new Vector2(82, 120),
        new Vector2(0, 120)
    });
    public static final Polygon TOPCORNER = new Polygon(new Vector2[] {
        new Vector2(-82, -53.5),
        new Vector2(-66.5, -61.6),
        new Vector2(-43.25, -85.9),
        new Vector2(-35.5, -102.1),
        new Vector2(-35.5, -120),
        new Vector2(-82, -120),
        new Vector2(-82, -53.5),
        new Vector2(-82, 53.5),
        new Vector2(-66.5, 61.6),
        new Vector2(-43.25, 85.9),
        new Vector2(-35.5, 102.1),
        new Vector2(-35.5, 120),
        new Vector2(-82, 120),
        new Vector2(-82, 53.5)
    });
    public static final Polygon BOTTOMCORNER = new Polygon(new Vector2[] {
        new Vector2(82, -53.5),
        new Vector2(66.5, -61.6),
        new Vector2(43.25, -85.9),
        new Vector2(35.5, -102.1),
        new Vector2(35.5, -120),
        new Vector2(82, -120),
        new Vector2(82, -53.5),
        new Vector2(82, 53.5),
        new Vector2(66.5, 61.6),
        new Vector2(43.25, 85.9),
        new Vector2(35.5, 102.1),
        new Vector2(35.5, 120),
        new Vector2(82, 120),
        new Vector2(82, 53.5)
    });
    public static final Polygon ORANGEBOX = new Polygon(new Vector2[] {
       new Vector2(50, 70),
       new Vector2(50, 120),
       new Vector2(-50, 120),
       new Vector2(-50, 70)
    });
    public static final Polygon BLUEBOX = new Polygon(new Vector2[] {
       new Vector2(50, -70),
       new Vector2(50, -120),
       new Vector2(-50, -120),
       new Vector2(-50, -70)
    });
}
