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
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvCircle;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_MEDIAN;
import static org.bytedeco.javacpp.opencv_imgproc.cvSmooth;
import static org.bytedeco.javacpp.opencv_nonfree.*;

/**
 * Created by heybai on 5/8/14.
 */
public class Recostuctor {

    static { Loader.load(opencv_nonfree.class); }

    private static Logger LOG = LoggerFactory.getLogger(Recostuctor.class);

    public Video grab(String videoPath) throws FrameGrabber.Exception {
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
        LOG.info("Video grabbed with {} frames", frames.size());
        return new Video(frames);
    }

    public Video grab(String... frame) {
        List<Frame> frames = new ArrayList<Frame>();
        for (int i = 0; i < frame.length; ++i) {
            IplImage img = cvLoadImage(frame[i]);
            frames.add(new Frame(img));
        }
        return new Video(frames);
    }

    public void removeDuplicates(Video video) {
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
        LOG.info("Duplicate frames removed, {} left", video.nFrames());
    }

    public boolean compare(Frame f1, Frame f2) {
        Mat m1 = f1.mat();
        Mat m2 = f2.mat();
        BytePointer p1;
        BytePointer p2;
        int count = 0;
        for (int i = 50; i < f1.width() - 50; ++i) {
            for (int j = 50; j < f1.height() - 50; ++j) {
                p1 = m1.ptr(j, i);
                p2 = m2.ptr(j, i);

                for (int k = 0; k < 3; ++k) {
                    if (p1.get(k) != p2.get(k)) {
                        ++count;
                        break;
                    }
                }
            }
        }
        if (count > 1000) {
            return false;
        }
        return true;
    }

    public void cut(Video video, int from, int to) {
        video.setFrames(video.getFrames().subList(from, to));
    }

