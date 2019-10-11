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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public ZoomController(Dimension windowSize, Point center) {
        this.windowSize = windowSize;
        this.center = center;
    }

    void zoomIn(Point mousePos) {
        scale(mousePos, true);
    }

    void zoomOut(Point mousePos) {
        scale(mousePos, false);
    }

    void scale(Point mousePos, boolean zoomIn) {

        int movement = zoomIn ? 1 : -1;
        Point imgPoint = new Point();
        try {
            getTransform().inverseTransform(mousePos, imgPoint);

        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }

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

    AffineTransform getTransform() {
        AffineTransform transform = new AffineTransform();
        
        transform.translate(windowSize.width / 2, windowSize.height / 2);
        
        transform.scale(scale, scale);
        transform.translate(-center.x, -center.y);

        return transform;
    }
}
