/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.awt.geom.AffineTransform;

/**
 *
 * @author grasp
 */
public interface ZoomChangeListener {

    void onZoomChange(AffineTransform transform);
}
