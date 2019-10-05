/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

/**
 *
 * @author alan
 */
public interface FrameChangeListener {
    public void onDataFrameChange(int frame);
    public void onVideoFrameChange(int frame);
}
