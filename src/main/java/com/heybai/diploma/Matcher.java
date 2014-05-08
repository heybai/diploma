package com.heybai.diploma;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.cvLine;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by heybai on 5/7/14.
 */
public class Matcher {

    public Matches match(Features f1, Features f2) {
        BFMatcher matcher = new BFMatcher(NORM_L2, true);
        DMatch matches = new DMatch();
        matcher.match(f1.getDescriptors(), f2.getDescriptors(), matches);

        List<Match> ms = new ArrayList<Match>();
        for (int i = 0; i < matches.capacity(); ++i) {
            DMatch m = matches.position(i);
            Point2f pt1 = f1.getKeyPoints().position(m.queryIdx()).pt();
            Point2f pt2 = f2.getKeyPoints().position(m.trainIdx()).pt();
            ms.add(new Match(
                    new Point(pt1.x(), pt1.y()),
                    new Point(pt2.x(), pt2.y())
            ));
        }

        return new Matches(ms);
    }

    public List<Matches> match(List<Features> featureses) {
        List<Matches> res = new ArrayList<Matches>();
        for (int i = 1; i < featureses.size(); ++i) {
            res.add(match(featureses.get(i - 1), featureses.get(i)));
        }
        return res;
    }

    public List<Matches> filter(List<Matches> matcheses, Point center, float radius) {
        List<Matches> res = new ArrayList<Matches>();
        for (Matches m : matcheses) {
            res.add(filter(m, center, radius));
        }
        return res;
    }

    public Matches filter(Matches matches, Point center, float radius) {
        List<Match> res = new ArrayList<Match>();
        for (Match m : matches.getMatches()) {
            if (isValid(m, center, radius)) {
                res.add(m);
            }
        }
        return new Matches(res);
    }

    public boolean isValid(Match match, Point center, float radius) {
        float p1Dist = MathUtils.dist(match.getP1(), center);
        float p2Dist = MathUtils.dist(match.getP2(), center);
        float dist = MathUtils.dist(match);

        if (p1Dist < radius * 0.3) {
            return false;
        }
        if (p2Dist < radius * 0.3) {
            return false;
        }

        if (dist > 120) {
            return false;
        }

        if (dist > 0.5) {
            float angel;
            if (p1Dist > p2Dist) {
                angel = MathUtils.angel(match.getP1(), center, match.getP2());
                if (angel > Math.PI / 10) {
                    return false;
                }
            } else {
                angel = MathUtils.angel(match.getP2(), center, match.getP1());
                if (angel > Math.PI / 10) {
                    return false;
                }
            }
        }

        return true;
    }

    public void apply(Frame f, Matches matches) {
        for (Match m : matches.getMatches()) {
            cvLine(f.img(), ImgUtils.point(m.getP1()), ImgUtils.point(m.getP2()), CvScalar.RED, 1, 8, 0);
        }
    }

}
