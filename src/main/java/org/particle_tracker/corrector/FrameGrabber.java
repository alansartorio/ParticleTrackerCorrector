package org.particle_tracker.corrector;

import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

class FrameGrabber {

    final Java2DFrameConverter converterToBufferedImage;
    final FFmpegFrameGrabber grabber;

    public FrameGrabber(File videoFile) {
        grabber = new FFmpegFrameGrabber(videoFile);
        converterToBufferedImage = new Java2DFrameConverter();
    }

    //returns framecount;
    public int start() throws org.bytedeco.javacv.FrameGrabber.Exception {
        grabber.start();
        return grabber.getLengthInVideoFrames();
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
