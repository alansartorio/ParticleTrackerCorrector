package org.particle_tracker.corrector;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;


abstract class Canvas extends JPanel {
    private static final long serialVersionUID = 1L;
  
    @Override
    protected void paintComponent(Graphics g) {
        draw((Graphics2D)g);
    }

    abstract void draw(Graphics2D graphics);
  }