/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

/**
 *
 * @author grasp
 */
public class ZoomController {

    Point2D center;
    float scale = 1;
    static float scaleIncrement = 1.2f;
    static float scaleMovement = 0.2f;
    private List<ZoomChangeListener> listeners = new ArrayList<>();
    private Dimension windowSize;
    private Point2D dragStartingPosition;

    public ZoomController(Dimension windowSize, Point center) {
        this.windowSize = windowSize;
        this.center = center;
    }

    void setWindowSize(Dimension windowSize) {
        this.windowSize = windowSize;
        callListeners();
    }
    
    void startDrag(Point mousePos) {
        dragStartingPosition = screenToImage(mousePos);
    }
    
    void updateDrag(Point mousePos) {
        Point2D imgPoint = screenToImage(mousePos);
        
        center.setLocation(center.getX() + dragStartingPosition.getX() - imgPoint.getX(), center.getY() + dragStartingPosition.getY() - imgPoint.getY());
        
        startDrag(mousePos);
        
        callListeners();
    }

    void zoomIn(Point mousePos) {
        scale(mousePos, true);
    }

    void zoomOut(Point mousePos) {
        scale(mousePos, false);
    }

    void scale(Point mousePos, boolean zoomIn) {
        int movement = zoomIn ? 1 : -1;
        Point scr = mousePos;
        float Sb = (float) (scale * Math.pow(scaleIncrement, movement));

        Point2D img = screenToImage(scr);

        double newX = (Sb * img.getX() + windowSize.width / 2 - scr.x) / Sb;
        double newY = (Sb * img.getY() + windowSize.height / 2 - scr.y) / Sb;
        
        center.setLocation(newX, newY);

        scale = Sb;

        callListeners();
    }

    public void addZoomChangeListener(ZoomChangeListener listener) {
        listener.onZoomChange(getTransform());
        listeners.add(listener);
    }

    public void removeZoomChangeListener(ZoomChangeListener listener) {
        listeners.remove(listener);
    }

    private void callListeners() {
        listeners.forEach(listener -> listener.onZoomChange(getTransform()));
    }
    
    Point2D screenToImage(Point screenPos) {
        try {
            Point2D imgPoint = new Point();
            getTransform().inverseTransform(screenPos, imgPoint);
            return imgPoint; 
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    Point imageToScreen(Point2D imagePos) {
        Point screenPoint = new Point();
        getTransform().transform(imagePos, screenPoint);
        return screenPoint;
    }

    AffineTransform getTransform() {
        AffineTransform transform = new AffineTransform();
        
        transform.translate(windowSize.width / 2, windowSize.height / 2);
        transform.scale(scale, scale);
        transform.translate(-center.getX(), -center.getY());

        return transform;
    }
}
