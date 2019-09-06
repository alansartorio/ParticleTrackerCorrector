package org.particle_tracker.corrector;

import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

class FrameGrabber {
  final Java2DFrameConverter converterToBufferedImage;
  final FFmpegFrameGrabber grabber;

  public FrameGrabber(File videoFile) {
    grabber =  new FFmpegFrameGrabber(videoFile);
    converterToBufferedImage = new Java2DFrameConverter();
  }

  //returns framecount;
  public int start() {

    try {
      grabber.start();
      return grabber.getLengthInVideoFrames();
    }
    catch(org.bytedeco.javacv.FrameGrabber.Exception e) {
      e.printStackTrace();
    }
    return -1;
  }

  public void stop() {
    try {
      grabber.stop();
    } 
    catch(org.bytedeco.javacv.FrameGrabber.Exception e) {
      e.printStackTrace();
    }
  }

  public BufferedImage getFrame(int frameNumber) {
    try {
      grabber.setVideoFrameNumber(frameNumber);
      org.bytedeco.javacv.Frame frame = grabber.grab();
      if (frame == null) {
        return null;
      }
      BufferedImage bufferedImage = converterToBufferedImage.convert(frame);
      return bufferedImage;
    }
    catch(org.bytedeco.javacv.FrameGrabber.Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}