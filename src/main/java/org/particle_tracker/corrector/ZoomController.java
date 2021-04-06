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

/**
 *
 * @author grasp
 */
public class ZoomController {

    Point center;
    float scale = 1;
    static float scaleIncrement = 1.2f;
    static float scaleMovement = 0.2f;
    private List<ZoomChangeListener> listeners = new ArrayList<>();
    public Dimension windowSize;
    private Point dragStartingPosition;

    public ZoomController(Dimension windowSize, Point center) {
        this.windowSize = windowSize;
        this.center = center;
    }
    
    void startDrag(Point mousePos) {
        dragStartingPosition = screenToImage(mousePos);
    }
    
    void updateDrag(Point mousePos) {
        Point imgPoint = screenToImage(mousePos);
        
        center.translate(dragStartingPosition.x - imgPoint.x, dragStartingPosition.y - imgPoint.y);
        
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
        Point imgPoint = screenToImage(mousePos);

        imgPoint.translate(-center.x, -center.y);
        imgPoint.setLocation(movement * imgPoint.x * scaleMovement, movement * imgPoint.y * scaleMovement);
        imgPoint.translate(center.x, center.y);

        Point newPoint = imgPoint;

        center.setLocation(newPoint);

        if (zoomIn) {
            scale *= scaleIncrement;
        } else {
            scale /= scaleIncrement;
        }

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
    
    Point screenToImage(Point screenPos) {
        try {
            Point imgPoint = new Point();
            getTransform().inverseTransform(screenPos, imgPoint);
            return imgPoint; 
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    Point imageToScreen(Point imagePos) {
        Point screenPoint = new Point();
        getTransform().transform(imagePos, screenPoint);
        return screenPoint;
    }

    AffineTransform getTransform() {
        AffineTransform transform = new AffineTransform();
        
        transform.translate(windowSize.width / 2, windowSize.height / 2);
        
        transform.scale(scale, scale);
        transform.translate(-center.x, -center.y);

        return transform;
    }
}
