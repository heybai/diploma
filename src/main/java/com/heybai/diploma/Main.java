package com.heybai.diploma;

import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by heybai on 5/7/14.
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    // Video file path
    static String videoPath = "/Volumes/Macintosh HD/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/materials/e1.avi";

    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
//        featuresStats();
//        matchesStats();
        triplesStats();
//        triangulation();
//        levenberg();
    }

    public static void featuresStats() throws FrameGrabber.Exception {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);
        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.04, 10, 1.6));
        r.outputFeatures(v);
        MathPlot.plot("Features stats", "frame", "nFeatures", r.featuresPlot(v));
    }

    public static void matchesStats() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);
//        Video v = r.grab(
//                "/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/code/diploma/f1.jpg",
//                "/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/code/diploma/f2.jpg"
//        );

        r.removeDuplicates(v);
        // best: 0, 3, 0.02, 10, 1.6
        r.findFeatures(v, new SiftConfig(0, 3, 0.04, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.outputMatches(v);
        MathPlot.plot("Matches stats", "frame", "nMatches", r.matchesPlot(v));
    }

    public static void triplesStats() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);
        r.printTriplesForLevenberg(v);
//        r.outputTriples(v);
//        MathPlot.plot("Triples stats", "frame", "nTriples", r.triplesPlot(v));
    }

    public static void triangulation() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(
                "/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/code/diploma/f1.jpg",
                "/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/code/diploma/f2.jpg"
        );
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findPipeCenter(v);
        r.findMatches(v);
        r.filterMatches(v);
//        r.outputMatches(v);
        r.mmm(v);
    }

    public static void levenberg() {
//        LevenbergMarquardt lm = new LevenbergMarquardt(new LevenbergMarquardt.Function() {
//            @Override
//            public void compute(DenseMatrix64F param, DenseMatrix64F x, DenseMatrix64F y) {
//                double k = param.get(0);
//                double b = param.get(1);
//                for (int i = 0; i < x.getNumElements(); ++i) {
//                    double xx = x.get(i, 0);
//                    double yy = x.get(i, 1);
//                    double res = k * xx + b - yy;
//                    y.set(i, res);
//                }
//            }
//        });
//        DenseMatrix64F param = new DenseMatrix64F(new double[][]{
//                {1, 0}
//        });
//        DenseMatrix64F X = new DenseMatrix64F(new double[][]{
//                {2, 3.1},
//                {4, 3.9},
//                {6, 5.1},
//                {8, 6.1},
//                {10,7.1},
//                {12, 7.9},
//        });
//        DenseMatrix64F Y = new DenseMatrix64F(new double[][]{
//                {0},
//                {0},
//                {0},
//                {0},
//                {0},
//                {0},
//        });
//        lm.optimize(param, X, Y);
//
//        System.out.println(param);
    }

}
