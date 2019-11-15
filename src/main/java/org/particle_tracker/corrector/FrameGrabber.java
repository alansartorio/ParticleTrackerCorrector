package org.particle_tracker.corrector;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.videoio.*;

class FrameGrabber {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    private final VideoCapture videoCapture;
    final int frameCount;
    final double frameRate;

    public FrameGrabber(File videoFile) {
        videoCapture = new VideoCapture(videoFile.getAbsolutePath());
        
        frameCount = (int)videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        frameRate = videoCapture.get(Videoio.CAP_PROP_FPS);
    }

    public void release() {
        videoCapture.release();
    }

    
    private static BufferedImage matToBufferedImage(Mat frame) {
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);

        return image;
    }
    
    public BufferedImage getFrame(int frameNumber) {
        double frameTime = 1000.0 * frameNumber / frameRate;
        videoCapture.set(Videoio.CAP_PROP_POS_MSEC, frameTime);
        Mat image = new Mat();
        if (videoCapture.read(image))
            return matToBufferedImage(image);
        return null;
    }
}