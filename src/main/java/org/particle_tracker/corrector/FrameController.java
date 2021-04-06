/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alan
 */
public class FrameController {

    private int videoFrame = 0;
    private int dataFrame = 0;

    public final int videoFrameCount;
    public final int dataFrameCount;
    public final int videoFramesPerDataFrame;

    private List<FrameChangeListener> frameChangeListeners = new ArrayList<>();

    public FrameController(int videoCount, boolean highFPS) {
        videoFramesPerDataFrame = highFPS ? 4 : 1;
        //videoFramesPerDataFrame = 1;
        
        this.videoFrameCount = videoCount;
        dataFrameCount = (videoFrameCount - 1) / videoFramesPerDataFrame + 1;
    }

    public void setVideoFrame(int frame) {
        if (frame < 0 || frame >= videoFrameCount) {
            return;
        }

        int oldDataFrame = dataFrame;
        int oldVideoFrame = videoFrame;

        videoFrame = frame;
        dataFrame = videoFrame / videoFramesPerDataFrame;

        if (oldDataFrame != dataFrame) {
            frameChangeListeners.forEach((l) -> l.onDataFrameChange(this));
        }
        if (oldVideoFrame != videoFrame) {
            frameChangeListeners.forEach((l) -> l.onVideoFrameChange(this));
        }
    }

    public int getVideoFrame() {
        return videoFrame;
    }

    public void setDataFrame(int frame) {
        setVideoFrame(frame * videoFramesPerDataFrame);
    }

    public int getDataFrame() {
        return dataFrame;
    }

    public void nextDataFrame() {
        setDataFrame(getDataFrame() + 1);
    }

    public void previousDataFrame() {
        setDataFrame(isInSync() ? getDataFrame() - 1 : getDataFrame());
    }

    public void nextVideoFrame() {
        setVideoFrame(getVideoFrame() + 1);
    }

    public void previousVideoFrame() {
        setVideoFrame(getVideoFrame() - 1);
    }

    public boolean isInSync() {
        return getDataFrame() * videoFramesPerDataFrame == getVideoFrame();
    }

    public void addFrameChangeListener(FrameChangeListener listener) {
        frameChangeListeners.add(listener);
    }

    public void removeFrameChangeListener(FrameChangeListener listener) {
        frameChangeListeners.remove(listener);
    }

    public void forceFrameChangeListenerCall() {
        frameChangeListeners.forEach((l) -> {
            l.onDataFrameChange(this);
            l.onVideoFrameChange(this);
        });
    }
}
