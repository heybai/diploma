package com.heybai.diploma;

import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by heybai on 5/7/14.
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    // Video file path
    static String videoPath = "/Volumes/Macintosh HD/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/materials/e1.avi";

    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
        LOG.info("Start");
//        featuresStats();
//        matchesStats();
//        triplesStats();
//        cameraPosesStats();
//        triangulation();
        triangulationParallel();
        LOG.info("Stop");
    }

    public static void featuresStats() throws FrameGrabber.Exception {
        Reconstructor r = new Reconstructor();

        Video v = r.grab(videoPath);
        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.04, 10, 1.6));
        r.outputFeatures(v);
        MathPlot.plot("Features stats", "frame", "nFeatures", r.featuresPlot(v));
    }

    public static void matchesStats() throws FrameGrabber.Exception, InterruptedException {
        Reconstructor r = new Reconstructor();

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
        Reconstructor r = new Reconstructor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);
//        r.printTriplesForLevenberg(v);
//        r.outputTriples(v);
//        MathPlot.plot("Triples stats", "frame", "nTriples", r.triplesPlot(v));
    }

    public static void cameraPosesStats() throws FrameGrabber.Exception, InterruptedException {
        Reconstructor r = new Reconstructor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);

        r.findCameraPoses(v, 4);
//        r.outputTriples(v);
        MathPlot.plot("Camera poses & avg matches", "camera", "delta z & avg match length", r.cameraPosesPlot(v), r.mathchesAvgDistPlot(v));
//        MathPlot.plot("Camera poses & avg matches", "camera", "delta z & avg match length", r.cameraPosesPlot(v));

//        List<Point>[] plots = new List[5];
//        for (int i = 0; i < 5; ++i) {
//            r.findCameraPoses(v, 2);
//            plots[i] = r.cameraPosesPlot(v);
//        }
    }

    public static void triangulation() throws FrameGrabber.Exception, InterruptedException {
        Reconstructor r = new Reconstructor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);

        r.findCameraPoses(v, 2);
        List<Point3D> points = r.triangulation(v, 2);
//        points = filterPoints(points);
        ObjProducer.createObj(points, "pipe.obj");

//        for (int i = 0; i < 5; ++i) {
//            r.findCameraPoses(v, i);
//            ObjProducer.createObj(r.triangulation(v, i), String.format("pipe - %s.obj", i));
//        }
    }

    public static void triangulationParallel() throws FrameGrabber.Exception, InterruptedException {
        Reconstructor r = new Reconstructor();

        Video v = r.grab(videoPath);

        r.removeDuplicatesParallel(v);
        r.findFeaturesParallel(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatchesParallel(v);
        r.findPipeCenterParallel(v);
        r.filterMatchesParallel(v);
        r.findTriplesParallel(v);
        r.findCameraPosesParallel(v, 2);
        List<Point3D> points = r.triangulationParallel(v, 2);
//        points = filterPoints(points);
        ObjProducer.createObj(points, "pipe.obj");
    }

    private static List<Point3D> filterPoints(List<Point3D> points) {
        Point center = new Point(0, 0);
        double e = 0.4;
        double rr = 3.18;
        double sumX = 0;
        double sumY = 0;
        double sumR = 0;

        List<Point3D> filtered = new ArrayList<Point3D>();
        for (Point3D p : points) {
            Point p2 = new Point((float) p.getX(), (float) p.getY());
            if (Math.abs(rr - MathUtils.dist(center, p2)) < e) {
                filtered.add(p);
            }
            sumX += p.getX();
            sumY = p.getY();
            sumR += MathUtils.dist(center, p2);
        }
        LOG.info("{} filtered points left", filtered.size());
        LOG.info("Average center is ({}, {}). Average radius is {}", sumX / points.size(), sumY / points.size(), sumR / points.size());

        return filtered;
    }

}
