package com.heybai.diploma;

import org.bytedeco.javacv.CanvasFrame;

/**
 * Created by heybai on 5/7/14.
 */
public class Canvas {

    public void frame(Frame frame, String name) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame(name);
        cf1.showImage(frame.img());
        cf1.waitKey();
        cf1.dispose();
    }

    public void frames(Frame f1, String n1, Frame f2, String n2) throws InterruptedException {
        CanvasFrame cf1 = new CanvasFrame(n1);
        CanvasFrame cf2 = new CanvasFrame(n2);
        cf1.showImage(f1.img());
        cf2.showImage(f2.img());
        cf1.waitKey();
        cf1.dispose();
        cf2.dispose();
    }

}
