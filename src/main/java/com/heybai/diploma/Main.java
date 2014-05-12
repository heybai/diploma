package com.heybai.diploma;

import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


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
//        triplesStats();
//        cameraPosesStats();
        triangulation();
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

    public static void cameraPosesStats() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);
        r.findCameraPoses(v);
//        r.outputTriples(v);
        MathPlot.plot("Camera poses & avg matches", "camera", "delta z & avg match length", r.cameraPosesPlot(v), r.mathchesAvgDistPlot(v));
    }

    public static void triangulation() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);

        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findMatches(v);
        r.findPipeCenter(v);
        r.filterMatches(v);
        r.findTriples(v);
        r.findCameraPoses(v);
        ObjProducer.createObj(r.triangulation(v));
    }

}
