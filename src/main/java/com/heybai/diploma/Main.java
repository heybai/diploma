package com.heybai.diploma;

import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by heybai on 5/7/14.
 */
public class Main {

    private static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws FrameGrabber.Exception {
        LOG.info("Start");

        // Video file path
        String videoPath = "/Volumes/Macintosh HD/Users/heybai/Documents/yandex.disk/Documents/University/Diploma/materials/e1.avi";

        // Parse video to Frames
        Video video = new VideoGrabber().grab(videoPath);
        LOG.info("Video parsed. Number of frames = {}", video.nFrames());
    }

}
