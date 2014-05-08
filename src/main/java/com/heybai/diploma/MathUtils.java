package com.heybai.diploma;

/**
 * Created by heybai on 5/7/14.
 */
public class MathUtils {

    public static float dist(Match match) {
        return dist(match.getP1(), match.getP2());
    }

    public static float dist(Point p1, Point p2) {
        return (float) Math.sqrt(
                pow(p1.getX() - p2.getX())
                        + pow(p1.getY() - p2.getY())
        );
    }

    public static float pow(float v) {
        return v * v;
    }

    public static float angel(Point ct, Point p1, Point p2) {
        float a = dist(ct, p1);
        float b = dist(ct, p2);
        float c = dist(p1, p2);
        return (float) Math.acos(
                (pow(a) + pow(b) - pow(c))
                / (2 * a * b)
        );
    }

}
