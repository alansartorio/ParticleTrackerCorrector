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
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author grasp
 */
public class NextFramePredictor extends InstantOperation {

    static int headSize = 32;
    static int delta = 32;

    Mat currentFrame;
    Mat nextFrame;
    Frame currentData;
    Frame nextData;

    List<Particle> addedParticles = new ArrayList<>();

    public NextFramePredictor(BufferedImage currentFrame, BufferedImage nextFrame, Frame currentData, Frame nextData) {
        System.out.println("HeadSize: "+headSize);
        System.out.println("Distance: "+delta);
        
        this.currentData = currentData;
        this.nextData = nextData;

        byte[] currentFramePixels = ((DataBufferByte) currentFrame.getRaster().getDataBuffer()).getData();
        this.currentFrame = new Mat(currentFrame.getHeight(), currentFrame.getWidth(), CvType.CV_8UC3);
        this.currentFrame.put(0, 0, currentFramePixels);
        //System.out.println("Current frame shape: " + this.currentFrame.size());

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
            if (headRect.x < 0
                    || headRect.y < 0
                    || headRect.x + headRect.width >= currentFrame.cols()
                    || headRect.y + headRect.height >= currentFrame.rows()) {
                continue;
            }

            Mat head = currentFrame.submat(headRect);

            Rect bigSquareRect = headRect.clone();

            bigSquareRect.x -= delta;
            bigSquareRect.y -= delta;
            bigSquareRect.width += 2 * delta;
            bigSquareRect.height += 2 * delta;

            if (bigSquareRect.x < 0 || bigSquareRect.y < 0
                    || bigSquareRect.x + bigSquareRect.width >= currentFrame.cols()
                    || bigSquareRect.y + bigSquareRect.height >= currentFrame.rows()) {
                continue;
            }

            //System.out.println(String.format("Particle: %s. Rect: %s", particle.position, bigSquareRect));

            Mat bigSquare = nextFrame.submat(bigSquareRect);

            Point newPosition = search(bigSquare, head);
            newPosition.translate(bigSquareRect.x, bigSquareRect.y);
            newPosition.translate(headSize / 2, headSize / 2);

            /*
            Point newPosition2 = search2(bigSquare, head);
            newPosition2.translate(bigSquareRect.x, bigSquareRect.y);
            newPosition2.translate(headSize / 2, headSize / 2);
             */
            
            //System.out.println(String.format("My method: %s\tOpenCV: %s", newPosition, newPosition2));

            if (newPosition != null && nextData.searchByIdentity(particle.identity) == null) {
                Particle newParticle = new Particle(newPosition, particle.identity);
                addedParticles.add(newParticle);
            }
        }

        redo();
        finish();
    }

    Point search2(Mat frame, Mat head) {
        Mat res = new Mat();
        Imgproc.matchTemplate(frame, head, res, Imgproc.TM_CCOEFF);
        MinMaxLocResult minMax = Core.minMaxLoc(res);
        //System.out.println("Matrix res: " + res.size());
        //System.out.println("Position: "+minMax.maxLoc.x);
        return new Point((int) minMax.maxLoc.x, (int) minMax.maxLoc.y);
    }

    Point search(Mat frame, Mat head) {

        HeadResult bestHead = null;

        Rect testRect = new Rect(0, 0, head.width(), head.height());

        for (testRect.x = 0; testRect.x < frame.width() - testRect.width; testRect.x++) {
            for (testRect.y = 0; testRect.y < frame.height() - testRect.height; testRect.y++) {

                Mat newHead = frame.submat(testRect);

                double diff = matDifference(head, newHead);

                if (bestHead == null || diff < bestHead.diff) {
                    bestHead = new HeadResult(newHead, testRect.clone(), diff);
                    //System.out.println("Best: " + testRect);
                }

            }
        }

        if (bestHead != null) {
            //System.out.println("Diff: " + bestHead.diff);
            //if (bestHead.diff <= 40*headSize*headSize){

            //Rect pos = bestHead.position;
            //Point newPos = new Point(pos.x + pos.width / 2, pos.y + pos.height / 2);
            //System.out.printf("Distance: %f\n", Point.distance(newPos.x, newPos.y, particle.position.x, particle.position.y));
            //nextData.addParticle(newParticle);
            return new Point(bestHead.position.x, bestHead.position.y);
            //}
        }
        return null;
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
