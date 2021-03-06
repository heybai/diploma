package com.heybai.diploma.model;

import com.heybai.diploma.model.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class Video {

    private List<Frame> frames = new ArrayList<Frame>();

    public Video(List<Frame> frames) {
        this.frames = frames;
    }

    public int nFrames() {
        return frames.size();
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public Frame get(int i) {
        return frames.get(i);
    }
}
