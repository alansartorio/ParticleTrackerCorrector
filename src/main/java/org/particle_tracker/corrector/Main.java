package org.particle_tracker.corrector;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    static float hue = 0;
    static JPanel panel;
    static MyCanvas canvas;
    static JSlider seekSlider;
    final static JFrame frame = new JFrame("A JFrame");
    static File fileDialogLocation = new File("user.home");

    public static void main(String[] args) {

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        //Adds a menu bar
        frame.setJMenuBar(createMenuBar());

        //Adds slider to the bottom
        frame.getContentPane().add(BorderLayout.SOUTH, createSlider());

        //Initializes the canvas
        changeCanvas(new MyCanvas(new FramesData(10)));

        setFontSize(14f);

        frame.getContentPane().add(canvas);

        frame.setVisible(true);
    }

    static void changeColor() {
        hue += 0.01f;
        panel.setBackground(Color.getHSBColor(hue, 1f, 1f));
    }

    static JMenuBar createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        // FILE MENU
        JMenu fileMenu = new JMenu("Archivo");
        // fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem newMenuItem = new JMenuItem("Guardar");
        newMenuItem.addActionListener(__ -> saveFile());
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Abrir");
        openMenuItem.addActionListener(__ -> openFile());
        fileMenu.add(openMenuItem);

        JMenuItem videoMenuItem = new JMenuItem("Importar Video");
        videoMenuItem.addActionListener(__ -> importVideo());
        fileMenu.add(videoMenuItem);

        fileMenu.addSeparator();

        JMenuItem compactMenuItem = new JMenuItem("Compactar");
        compactMenuItem.addActionListener(__ -> {
            canvas.framesData.compactIds();
            canvas.repaint();
        });
        fileMenu.add(compactMenuItem);

        fileMenu.addSeparator();

        JMenuItem exitMenuItem = new JMenuItem("Salir");
        exitMenuItem.addActionListener((ActionEvent actionEvent) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(exitMenuItem);

        // EDIT MENU
        JMenu editMenu = new JMenu("Editar");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        JMenuItem undoMenuItem = new JMenuItem("Deshacer");
        undoMenuItem.addActionListener((ActionEvent actionEvent) -> {
            canvas.undo();
        });
        editMenu.add(undoMenuItem);

        JMenuItem redoMenuItem = new JMenuItem("Rehacer");
        redoMenuItem.addActionListener((ActionEvent actionEvent) -> {
            canvas.redo();
        });
        editMenu.add(redoMenuItem);

        // SCALE MENU
        JMenu scaleMenu = new JMenu("Escalas");
        menuBar.add(scaleMenu);

        float[] scales = new float[]{
            0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f, 5f
        };

        for (float scale : scales) {
            JMenuItem scaleMenuItem = new JMenuItem(String.valueOf(scale * 100) + '%');
            scaleMenuItem.addActionListener((ActionEvent actionEvent) -> {
                setScale(scale);
            });
            scaleMenu.add(scaleMenuItem);
        }

        // FONT SIZE MENU
        JMenu fontSizeMenu = new JMenu("Tamaño de Fuente");
        menuBar.add(fontSizeMenu);

        float[] fontSizes = new float[]{
            5f, 8f, 10f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 30f
        };

        for (float fontSize : fontSizes) {
            JMenuItem fontSizeMenuItem = new JMenuItem(String.valueOf(fontSize));
            fontSizeMenuItem.addActionListener((ActionEvent actionEvent) -> {
                setFontSize(fontSize);
            });
            fontSizeMenu.add(fontSizeMenuItem);
        }

        // CONFIGURATIONS MENU
        JMenu configurationsMenu = new JMenu("Configuraciones");
        menuBar.add(configurationsMenu);

        // AUTONEXT ON CLICK CHECKBOX
        JCheckBoxMenuItem autoNextCheckbox = new JCheckBoxMenuItem("Siguiente frame automatico al traer particula");
        //autoNextCheckbox.add;
        configurationsMenu.add(autoNextCheckbox);
        autoNextCheckbox.addActionListener((event) -> {
            boolean selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            canvas.autoNextOnBringParticle = selected;
        });

        return menuBar;
    }

    static void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV data", "csv"));
        fileChooser.setCurrentDirectory(fileDialogLocation);
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileLocation(file);
            try {
                canvas.framesData.saveToCSV(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error al guardar archivo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV data", "csv"));
        fileChooser.setCurrentDirectory(fileDialogLocation);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileLocation(file);
            try {
                canvas.framesData.loadFromCSV(file);
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error al leer archivo", JOptionPane.ERROR_MESSAGE);
            }
            canvas.repaint();
        }
    }

    static void changeCanvas(MyCanvas newCanvas) {
        if (canvas != null) {
            frame.getContentPane().remove(canvas);
            canvas.dispose();
        }
        canvas = newCanvas;

        canvas.addChangeFrameListener((ActionEvent e) -> {
            int currentFrame = e.getID();
            seekSlider.setValue(currentFrame);
            frame.setTitle("frame: " + (currentFrame + 1) + '/' + canvas.framesData.frames.length);
        });

        seekSlider.setMaximum(canvas.framesData.frames.length - 1);
        seekSlider.setValue(canvas.currentFrame);

        frame.getContentPane().add(canvas);
        frame.validate();
    }

    static void setScale(float scale) {
        MyCanvas.scale = scale;
        BetterMouseEvent.scale = scale;
        canvas.repaint();
    }

    static void setFontSize(float fontSize) {
        Identity.font = Identity.font.deriveFont(fontSize);
        canvas.repaint();
    }

    static void importVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(fileDialogLocation);
        fileChooser.setFileFilter(new FileNameExtensionFilter("video", "mp4"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileLocation(file);
            try {
                changeCanvas(MyCanvas.fromVideo(file));
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(), "Error al cargar video", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static void saveFileLocation(File file) {

        fileDialogLocation = file.getParentFile();
    }

    private static JSlider createSlider() {
        // SLIDER
        seekSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
        seekSlider.addChangeListener((ChangeEvent e) -> {
            JSlider source = (JSlider) e.getSource();
            /// if (source.getValueIsAdjusting())
            canvas.goToFrame(source.getValue());
        });
        seekSlider.setFocusable(false);

        return seekSlider;
    }
}

class MyCanvas extends Canvas implements KeyListener, OperationFinishedListener {

    private static final long serialVersionUID = 1L;
    static float scale = 1;
    final FramesData framesData;
    int currentFrame = 0;
    final OperationManager operationManager;
    BufferedImage videoFrame;
    FrameGrabber grabber;
    boolean hide = false;
    public boolean autoNextOnBringParticle = false;

    ArrayList<ActionListener> changeFrameListeners = new ArrayList<>();

    public MyCanvas(FramesData framesData) {
        final Class<? extends Operation>[] classes = new Class[]{MoveParticle.class, BringParticle.class,
            RemoveParticle.class, CreateParticle.class};//, SymmetricalCopy.class};

        operationManager = new OperationManager(this, classes);
        operationManager.addOperationFinishListener(this);

        // mouseHandler.addBetterMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        grabFocus();

        this.framesData = framesData;
    }

    public static MyCanvas fromVideo(File video) throws org.bytedeco.javacv.FrameGrabber.Exception {
        FrameGrabber grabber = new FrameGrabber(video);
        int frameCount = grabber.start();
        MyCanvas canvas = new MyCanvas(new FramesData(frameCount));
        canvas.grabber = grabber;
        canvas.getFrame();
        return canvas;
    }

    public void dispose() {
        operationManager.dispose();
        removeKeyListener(this);
        if (grabber != null) {
            try {
                grabber.stop();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception ex) {
                System.out.println("Error closing frameGrabber: " + ex.getMessage());
                //Logger.getLogger(MyCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void getFrame() {
        if (grabber != null) {
            try {
                videoFrame = grabber.getFrame(currentFrame);
                repaint();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception ex) {
                System.out.println("Error getting frame: " + ex.getMessage());
                //Logger.getLogger(MyCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        changeFrameListeners.forEach(l -> l.actionPerformed(new ActionEvent(this, currentFrame, null)));
    }

    public void addChangeFrameListener(ActionListener listener) {
        listener.actionPerformed(new ActionEvent(this, currentFrame, null));
        changeFrameListeners.add(listener);
    }

    public void removeChangeFrameListener(ActionListener listener) {
        changeFrameListeners.remove(listener);
    }

    public void draw(Graphics2D g) {

        g.setBackground(Color.black);
        g.setColor(g.getBackground());
        //g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.black);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        Point translation = new Point(0, 0);
        if (videoFrame != null) {
            translation = new Point((int) ((getWidth() - videoFrame.getWidth() * scale) / 2), (int) ((getHeight() - videoFrame.getHeight() * scale) / 2));
        }

        BetterMouseEvent.translation = translation;
        g.translate(translation.x, translation.y);
        g.scale(scale, scale);

        g.setColor(Color.white);
        if (videoFrame != null) {
            g.drawImage(videoFrame, 0, 0, Color.white, null);
        }

        if (!hide) {
            if (currentFrame > 0) {
                framesData.frames[currentFrame - 1].drawDashed(g);
            }
            framesData.frames[currentFrame].draw(g);

        }

        if (operationManager.currentOperation != null) {
            operationManager.currentOperation.draw(g);
        }
    }

    Frame getCurrentFrame() {
        return framesData.frames[currentFrame];
    }

    @Override
    public void onOperationFinish(Operation operation) {
        if (operation instanceof BringParticle && autoNextOnBringParticle) {
            goToNextFrame();
        }
    }    

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if ((keyEvent.getKeyCode() == KeyEvent.VK_Z) && keyEvent.isControlDown()) {
            undo();
        } else if ((keyEvent.getKeyCode() == KeyEvent.VK_Y) && keyEvent.isControlDown()) {
            redo();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT && currentFrame < framesData.frames.length - 1) {
            goToNextFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT && currentFrame > 0) {
            goToPreviousFrame();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            hide = true;
            repaint();
        }
    }

    public void setBackgroundVideo(File video) {

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
    }
    
    void goToNextFrame() {
        goToFrame(currentFrame + 1);
    }
    
    void goToPreviousFrame() {
        goToFrame(currentFrame - 1);
    }

    void goToFrame(int frame) {
        int oldFrame = currentFrame;
        if (frame >= 0 && frame < framesData.frames.length && frame != currentFrame) {
            operationManager.startOperation(new CallbackOperation(new Callback() {
                @Override
                public void onEvent() {
                    currentFrame = frame;
                    getFrame();
                }
            }, new Callback() {
                @Override
                public void onEvent() {
                    currentFrame = oldFrame;
                    getFrame();
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
}
