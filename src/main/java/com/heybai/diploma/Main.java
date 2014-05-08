package com.heybai.diploma;

import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;

/**
 * Created by heybai on 5/7/14.
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    // Video file path
    static String videoPath = "/Volumes/Macintosh HD/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/materials/e1.avi";

    public static void main(String[] args) throws FrameGrabber.Exception, InterruptedException {
//        featuresStats();
        matchesStats();
    }

    public static void featuresStats() throws FrameGrabber.Exception {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);
        r.removeDuplicates(v);
        r.findFeatures(v, new SiftConfig(0, 3, 0.03, 10, 1.6));
        r.outputFeatures(v);
        MathPlot.plot("Features stats", "frame", "nFeatures", r.featuresPlot(v));
    }

    public static void matchesStats() throws FrameGrabber.Exception, InterruptedException {
        Recostuctor r = new Recostuctor();

        Video v = r.grab(videoPath);
        r.removeDuplicates(v);
        // best: 0, 3, 0.02, 10, 1.6
        r.findFeatures(v, new SiftConfig(0, 3, 0.02, 10, 1.6));
        r.findPipeCenter(v);
        r.findMatches(v);
        r.filterMatches(v);
        r.outputMatches(v);
        MathPlot.plot("Matches stats", "frame", "nMatches", r.matchesPlot(v));
    }

//    public static void featuresStats() throws FrameGrabber.Exception, InterruptedException {
//        LOG.info("Start");
//
//        // Parse video to Frames
//        Video video = new VideoGrabber().grab(videoPath);
//        LOG.info("Video parsed. Number of frames = {}", video.nFrames());
//
//        // Cut the video
//        video = new Video(video.getFrames().subList(30, 70));
//        LOG.info("Video cropped & now has {} frames", video.nFrames());
//
//        // Extract Features
//        FeaturesExtractor featuresExtractor = new FeaturesExtractor();
//        List<Features> featureses = featuresExtractor.extract(video,
//                new FeaturesExtractor.SiftConfig(0, 3, 0.08, 10, 1.6));
//        LOG.info("Features extracted");
//
//        // Plot stats
//        new MathPlot().features(featureses);
//
//        // Min & max features
//        int iMin = 0;
//        int iMax = 0;
//        for (int i = 0; i < video.nFrames(); ++i) {
//            if (featureses.get(i).nFeatures() < featureses.get(iMin).nFeatures()) {
//                iMin = i;
//            }
//            if (featureses.get(i).nFeatures() > featureses.get(iMax).nFeatures()) {
//                iMax = i;
//            }
//        }
//        Frame min = ImgUtils.copy(video.get(iMin));
//        featuresExtractor.apply(video.get(iMin), featureses.get(iMin), min);
//        Frame max = ImgUtils.copy(video.get(iMax));
//        featuresExtractor.apply(video.get(iMax), featureses.get(iMax), max);
//
//        new Canvas().frames(min, "Min", max, "Max");
//    }

    public static void matches() throws FrameGrabber.Exception, InterruptedException {
//        LOG.info("Start");
//
////        Point c = new Point(0, 0);
////        Point p1 = new Point(200, 300);
////        Point p2 = new Point(-200, 300);
////        System.out.println(MathUtils.angel(p2, c, p1));
//
//        // Parse video to Frames
//        Video video = new VideoGrabber().grab(videoPath);
//        LOG.info("Video parsed. Number of frames = {}", video.nFrames());
//
//        // Cut the video
//        video = new Video(video.getFrames().subList(30, 70));
//        LOG.info("Video cropped & now has {} frames", video.nFrames());
//
//        // Extract Features
//        FeaturesExtractor featuresExtractor = new FeaturesExtractor();
//        List<Features> featureses = featuresExtractor.extract(video,
//                new FeaturesExtractor.SiftConfig(0, 3, 0.01, 10, 1.6));
//        LOG.info("Features extracted");
//
//        // Match
//        Matcher matcher = new Matcher();
//        List<Matches> matcheses = matcher.match(featureses);
//        LOG.info("Matches found");
//
//        // Filter Matches
//        List<Matches> filtered = matcher.filter(matcheses, ImgUtils.center(video), ImgUtils.radius(video));
//        LOG.info("Matches filtered");
//
//        // Plot stats
//        new MathPlot().matches(filtered);

//        // Images with matches;
//        for (int i = 0; i < filtered.size(); ++i) {
//            Frame f = ImgUtils.copy(video.get(i));
//            matcher.apply(f, filtered.get(i));
//            Frame f2 = ImgUtils.copy(video.get(i + 1));
//            matcher.apply(f2, filtered.get(i));
//            new Canvas().frames(f, String.format("Frame #%s", i), f2, String.format("Frame #%s", i + 1));
//        }
    }

}
