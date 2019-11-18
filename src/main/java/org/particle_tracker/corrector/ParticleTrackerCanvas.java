/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.particle_tracker.corrector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author alan
 */
public class ParticleTrackerCanvas extends Canvas implements KeyListener, OperationFinishedListener, FrameChangeListener, MouseWheelListener, ComponentListener, MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    final static Class<? extends Operation>[] operationClasses = new Class[]{MoveParticle.class, BringParticle.class,
        RemoveParticle.class, CreateParticle.class};

    static float scale = 1;
    final FramesData framesData;
    final ZoomController zoomController;

    final OperationManager operationManager;

    private BufferedImage videoFrame;
    final FrameGrabber grabber;

    //Configurations
    boolean hide = false;
    public boolean autoNextOnBringParticle = false;
    public boolean pathFromLastToCurrentParticle = true;
    public boolean markCenterOfParticles = false;

    final FrameController frameController;

    public ParticleTrackerCanvas(int frameCount) {
        this(new FramesData(frameCount));
    }

    public ParticleTrackerCanvas(FramesData framesData) {

        operationManager = new OperationManager(this, operationClasses);
        operationManager.addOperationFinishListener(this);

        addMouseListener(this);
        addMouseMotionListener(this);

        // mouseHandler.addBetterMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        grabFocus();

        grabber = null;

        frameController = new FrameController(framesData.frames.length, false);
        frameController.addFrameChangeListener(this);
        frameController.forceFrameChangeListenerCall();

        zoomController = new ZoomController(getDimension(), new Point(0, 0));
        initializeZoomController();

        this.framesData = framesData;
    }

    public ParticleTrackerCanvas(FrameGrabber grabber) {
        operationManager = new OperationManager(this, operationClasses);
        operationManager.addOperationFinishListener(this);

        addMouseListener(this);
        addMouseMotionListener(this);

        addKeyListener(this);
        setFocusable(true);
        grabFocus();

        this.grabber = grabber;

        boolean highFPSMode = false;

        if (this.grabber != null) {
            highFPSMode = this.grabber.frameRate > 15; //If video framerate is higher than 15, then the high FPS mode is automatically activated
            System.out.printf("Video FPS: %f\nHigh FPS mode: %s\n", this.grabber.frameRate, highFPSMode ? "enabled" : "disabled");
        }

        frameController = new FrameController(grabber.frameCount, highFPSMode);
        frameController.addFrameChangeListener(this);
        frameController.forceFrameChangeListenerCall();

        zoomController = new ZoomController(getDimension(), new Point(0, 0));
        initializeZoomController();

        this.framesData = new FramesData(frameController.dataFrameCount);
    }

    final Dimension getDimension() {
        return new Dimension(getWidth(), getHeight());
    }

    final void initializeZoomController() {
        addMouseWheelListener(this);
        zoomController.addZoomChangeListener(new ZoomChangeListener() {
            @Override
            public void onZoomChange(AffineTransform transform) {
                BetterMouseEvent.transform = transform;
                repaint();
            }
        });

    }

    public static ParticleTrackerCanvas fromVideo(File video) {
        FrameGrabber grabber = new FrameGrabber(video);
        ParticleTrackerCanvas canvas = new ParticleTrackerCanvas(grabber);
        return canvas;
    }

    public void dispose() {
        operationManager.dispose();
        frameController.removeFrameChangeListener(this);
        removeKeyListener(this);
        if (grabber != null) {
            grabber.release();
        }
    }

    public void getFrame() {
        if (grabber != null) {
            videoFrame = grabber.getFrame(frameController.getVideoFrame());
            repaint();
        }
    }

    public void draw(Graphics2D g) {

        g.setBackground(Color.black);
        g.setColor(g.getBackground());
        //g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.black);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        /*
        Point translation = new Point(getWidth() / 2, getHeight() / 2);
        if (videoFrame != null) {
            translation.move(translation.x - (int) (videoFrame.getWidth() * scale / 2), translation.y - (int) (videoFrame.getHeight() * scale / 2));
        }

        BetterMouseEvent.translation = translation;
        g.translate(translation.x, translation.y);
        g.scale(scale, scale);
         */
        AffineTransform transform = new AffineTransform();
        //transform.translate(getWidth() / 2, getHeight() / 2);
        transform.concatenate(zoomController.getTransform());

        g.transform(transform);

        g.setColor(Color.white);
        if (videoFrame != null) {
            g.drawImage(videoFrame, 0, 0, Color.white, null);
        }

        if (!hide && frameController.isInSync()) {
            int dataFrame = frameController.getDataFrame();
            if (dataFrame > 0) {
                //framesData.frames[currentFrame - 1].drawDashed(g);
                for (Particle particleA : framesData.frames[dataFrame - 1].particles) {
                    Optional<Particle> particleB = framesData.frames[dataFrame].particles
                            .stream()
                            .filter(p -> p.identity == particleA.identity)
                            .findAny();

                    //Si se encuentra una particula en el frame actual con la misma id que uno del frame anterior, se dibuja un path entre las dos
                    if (pathFromLastToCurrentParticle && particleB.isPresent()) {
                        particleA.drawDashedPath(g, particleB.get().position);
                    } else {
                        particleA.drawDashed(g);
                    }
                }
            }
            framesData.frames[dataFrame].draw(g);
            if (markCenterOfParticles) {
                int markRadius = 2;
                for (Particle particle : framesData.frames[dataFrame].particles) {
                    g.setColor(particle.identity.color);
                    g.fillOval(particle.position.x - markRadius, particle.position.y - markRadius, markRadius * 2, markRadius * 2);
                }
            }
        }

        if (operationManager.currentOperation != null) {
            operationManager.currentOperation.draw(g);
        }

        /*
        g.scale(1 / scale, 1 / scale);
        g.translate(-translation.x, -translation.y);
         */
        try {
            g.transform(transform.createInverse());
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(ParticleTrackerCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!frameController.isInSync()) {
            g.setStroke(new BasicStroke(10));
            g.setColor(Color.red);
            g.drawRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void onOperationFinish(Operation operation) {
        if (operation instanceof BringParticle && autoNextOnBringParticle) {
            goToNextDataFrame();
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println("Tecla presionada...");
        if ((keyEvent.getKeyCode() == KeyEvent.VK_Z) && keyEvent.isControlDown()) {
            undo();
        } else if ((keyEvent.getKeyCode() == KeyEvent.VK_Y) && keyEvent.isControlDown()) {
            redo();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT && keyEvent.isControlDown()) {
            goToNextVideoFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT && keyEvent.isControlDown()) {
            goToPreviousVideoFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
            goToNextDataFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
            goToPreviousDataFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            hide = true;
            repaint();
        }
    }

    public void showPathFromLastToCurrentParticle(boolean value) {
        pathFromLastToCurrentParticle = value;
        repaint();
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            hide = false;
            repaint();

        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        char ch = keyEvent.getKeyChar();
        if (ch == 'n' || ch == 'N') {
            if (grabber == null) {
                return;
            }

            int nextFrameNumber = frameController.getVideoFrame() + frameController.videoFramesPerDataFrame;
            int nextDataNumber = frameController.getDataFrame() + 1;

            if (frameController.videoFrameCount <= nextFrameNumber) {
                return;
            }

            BufferedImage currentFrame = videoFrame;
            BufferedImage nextFrame = grabber.getFrame(nextFrameNumber);
            Frame currentData = framesData.frames[frameController.getDataFrame()];
            Frame nextData = framesData.frames[nextDataNumber];

            //System.out.println(nextData);
            operationManager.startOperation(new NextFramePredictor(currentFrame, nextFrame, currentData, nextData));
            goToNextDataFrame();
            //System.out.println(nextData);

        }
    }

    void goToNextDataFrame() {
        goToDataFrame(frameController.getDataFrame() + 1);
    }

    void goToPreviousDataFrame() {
        goToDataFrame(frameController.isInSync() ? frameController.getDataFrame() - 1 : frameController.getDataFrame());
    }

    void goToDataFrame(int frame) {
        int oldFrame = frameController.getDataFrame();
        if (frame >= 0 && frame < frameController.dataFrameCount && (!frameController.isInSync() || frame != oldFrame)) {
            operationManager.startOperation(new CallbackOperation(new Callback() {
                @Override
                public void onEvent() {
                    frameController.setDataFrame(frame);
                }
            }, new Callback() {
                @Override
                public void onEvent() {
                    frameController.setDataFrame(oldFrame);
                }
            }));
        }
    }

    void goToNextVideoFrame() {
        goToVideoFrame(frameController.getVideoFrame() + 1);
    }

    void goToPreviousVideoFrame() {
        goToVideoFrame(frameController.getVideoFrame() - 1);
    }

    void goToVideoFrame(int frame) {
        int oldFrame = frameController.getVideoFrame();
        if (frame >= 0 && frame < frameController.videoFrameCount && frame != oldFrame) {
            operationManager.startOperation(new CallbackOperation(new Callback() {
                @Override
                public void onEvent() {
                    frameController.setVideoFrame(frame);
                }
            }, new Callback() {
                @Override
                public void onEvent() {
                    frameController.setVideoFrame(oldFrame);
                }
            }));
        }
    }

    public void undo() {
        operationManager.undo();
    }

    public void redo() {
        operationManager.redo();
    }

    @Override
    public void onDataFrameChange(FrameController controller) {
    }

    @Override
    public void onVideoFrameChange(FrameController controller) {
        getFrame();
        operationManager.setEnabled(frameController.isInSync());
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        int movement = e.getWheelRotation();
        Point zoomPoint = e.getPoint();

        if (movement < 0) {
            zoomController.zoomIn(zoomPoint);
        } else if (movement > 0) {

            zoomController.zoomOut(zoomPoint);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        zoomController.windowSize = getDimension();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /*
    @Override
    public void mouseDragged(BetterMouseEvent mouseEvent) {
        if (mouseEvent.middleButton) {
            System.out.println("Middle button pressed "+mouseEvent.position);
            zoomController.updateDrag(mouseEvent.position);
        }
    }

    @Override
    public void mousePressed(BetterMouseEvent mouseEvent) {
        if (mouseEvent.middleButton) {
            System.out.println("Middle button pressed "+mouseEvent.position);
            zoomController.startDrag(mouseEvent.position);
        }
    }
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            Point mousePos = e.getPoint();
            //System.out.println("Middle button pressed " + mousePos);
            zoomController.startDrag(mousePos);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            Point mousePos = e.getPoint();
            //System.out.println("Middle button moved " + mousePos);
            zoomController.updateDrag(mousePos);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
