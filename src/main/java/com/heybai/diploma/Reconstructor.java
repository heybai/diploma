package com.heybai.diploma;

import com.heybai.diploma.lma.LMA;
import com.heybai.diploma.lma.LMAMultiDimFunction;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_nonfree;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
public class Reconstructor {

    static { Loader.load(opencv_nonfree.class); }

    private static Logger LOG = LoggerFactory.getLogger(Reconstructor.class);

    public Video grab(String videoPath) throws FrameGrabber.Exception {
        FrameGrabber grabber = OpenCVFrameGrabber.createDefault(videoPath);
        grabber.start();
        opencv_core.IplImage img;
        List<Frame> frames = new ArrayList<Frame>();
        int idx = 0;
        while (true) {
            try {
                img = grabber.grab();
            } catch (Exception e) {
                break;
            }
            if (img == null) {
                break;
            } else {
                frames.add(new Frame(idx++, ImgUtils.copy(img)));
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
            frames.add(new Frame(i, img));
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
        for (int i = 0; i < video.nFrames(); ++i) {
            video.get(i).setIdx(i);
        }
        LOG.info("Duplicate frames removed, {} left", video.nFrames());
    }

    public void removeDuplicatesParallel(final Video video) {
        for (Frame f : video.getFrames()) {
            f.setUnique(false);
        }
        video.getFrames().parallelStream()
                .forEach(new Consumer<Frame>() {
                    @Override
                    public void accept(Frame frame) {
                        int idx = frame.getIdx();
                        if (idx == 0 || !compare(frame, video.get(idx - 1))) {
                            frame.setUnique(true);
                        }
                    }
                });
        List<Frame> filtered = new ArrayList<Frame>();
        for (Frame f : video.getFrames()) {
            if (f.isUnique()) {
                f.setIdx(filtered.size());
                filtered.add(f);
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
//        System.out.println(count);
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

    public void findPipeCenterParallel(Video video) {
        video.getFrames().parallelStream()
                .forEach(new Consumer<Frame>() {
                    @Override
                    public void accept(Frame f) {
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
                            f.setPipeCenter(new Point(center.x(), center.y()));
                        }
                    }
                });

        float xSum = 0;
        float ySum = 0;
        float count = 0;
        for (Frame f : video.getFrames()) {
            xSum += f.getPipeCenter().getX();
            ySum += f.getPipeCenter().getY();
            ++count;
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

    public void findFeaturesParallel(Video video, final SiftConfig sc) {
        video.getFrames().parallelStream()
                .forEach(new Consumer<Frame>() {
                    @Override
                    public void accept(Frame f) {
                        SIFT sift = new SIFT(sc.nfeatures, sc.nOctaveLayers,
                                sc.contrastThreshold, sc.edgeThreshold, sc.sigma);
                        KeyPoint keypoints = new KeyPoint();
                        Mat descriptors = new Mat();
                        sift.detect(f.mat(), keypoints);
                        sift.compute(f.mat(), keypoints, descriptors);
                        f.setFeatures(keypoints, descriptors);
                    }
                });
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

    public void findMatchesParallel(final Video video) {
        video.getFrames().parallelStream()
                .filter(f -> f.getIdx() < video.nFrames() - 1)
                .forEach(f -> findMatches(f, video.get(f.getIdx() + 1)));
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

    public void filterMatchesParallel(Video video) {
        video.getFrames().parallelStream()
                .filter(f -> f.getIdx() < video.nFrames() - 1)
                .forEach(f -> filterMatches(f));
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
//            cvCircle(dst.img(), ImgUtils.point(src.getPipeCenter()), 20, CvScalar.RED, 1, CV_AA, 0);
            cvSaveImage(String.format("frame-matches-%s.jpg", i), dst.img());
        }
        LOG.info("Frames with matches & pipe centers written to files");
    }

    public void findTriples(Video video) {
        double e = 0.1;
        for (int i = 0; i < video.nFrames() - 2; ++i) {
            List<Match> ms1 = video.get(i).getMatches();
            List<Match> ms2 = video.get(i + 1).getMatches();
            List<Triple> triples = new ArrayList<Triple>();
            for (Match m1 : ms1) {
                for (Match m2 : ms2) {
                    if (MathUtils.dist(m1.getP2(), m2.getP1()) < e) {
                        triples.add(new Triple(m1.getP1(), m1.getP2(), m2.getP2()));
                        break;
                    }
                }
            }
            video.get(i).setTriples(triples);
        }
        LOG.info("Triples found");
    }

    public void findTriplesParallel(Video video) {
        double e = 0.1;
        video.getFrames().parallelStream()
                .filter(f -> f.getIdx() < video.nFrames() - 2)
                .forEach(new Consumer<Frame>() {
                    @Override
                    public void accept(Frame f) {
                        List<Match> ms1 = f.getMatches();
                        List<Match> ms2 = video.get(f.getIdx() + 1).getMatches();
                        List<Triple> triples = new ArrayList<>();
                        for (Match m1 : ms1) {
                            for (Match m2 : ms2) {
                                if (MathUtils.dist(m1.getP2(), m2.getP1()) < e) {
                                    triples.add(new Triple(m1.getP1(), m1.getP2(), m2.getP2()));
                                    break;
                                }
                            }
                        }
                        f.setTriples(triples);
                    }
                });
        LOG.info("Triples found");
    }

    public void outputTriples(Video video) {
        for (int i = 0; i < video.nFrames() - 2; ++i) {
            Frame src = video.get(i);
            Frame dst = ImgUtils.copy(src);
            for (Triple t : src.getTriples()) {
                cvLine(dst.img(), ImgUtils.point(t.getP1()), ImgUtils.point(t.getP2()), CvScalar.RED);
                cvLine(dst.img(), ImgUtils.point(t.getP2()), ImgUtils.point(t.getP3()), CvScalar.GREEN);
                cvCircle(dst.img(), ImgUtils.point(t.getP2()), 2, CvScalar.YELLOW, 1, CV_AA, 0);
            }
            cvCircle(dst.img(), ImgUtils.point(src.getPipeCenter()), 20, CvScalar.RED, 1, CV_AA, 0);
            cvSaveImage(String.format("frame-matches-%s.jpg", i), dst.img());
        }
        LOG.info("Frames with matches & pipe centers written to files");
    }

    public List<Point> triplesPlot(Video video) {
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < video.nFrames() - 2; ++i) {
            plot.add(new Point(i, video.get(i).nTriples()));
        }
        return plot;
    }

    public void printTriplesForLevenberg(Video video) {
        Frame f1 = video.get(12);
        Point center = ImgUtils.center(video);
        for (Triple t : f1.getTriples()) {
            System.out.println(String.format("{0, %.7f, %.7f, %.7f},",
                    MathUtils.dist(center, t.getP1()),
                    MathUtils.dist(center, t.getP2()),
                    MathUtils.dist(center, t.getP3())
            ));
        }
    }

    public void findCameraPoses(Video video, final int equationType) {
        // One penguin :)
        video.get(0).setDz(1);

        final Point c = ImgUtils.center(video);
        final double f = f();

        for (int i = 0; i < video.nFrames() - 2; ++i) {
            Frame fr = video.get(i);
            video.get(i + 1).setDz(findDz(fr, c, f, equationType));
        }

        LOG.info("Camera poses found");
    }

    public void findCameraPosesParallel(Video video, final int equationType) {
        // One penguin :)
        video.get(0).setDz(1);

        final Point c = ImgUtils.center(video);
        final double f = f();

        video.getFrames().parallelStream()
                .filter(fr -> fr.getIdx() > 0 && fr.getIdx() < video.nFrames() - 1)
                .forEach(fr -> fr.setDz(findDz(video.get(fr.getIdx() - 1), c, f, equationType)));

        LOG.info("Camera poses found");
    }

    private double findDz(Frame fr, Point c, double f, int equationType) {
        // y=0 z1 r1 r2 r3
        double data[][] = new double[fr.nTriples()][5];
        for (int t = 0; t < fr.nTriples(); ++t) {
            data[t][0] = 0;
            data[t][1] = fr.getDz();
            data[t][2] = MathUtils.dist(c, fr.getTriples().get(t).getP1());
            data[t][3] = MathUtils.dist(c, fr.getTriples().get(t).getP2());
            data[t][4] = MathUtils.dist(c, fr.getTriples().get(t).getP3());
        }

        LMA lma = new LMA(
                new LMAMultiDimFunction() {

                    private double ang(double r) {
                        switch (equationType) {
                            case 0: return Math.atan(r / f);
                            case 1: return 2.0 * Math.atan(r / f / 2.0);
                            case 2: return r / f;
                            case 3: return Math.asin(r / f);
                            case 4: return 2.0 * Math.asin(r / f / 2.0);
                        }
                        throw new IllegalArgumentException("Unknown equation type " + equationType);
                    }

                    private double koef(double r1, double r2) {
                        return Math.tan(ang(r1)) * Math.tan(ang(r2)) / (Math.tan(ang(r1)) - Math.tan(ang(r2)));
                    }

                    private double pow(double a) {
                        return a * a;
                    }

                    @Override
                    public double getY(double[] x, double[] a) {
                        double z1 = x[0];
                        double r1 = x[1];
                        double r2 = x[2];
                        double r3 = x[3];

                        double z2 = a[0];

//                            return z1 * koef(r1, r2) - z2 * koef(r2, r3);
                        return pow(z1 * koef(r1, r2) - z2 * koef(r2, r3));
                    }

                    @Override
                    public double getPartialDerivate(double[] x, double[] a, int parameterIndex) {
                        double z1 = x[0];
                        double r1 = x[1];
                        double r2 = x[2];
                        double r3 = x[3];

                        double z2 = a[0];

                        switch (parameterIndex) {
                            // z2
                            case 0:
//                                    return - koef(r2, r3);
                                return - 2 * koef(r2, r3) * (z1 * koef(r1, r2) - z2 * koef(r2, r3));
                        }

                        throw new RuntimeException("No such parameter index: " + parameterIndex);
                    }
                },
                // z2
                new double[] {1},
                data
        );
        lma.fit();

        return lma.parameters[0];
    }

    public List<Point> cameraPosesPlot(Video video) {
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < video.nFrames() - 1; ++i) {
            plot.add(new Point(i, (float) video.get(i).getDz()));
        }
        return plot;
    }

    public List<Point> mathchesAvgDistPlot(Video video) {
        List<Point> plot = new ArrayList<Point>();
        for (int i = 0; i < video.nFrames() - 1; ++i) {
            double sum = 0;
            for (Match m : video.get(i).getMatches()) {
                sum += MathUtils.dist(m.getP1(), m.getP2());
            }
            plot.add(new Point(i, (float) (sum / (double) video.get(i).nMatches())));
        }
        return plot;
    }

    public List<Point3D> triangulation(Video video, final int equationType) {
        List<Point3D> res = new ArrayList<Point3D>();

        double cameraZ = 10;
        Point center = ImgUtils.center(video);

        for (int i = 0; i < video.nFrames() - 1; ++i) {
            Frame fr = video.get(i);
            for (Match m : fr.getMatches()) {
                double r1 = MathUtils.dist(center, m.getP1());
                double r2 = MathUtils.dist(center, m.getP2());

                double dz = fr.getDz() * Math.tan(ang(r1, equationType)) / (Math.tan(ang(r1, equationType)) - Math.tan(ang(r2, equationType)));
                double r = dz * Math.tan(ang(r2, equationType));

                double koef = r / r1;
                double x = m.getP1().getX() - center.getX();
                double y = m.getP1().getY() - center.getY();

                res.add(new Point3D(x * koef, y * koef, cameraZ - dz));
            }

            cameraZ += fr.getDz();
        }

        LOG.info("Triangulation done with {} points", res.size());
        return res;
    }

    public List<Point3D> triangulationParallel(Video video, final int equationType) {
        Point center = ImgUtils.center(video);
        for (int i = 0; i < video.nFrames(); ++i) {
            video.get(i).setZ(i == 0 ? 10 : video.get(i - 1).getZ() + video.get(i - 1).getDz());
        }

        List<Point3D> points = video.getFrames().parallelStream()
                .filter(f -> f.getIdx() < video.nFrames() - 1)
                .map(new Function<Frame, List<Point3D>>() {
                    @Override
                    public List<Point3D> apply(Frame fr) {
                        List<Point3D> res = new ArrayList<>();
                        for (Match m : fr.getMatches()) {
                            double r1 = MathUtils.dist(center, m.getP1());
                            double r2 = MathUtils.dist(center, m.getP2());

                            double dz = fr.getDz() * Math.tan(ang(r1, equationType)) / (Math.tan(ang(r1, equationType)) - Math.tan(ang(r2, equationType)));
                            double r = dz * Math.tan(ang(r2, equationType));

                            double koef = r / r1;
                            double x = m.getP1().getX() - center.getX();
                            double y = m.getP1().getY() - center.getY();

                            res.add(new Point3D(x * koef, y * koef, fr.getZ()));
                        }
                        return res;
                    }
                })
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());

        LOG.info("Triangulation done with {} points", points.size());
        return points;
    }



    public void mmm(Video video) {
        Frame f1 = video.get(0);

        Point center = ImgUtils.center(video);
        double f = 1.178;
//        double k = 206;
        double k = 320 / (1.178 * Math.sin(Math.PI * 5 / 9));
        LOG.info("k = " + k);
        double del = 0.6;

        List<R> rs = new ArrayList<R>();
        for (Match m : f1.getMatches()) {
            Point a = m.getP1();
            Point b = m.getP2();

            double a_rad = MathUtils.dist(center, a);
            double b_rad = MathUtils.dist(center, b);

//            double a_ang = Math.asin(a_rad / (f * k));
//            double b_ang = Math.asin(b_rad / (f * k));
            double a_ang = a_rad / (k * f);
            double b_ang = b_rad / (k * f);

            double h = (del + Math.tan(b_ang)) / (1.0 - (Math.tan(b_ang) / Math.tan(a_ang)));
            double zz = (del + Math.tan(b_ang)) / (Math.tan(a_ang) - Math.tan(b_ang));

//            System.out.println(String.format("a_rad=%.5f, a_ang=%.5f, h=%.5f, zz=%.5f", a_rad, a_ang, h, zz));
            rs.add(new R(a_rad, a_ang, h, zz));
        }

        Collections.sort(rs, new Comparator<R>() {
            @Override
            public int compare(R r, R r2) {
                return r.h < r2.h ? -1 : 1;
//                return r.zz < r2.zz ? -1 : 1;
//                return r.a_rad < r2.a_rad ? -1 : 1;
            }
        });
//        for (R r : rs) {
//            System.out.println(r);
//        }
//        List<Point> plot = new ArrayList<Point>();
//        for (int i = 0; i < rs.size(); ++i) {
//            plot.add(new Point(i, (float) rs.get(i).h));
//        }
//        MathPlot.plot("Heights", "i", "height", plot);
    }

    private static boolean printF = true;

    private double f() {
        double focus = 1.178;
        double k = 320 / (focus * Math.sin(Math.PI * 5 / 11));
        double f = focus * k;
        f = 275;
        if (printF) {
            printF = false;
            LOG.info("focus={}, k={}, f={}", focus, k, f);
        }
        return f;
    }

    private double ang(double r, int equationType) {
        double f = f();
        switch (equationType) {
            case 0: return Math.atan(r / f);
            case 1: return 2.0 * Math.atan(r / f / 2.0);
            case 2: return r / f;
            case 3: return Math.asin(r / f);
            case 4: return 2.0 * Math.asin(r / f / 2.0);
        }
        throw new IllegalArgumentException("Unknown equation type " + equationType);
    }

    private class R {
        double a_rad;
        double a_ang;
        double h;
        double zz;

        private R(double a_rad, double a_ang, double h, double zz) {
            this.a_rad = a_rad;
            this.a_ang = a_ang;
            this.h = h;
            this.zz = zz;
        }

        @Override
        public String toString() {
            return String.format("a_rad=%.5f, a_ang=%.5f, h=%.5f, zz=%.5f", a_rad, a_ang, h, zz);
        }
    }

}
