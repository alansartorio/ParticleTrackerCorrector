/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 *
 * @author grasp
 */
public class NextFramePredictor extends InstantOperation {

    static int headSize = 32;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    Mat currentFrame;
    Mat nextFrame;
    Frame currentData;
    Frame nextData;

    List<Particle> addedParticles = new ArrayList<>();

    public NextFramePredictor(BufferedImage currentFrame, BufferedImage nextFrame, Frame currentData, Frame nextData) {
        this.currentData = currentData;
        this.nextData = nextData;

        byte[] currentFramePixels = ((DataBufferByte) currentFrame.getRaster().getDataBuffer()).getData();
        this.currentFrame = new Mat(currentFrame.getHeight(), currentFrame.getWidth(), CvType.CV_8UC3);
        this.currentFrame.put(0, 0, currentFramePixels);
        System.out.println("Current frame shape: " + this.currentFrame.size());

        byte[] nextFramePixels = ((DataBufferByte) nextFrame.getRaster().getDataBuffer()).getData();
        this.nextFrame = new Mat(nextFrame.getHeight(), nextFrame.getWidth(), CvType.CV_8UC3);
        this.nextFrame.put(0, 0, nextFramePixels);
    }

    @Override
    void undo() {
        addedParticles.forEach(p -> nextData.removeParticle(p));
    }

    @Override
    void redo() {
        addedParticles.forEach(p -> nextData.addParticle(p));
    }

    static double matDifference(Mat a, Mat b) {

        Mat sub = new Mat();

        Core.absdiff(a, b, sub);
        Scalar diffs = Core.sumElems(sub);

        double diff = 0;

        for (double val : diffs.val) {
            diff += val;
        }

        return diff;
    }

    @Override
    void init() {
        for (Particle particle : currentData.particles) {
            Rect headRect = new Rect(particle.position.x - headSize / 2, particle.position.y - headSize / 2, headSize, headSize);
            //System.out.println("Original head rect: " + headRect);

            //Si el rectangulo de la cabeza se encuentra afuera del frame, ignorar la particula
            if (headRect.x < 0 || 
                    headRect.y < 0 || 
                    headRect.x + headRect.width >= currentFrame.cols() || 
                    headRect.y + headRect.height >= currentFrame.rows()) {
                continue;
            }

            Mat head = currentFrame.submat(headRect);

            HeadResult bestHead = null;

            final int delta = 80;
            for (int dx = -delta; dx <= delta; dx++) {
                for (int dy = -delta; dy <= delta; dy++) {
                    Rect newHeadRect = new Rect(headRect.x + dx, headRect.y + dy, headRect.width, headRect.height);
                    
                    if (newHeadRect.x < 0 || 
                            newHeadRect.y < 0 || 
                            newHeadRect.x + newHeadRect.width >= nextFrame.cols() || 
                            newHeadRect.y + newHeadRect.height >= nextFrame.rows()) {
                        continue;
                    }
                    
                    Mat newHead = nextFrame.submat(newHeadRect);

                    double diff = matDifference(head, newHead);

                    if (bestHead == null || diff < bestHead.diff) {
                        bestHead = new HeadResult(newHead, newHeadRect, diff);
                    }

                }
            }

            if (bestHead != null) {
                System.out.println("Diff: " + bestHead.diff);
                if (bestHead.diff <= 40*headSize*headSize){
                    
                    Rect pos = bestHead.position;
                    Point newPos = new Point(pos.x + pos.width / 2, pos.y + pos.height / 2);
                    System.out.printf("Distance: %f\n", Point.distance(newPos.x, newPos.y, particle.position.x, particle.position.y));
                    //nextData.addParticle(newParticle);

                    if (nextData.searchByIdentity(particle.identity) == null) {
                        Particle newParticle = new Particle(newPos, particle.identity);
                        addedParticles.add(newParticle);
                    }
                }
            }

        }

        redo();
        finish();
    }
}

class HeadResult {

    double diff;
    Mat head;
    Rect position;

    public HeadResult(Mat head, Rect position, double diff) {
        this.diff = diff;
        this.head = head;
        this.position = position;
    }
}
