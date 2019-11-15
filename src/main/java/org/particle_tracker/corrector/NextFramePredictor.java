/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 *
 * @author grasp
 */
public class NextFramePredictor extends InstantOperation {
    static int headSize = 20;
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    BufferedImage currentFrame;
    BufferedImage nextFrame;
    Frame currentData;
    Frame nextData;
    
    public NextFramePredictor(BufferedImage currentFrame, BufferedImage nextFrame, Frame currentData) {
        this.currentData = currentData;
        this.currentFrame = currentFrame;
        this.nextFrame = nextFrame;
    }
    
    @Override
    void undo() {
    }

    @Override
    void redo() {
    }

    @Override
    void init() {
        System.out.println("Hola");
        Mat head = new Mat();
        for (Particle particle : currentData.particles) {
            Rect r = new Rect(particle.position.x - headSize / 2, particle.position.y - headSize / 2, headSize, headSize);
            System.out.println(r);
        }
    }
    
    
}
