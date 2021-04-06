package org.particle_tracker.corrector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    static float hue = 0;
    static JPanel panel;
    static ParticleTrackerCanvas canvas;
    static JSlider seekSlider;
    final static JFrame frame = new JFrame("Corrector Particulas");
    static File fileDialogLocation = new File(System.getProperty("user.dir"));

    public static void main(String[] args) {

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        //Adds slider to the bottom
        frame.getContentPane().add(BorderLayout.SOUTH, createSlider());

        //Initializes the canvas
        changeCanvas(new ParticleTrackerCanvas(1601));
        //changeCanvas( ParticleTrackerCanvas.fromVideo(new File("/home/alan/Documents/Datasets/OriginalData/20190708_030028_IR.mp4")));
        //changeCanvas( ParticleTrackerCanvas.fromVideo(new File("/home/grasp/Documents/Bulls/Movie_AltaDensidad/MovieOriginal/Trimmed.mov")));
        //changeCanvas( ParticleTrackerCanvas.fromVideo(new File("/home/grasp/Documents/Bulls/Movie_AltaDensidad/SCALED_Trimmed.mov")));
        //changeCanvas( ParticleTrackerCanvas.fromVideo(new File("/home/grasp/Documents/Bulls/Movie_1/20190708_030028_IR.mp4")));

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

        JMenuItem videoMenuItem = new JMenuItem("Importar Video");
        videoMenuItem.addActionListener(__ -> importVideo());
        fileMenu.add(videoMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Importar CSV");
        openMenuItem.addActionListener(__ -> openFile());
        fileMenu.add(openMenuItem);

        fileMenu.addSeparator();

        JMenuItem newMenuItem = new JMenuItem("Exportar CSV");
        newMenuItem.addActionListener(__ -> saveFile());
        fileMenu.add(newMenuItem);

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
        float selectedFontSize = fontSizes[3];

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
        {

            JMenu subMenu = new JMenu("Radio de las particulas");
            ButtonGroup group = new ButtonGroup();

            float[] particleSizes = new float[]{
                5f, 10f, 20f, 50f, 100f, 150f, 200f
            };

            float selectedParticleSize = 20f;

            for (float particlesSize : particleSizes) {
                JMenuItem menuItem = new JRadioButtonMenuItem(String.valueOf(particlesSize));
                menuItem.addActionListener((ActionEvent actionEvent) -> {
                    Particle.radius = particlesSize;
                    canvas.repaint();
                });
                if (particlesSize == selectedParticleSize) {
                    //particleSizeMenuItem.setSelected(true);
                    menuItem.doClick();
                }
                group.add(menuItem);
                subMenu.add(menuItem);
            }

            configurationsMenu.add(subMenu);
        }

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

        // AUTOMATIC DETECTOR HEAD SIZE
        {

            JMenu subMenu = new JMenu("Tamaño de las particulas para trackeo automatico");
            ButtonGroup group = new ButtonGroup();

            int[] particleSizes = new int[]{
                8, 16, 32, 64, 128
            };

            int selectedParticleSize = particleSizes[2];

            for (int particlesSize : particleSizes) {
                JMenuItem menuItem = new JRadioButtonMenuItem(String.format("%dpx", particlesSize));
                menuItem.addActionListener((ActionEvent actionEvent) -> {
                    NextFramePredictor.headSize = particlesSize;
                    //canvas.repaint();
                });
                if (particlesSize == selectedParticleSize) {
                    //particleSizeMenuItem.setSelected(true);
                    menuItem.doClick();
                }
                group.add(menuItem);
                subMenu.add(menuItem);
            }

            configurationsMenu.add(subMenu);
        }
        // AUTOMATIC DETECTOR SEARCH DISTANCE FROM ORIGIN
        {

            JMenu subMenu = new JMenu("Distancia de busqueda en trackeo automatico");
            ButtonGroup group = new ButtonGroup();

            int[] distances = new int[]{
                8, 16, 32, 64, 96, 128
            };

            int selectedDistance = distances[2];

            for (int distance : distances) {
                JMenuItem menuItem = new JRadioButtonMenuItem(String.format("%dpx", distance));
                menuItem.addActionListener((ActionEvent actionEvent) -> {
                    NextFramePredictor.delta = distance;
                    //canvas.repaint();
                });
                if (distance == selectedDistance) {
                    //particleSizeMenuItem.setSelected(true);
                    menuItem.doClick();
                }
                group.add(menuItem);
                subMenu.add(menuItem);
            }

            configurationsMenu.add(subMenu);
        }

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
                seekSlider.setMaximum(controller.dataFrameCount);
                seekSlider.setValue(controller.getDataFrame());
            }

            @Override
            public void onVideoFrameChange(FrameController controller) {
                frame.setTitle(String.format("frame: %d / %d",
                        controller.getVideoFrame() + 1,
                        controller.videoFrameCount));
            }
        });

        canvas.frameController.forceFrameChangeListenerCall();

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
            changeCanvas(ParticleTrackerCanvas.fromVideo(file));
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
