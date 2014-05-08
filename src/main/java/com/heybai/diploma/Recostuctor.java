package com.heybai.diploma;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_nonfree.*;

/**
 * Created by heybai on 5/8/14.
 */
public class Recostuctor {

    static { Loader.load(opencv_nonfree.class); }

    private static Logger LOG = LoggerFactory.getLogger(Recostuctor.class);

    public Video grab(String videoPath) throws FrameGrabber.Exception {
        LOG.info("Grab start");
        FrameGrabber grabber = OpenCVFrameGrabber.createDefault(videoPath);
        grabber.start();
        opencv_core.IplImage img;
        List<Frame> frames = new ArrayList<Frame>();
        while (true) {
            try {
                img = grabber.grab();
            } catch (Exception e) {
                break;
            }
            if (img == null) {
                break;
            } else {
                frames.add(new Frame(ImgUtils.copy(img)));
            }
        }
        grabber.stop();
        LOG.info("Grab stop with {} frames", frames.size());
        return new Video(frames);
    }

    public void removeDuplicates(Video video) {
        LOG.info("Remove dups start");
        List<Frame> filtered = new ArrayList<Frame>();
        filtered.add(video.get(0));
        for (int i = 1; i < video.nFrames(); ++i) {
            Frame f1 = filtered.get(filtered.size() - 1);
            Frame f2 = video.get(i);
            if (!compare(f1, f2)) {
                filtered.add(f2);
            }
        }
        video.setFrames(filtered);
        LOG.info("Remove dups stop with {} frames left", video.nFrames());
    }

    public boolean compare(Frame f1, Frame f2) {
        Mat m1 = f1.mat();
        Mat m2 = f2.mat();
        BytePointer p1;
        BytePointer p2;
        for (int i = 0; i < f1.width(); ++i) {
            for (int j = 0; j < f1.height(); ++j) {
                p1 = m1.ptr(j, i);
                p2 = m2.ptr(j, i);

                for (int k = 0; k < 3; ++k) {
                    if (p1.get(k) != p2.get(k)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void cut(Video video, int from, int to) {
        video.setFrames(video.getFrames().subList(from, to));
    }

    public void findFeatures(Video video, SiftConfig sc) {
        SIFT sift = new SIFT(sc.nfeatures, sc.nOctaveLayers, sc.contrastThreshold, sc.edgeThreshold, sc.sigma);

        for (Frame f : video.getFrames()) {
            KeyPoint keypoints = new KeyPoint();
            Mat descriptors = new Mat();
            sift.detect(f.mat(), keypoints);
            sift.compute(f.mat(), keypoints, descriptors);

            f.setFeatures(keypoints, descriptors);
        }
    }

    public void findMatches(Video video) {
        for (int i = 1; i < video.nFrames(); ++i) {
            findMatches(video.get(i - 1), video.get(i));
        }
    }

    public void findMatches(Frame f1, Frame f2) {
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

        f1.setMatches(ms);
    }

    public void filterMatches(Video video) {
        for (Frame frame : video.getFrames()) {
            filterMatches(frame);
        }
    }

    public void filterMatches(Frame frame) {
        List<Match> filtered = new ArrayList<Match>();
        for (Match m : frame.getMatches()) {
            if (isValid(frame, m)) {
                filtered.add(m);
            }
        }
        frame.setMatches(filtered);
    }

    public boolean isValid(Frame frame, Match match) {
        float p1Dist = MathUtils.dist(match.getP1(), frame.getPipeCenter());
        float p2Dist = MathUtils.dist(match.getP2(), frame.getPipeCenter());
        float dist = MathUtils.dist(match);

        double rd = ImgUtils.radius(frame) * 0.3;
        if (p1Dist < rd || p2Dist < rd) {
            return false;
        }

        if (dist > 120) {
            return false;
        }

        if (dist > 0.5) {
            float angel;
            double ang = Math.PI / 10;
            if (p1Dist > p2Dist) {
                angel = MathUtils.angel(match.getP1(), frame.getPipeCenter(), match.getP2());
                if (angel > ang) {
                    return false;
                }
            } else {
                angel = MathUtils.angel(match.getP2(), frame.getPipeCenter(), match.getP1());
                if (angel > ang) {
                    return false;
                }
            }
        }

        return true;
    }

}