    public void findPipeCenter(Video video) throws InterruptedException {
        float xSum = 0;
        float ySum = 0;
        float count = 0;
        for (Frame f : video.getFrames()) {
            // color range of black like color
            CvScalar min = cvScalar(0, 0, 0, 0);
            CvScalar max= cvScalar(30, 30, 30, 0);
            // create binary image of original size
            IplImage bin = cvCreateImage(cvGetSize(f.img()), 8, 1);
            // apply thresholding
            cvInRangeS(f.img(), min, max, bin);
//            Canvas.img(bin);
            // crop center
            int w = 100;
            int h = 200;
            opencv_core.IplImage binCropped = cvCreateImage(cvGetSize(f.img()), 8, 1);
            cvSetImageROI(bin, cvRect(f.img().width() / 2 - w / 2, f.img().height() / 2 - h / 2, w, h));
            cvSetImageROI(binCropped, cvRect(f.img().width() / 2 - w / 2, f.img().height() / 2 - h / 2, w, h));
            cvCopy(bin, binCropped);
            cvResetImageROI(bin);
            cvResetImageROI(binCropped);
//            Canvas.img(binCropped);
            // ex
            IplConvKernel kernel = cvCreateStructuringElementEx(7, 7, 3, 3, CV_SHAPE_ELLIPSE);
            cvMorphologyEx(binCropped, binCropped, null, kernel, CV_MOP_OPEN);
            // smooth filter - median
            cvSmooth(binCropped, binCropped, CV_MEDIAN, 5, 0, 0, 0);
//            Canvas.img(binCropped);
            // find circles
            CvMemStorage mem = CvMemStorage.create();
            CvSeq circles = cvHoughCircles(binCropped, mem, CV_HOUGH_GRADIENT, 2,
                    (double) binCropped.height() / 1, 1, 1, 1, 300);
            if (circles.total() > 0) {
                CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, 0));
                CvPoint center = cvPoint((int) circle.x(), (int) circle.y());
                xSum += center.x();
                ySum += center.y();
                ++count;
            }
        }

        Point center = new Point(xSum / count, ySum / count);
        for (Frame f : video.getFrames()) {
            f.setPipeCenter(center);
        }

        LOG.info("Pipe center was detected at {}, while image center is at {}", center, ImgUtils.center(video));
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
        LOG.info("Features found");
    }

    public List<Point> featuresPlot(Video video) {
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < video.nFrames(); ++i) {
            plot.add(new Point(i, video.get(i).nFeatures()));
        }
        LOG.info("Features plot created");
        return plot;
    }

    public void outputFeatures(Video video) {
        for (int i = 0; i < video.nFrames(); ++i) {
            Frame src = video.get(i);
            Frame dst = ImgUtils.copy(src);
            drawKeypoints(src.mat(), src.getKeyPoints(), dst.mat());
            cvSaveImage(String.format("frame-features-%s.jpg", i), dst.img());
        }
        LOG.info("Frames with matches & pipe centers written to files");
    }

    public void findMatches(Video video) {
        for (int i = 1; i < video.nFrames(); ++i) {
            findMatches(video.get(i - 1), video.get(i));
        }
        LOG.info("Matches found");
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
        for (int i = 0; i < video.nFrames() - 1; ++i) {
            filterMatches(video.get(i));
        }
        LOG.info("Matches filtered");
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

        // not in center
        double rd = ImgUtils.radius(frame) * 0.3;
        if (p1Dist < rd || p2Dist < rd) {
            return false;
        }

        // not far away
        if (dist < 10 || dist > 60) {
            return false;
        }

        // must be directed to center
        if (p1Dist < p2Dist) {
            return false;
        }

        // not water
        Point c = frame.getPipeCenter();
        Point p = new Point(c.getX(), c.getY() + 100);
        double wt = Math.PI / 5;
        if (MathUtils.angel(c, p, match.getP1()) < wt || MathUtils.angel(c, p, match.getP2()) < wt) {
            return false;
        }

        // directed to center of the pipe
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

    public List<Point> matchesPlot(Video video) {
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < video.nFrames() - 1; ++i) {
            plot.add(new Point(i, video.get(i).nMatches()));
        }
        LOG.info("Matches plot");
        return plot;
    }

    public void outputMatches(Video video) {
        for (int i = 0; i < video.nFrames() - 1; ++i) {
            Frame src = video.get(i);
            Frame dst = ImgUtils.copy(src);
            for (Match m : src.getMatches()) {
                cvLine(dst.img(), ImgUtils.point(m.getP1()), ImgUtils.point(m.getP2()), CvScalar.RED);
            }
            cvCircle(dst.img(), ImgUtils.point(src.getPipeCenter()), 20, CvScalar.RED, 1, CV_AA, 0);
            cvSaveImage(String.format("frame-matches-%s.jpg", i), dst.img());
        }
        LOG.info("Frames with matches & pipe centers written to files");
    }

    public void mmm(Video video) {
        Frame f1 = video.get(0);

        Point center = ImgUtils.center(video);
        double k = 300 / (Math.PI / 2.1);
        double del = 20;

        List<Double> heights = new ArrayList<Double>();
        for (Match m : f1.getMatches()) {
            Point a = m.getP1();
            Point b = m.getP2();

            double a_rad = MathUtils.dist(center, a);
            double b_rad = MathUtils.dist(center, b);

            double a_ang = a_rad / k;
            double b_ang =b_rad / k;

            double h = (del + Math.tan(b_ang)) / (1.0 - (Math.tan(b_ang) / Math.tan(a_ang)));
            double zz = (del + Math.tan(b_ang)) / (Math.tan(a_ang) - Math.tan(b_ang));

            heights.add(h);
//            System.out.println(String.format("a_rad=%.5f, a_ang=%.5f, h=%.5f, zz=%.5f", a_rad, a_ang, h, zz));
        }

        Collections.sort(heights);
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < heights.size(); ++i) {
            plot.add(new Point(i, heights.get(i).floatValue()));
        }
        MathPlot.plot("Heights", "i", "height", plot);
    }

}
