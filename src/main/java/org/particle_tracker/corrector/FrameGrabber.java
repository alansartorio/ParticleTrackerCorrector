package org.particle_tracker.corrector;

import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

class FrameGrabber {

    private final Java2DFrameConverter converterToBufferedImage;
    private final FFmpegFrameGrabber grabber;
    final int frameCount;
    final double frameRate;

    public FrameGrabber(File videoFile) throws org.bytedeco.javacv.FrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber(videoFile);
        converterToBufferedImage = new Java2DFrameConverter();
        start();
        frameCount = grabber.getLengthInVideoFrames();
        frameRate = grabber.getFrameRate();
    }

    //returns framecount;
    private final void start() throws org.bytedeco.javacv.FrameGrabber.Exception {
        grabber.start();
    }

    public void stop() throws org.bytedeco.javacv.FrameGrabber.Exception {
        grabber.stop();
    }

    public BufferedImage getFrame(int frameNumber) throws org.bytedeco.javacv.FrameGrabber.Exception {
        grabber.setVideoFrameNumber(frameNumber);
        org.bytedeco.javacv.Frame frame = grabber.grab();
        if (frame == null) {
            return null;
        }
        BufferedImage bufferedImage = converterToBufferedImage.convert(frame);
        return bufferedImage;
    }
}
