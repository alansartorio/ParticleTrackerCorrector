package org.particle_tracker.corrector;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    static float hue = 0;
    static JPanel panel;
    static ParticleTrackerCanvas canvas;
    static JSlider seekSlider;
    final static JFrame frame = new JFrame("A JFrame");
    static File fileDialogLocation = new File("user.home");

    public static void main(String[] args) {

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        //Adds slider to the bottom
        frame.getContentPane().add(BorderLayout.SOUTH, createSlider());

        //Initializes the canvas
        changeCanvas(new ParticleTrackerCanvas(1601));

        //Adds a menu bar
        frame.setJMenuBar(createMenuBar());

        setFontSize(14f);

        //frame.getContentPane().add(canvas);
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

        // CONFIGURATIONS MENU
        JMenu configurationsMenu = new JMenu("Configuraciones");
        menuBar.add(configurationsMenu);

        // SCALE MENU
        /*
        JMenu scaleMenu = new JMenu("Escalas");
        ButtonGroup scalesGroup = new ButtonGroup();
        configurationsMenu.add(scaleMenu);

        float[] scales = new float[]{
            0.1f, 0.2f, 0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f, 5f
        };
        float selectedScale = 1.5f;

        for (float scale : scales) {
            JMenuItem scaleMenuItem = new JRadioButtonMenuItem(String.valueOf(scale * 100) + '%');
            scaleMenuItem.addActionListener((ActionEvent actionEvent) -> {
                setScale(scale);
            });
            if (selectedScale == scale) {
                scaleMenuItem.doClick();
            }
            scalesGroup.add(scaleMenuItem);
            scaleMenu.add(scaleMenuItem);
        }
         */
        
        // FONT SIZE MENU
        JMenu fontSizeMenu = new JMenu("Tamaño de Fuente");
        ButtonGroup fontSizeGroup = new ButtonGroup();
        configurationsMenu.add(fontSizeMenu);

        float[] fontSizes = new float[]{
            5f, 8f, 10f, 12f, 14f, 16f, 18f, 20f, 24f, 28f, 30f
        };
        float selectedFontSize = 12f;

        for (float fontSize : fontSizes) {
            JMenuItem fontSizeMenuItem = new JRadioButtonMenuItem(String.valueOf(fontSize));

            fontSizeMenuItem.addActionListener((ActionEvent actionEvent) -> {
                setFontSize(fontSize);
            });

            if (fontSize == selectedFontSize) {
                fontSizeMenuItem.doClick();
            }
            fontSizeGroup.add(fontSizeMenuItem);
            fontSizeMenu.add(fontSizeMenuItem);
        }

        // SIZE OF PARTICLE CIRCLES RADIO BUTTONS
        JMenu particleSizeSubMenu = new JMenu("Tamaño de las particulas");
        ButtonGroup particleSizeGroup = new ButtonGroup();

        float[] particleSizes = new float[]{
            5f, 10f, 20f, 50f, 100f, 150f, 200f
        };

        float selectedParticleSize = 20f;

        for (float particlesSize : particleSizes) {
            JMenuItem particleSizeMenuItem = new JRadioButtonMenuItem(String.valueOf(particlesSize));
            particleSizeMenuItem.addActionListener((ActionEvent actionEvent) -> {
                Particle.radius = particlesSize;
                canvas.repaint();
            });
            if (particlesSize == selectedParticleSize) {
                //particleSizeMenuItem.setSelected(true);
                particleSizeMenuItem.doClick();
            }
            particleSizeGroup.add(particleSizeMenuItem);
            particleSizeSubMenu.add(particleSizeMenuItem);
        }

        configurationsMenu.add(particleSizeSubMenu);

        // AUTONEXT ON CLICK CHECKBOX
        JCheckBoxMenuItem autoNextCheckbox = new JCheckBoxMenuItem("Siguiente frame automatico al traer particula", canvas.autoNextOnBringParticle);
        autoNextCheckbox.addActionListener((event) -> {
            boolean selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            canvas.autoNextOnBringParticle = selected;
        });
        configurationsMenu.add(autoNextCheckbox);

        // PATH BETWEEN LAST AND ACTUAL PARTICLE CHECKBOX
        JCheckBoxMenuItem pathBetweenParticles = new JCheckBoxMenuItem("Union entre particula anterior y actual", canvas.pathFromLastToCurrentParticle);
        pathBetweenParticles.addActionListener((event) -> {
            boolean selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            canvas.showPathFromLastToCurrentParticle(selected);
        });
        configurationsMenu.add(pathBetweenParticles);

        // MARK CENTER OF PARTICLES CHECKBOX
        JCheckBoxMenuItem markParticlesCenter = new JCheckBoxMenuItem("Marcar centro de particulas", canvas.markCenterOfParticles);
        markParticlesCenter.addActionListener((event) -> {
            boolean selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            canvas.markCenterOfParticles = selected;
            canvas.repaint();
        });
        configurationsMenu.add(markParticlesCenter);

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

    static void changeCanvas(ParticleTrackerCanvas newCanvas) {
        if (canvas != null) {
            frame.getContentPane().remove(canvas);
            canvas.dispose();
        }
        canvas = newCanvas;

        canvas.frameController.addFrameChangeListener(new FrameChangeListener() {
            @Override
            public void onDataFrameChange(FrameController controller) {
                seekSlider.setValue(controller.getDataFrame());
            }

            @Override
            public void onVideoFrameChange(FrameController controller) {
                frame.setTitle(String.format("frame: %d / %d",
                        controller.getVideoFrame() + 1,
                        controller.videoFrameCount));
            }
        });

        seekSlider.setMaximum(canvas.frameController.dataFrameCount - 1);
        seekSlider.setValue(canvas.frameController.getDataFrame());

        frame.getContentPane().add(canvas);
        frame.validate();
    }

    static void setFontSize(float fontSize) {
        Identity.font = Identity.font.deriveFont(fontSize);
        canvas.repaint();
    }

    static void importVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(fileDialogLocation);
        //Desactivo el filtro de mp4 por si se quiere cargar algun otro formato de video
        //fileChooser.setFileFilter(new FileNameExtensionFilter("video", "mp4"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveFileLocation(file);
            try {
                changeCanvas(ParticleTrackerCanvas.fromVideo(file));
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
            canvas.goToDataFrame(source.getValue());
        });
        seekSlider.setFocusable(false);

        return seekSlider;
    }
}
