package org.particle_tracker.corrector;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {
	static float hue = 0;
	static JPanel panel;
	static MyCanvas canvas;
	static JSlider seekSlider;
	final static JFrame frame = new JFrame("A JFrame");

	public static void main(String[] args) {

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);

		JMenuBar menuBar = new JMenuBar();

		// FILE MENU
		JMenu fileMenu = new JMenu("Archivo");
		// fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem newMenuItem = new JMenuItem("Guardar");
		newMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				newFile();
			}
		});
		fileMenu.add(newMenuItem);

		JMenuItem openMenuItem = new JMenuItem("Abrir");
		openMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				openFile();
			}
		});
		fileMenu.add(openMenuItem);

		JMenuItem videoMenuItem = new JMenuItem("Importar Video");
		videoMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				importVideo();
			}
		});
		fileMenu.add(videoMenuItem);

		JMenuItem exitMenuItem = new JMenuItem("Salir");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		fileMenu.add(exitMenuItem);

		// EDIT MENU
		JMenu editMenu = new JMenu("Editar");
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);

		JMenuItem undoMenuItem = new JMenuItem("Deshacer");
		undoMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.undo();
			}
		});
		editMenu.add(undoMenuItem);

		JMenuItem redoMenuItem = new JMenuItem("Rehacer");
		redoMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				canvas.redo();
			}
		});
		editMenu.add(redoMenuItem);

		// SCALE MENU
		JMenu scaleMenu = new JMenu("Escalas");
		scaleMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(scaleMenu);
		
		float[] scales = new float[] {
				0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f, 5f
		};
		
		for (float scale : scales) {
			JMenuItem scaleMenuItem = new JMenuItem(String.valueOf(scale * 100) + '%');
			scaleMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					setScale(scale);
				}
			});
			scaleMenu.add(scaleMenuItem);
		}

		frame.setJMenuBar(menuBar);

		// SLIDER
		seekSlider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
		seekSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				/// if (source.getValueIsAdjusting())
				canvas.goToFrame(source.getValue());
			}
		});
		seekSlider.setFocusable(false);
		frame.getContentPane().add(BorderLayout.SOUTH, seekSlider);

		// FramesData framesData = ;

		changeCanvas(new MyCanvas(new FramesData(10)));

		frame.getContentPane().add(canvas);

		frame.setVisible(true);
	}

	static void changeColor() {
		hue += 0.01f;
		panel.setBackground(Color.getHSBColor(hue, 1f, 1f));
	}

	static void newFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showSaveDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			canvas.framesData.saveToCSV(file);
			canvas.repaint();
		}
	}

	static void openFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			canvas.framesData.loadFromCSV(file);
			canvas.repaint();
		}
	}

	static void changeCanvas(MyCanvas newCanvas) {
		if (canvas != null) {
			frame.getContentPane().remove(canvas);
			canvas.dispose();
		}
		canvas = newCanvas;

		canvas.addChangeFrameListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int currentFrame = e.getID();
				seekSlider.setValue(currentFrame);
				frame.setTitle("frame: " + (currentFrame + 1) + '/' + canvas.framesData.frames.length);
			}
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

	static void importVideo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			changeCanvas(MyCanvas.fromVideo(file));
		}
	}
}

class MyCanvas extends Canvas implements KeyListener {
	private static final long serialVersionUID = 1L;
	static float scale = 1;
	final FramesData framesData;
	int currentFrame = 0;
	final OperationManager operationManager;
	BufferedImage videoFrame;
	FrameGrabber grabber;

	ArrayList<ActionListener> changeFrameListeners = new ArrayList<>();

	public MyCanvas(FramesData framesData) {
		@SuppressWarnings("unchecked")
		final Class<? extends Operation>[] classes = new Class[] { MoveParticle.class, BringParticle.class,
				RemoveParticle.class, CreateParticle.class, SymmetricalCopy.class };

		operationManager = new OperationManager(this, classes);

		// mouseHandler.addBetterMouseListener(this);
		addKeyListener(this);
		setFocusable(true);
		grabFocus();

		this.framesData = framesData;
	}

	public static MyCanvas fromVideo(File video) {
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
		if (grabber != null)
			grabber.stop();
	}

	public void getFrame() {
		if (grabber != null) {
			videoFrame = grabber.getFrame(currentFrame);
			repaint();
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
			translation = new Point((int)((getWidth() - videoFrame.getWidth() * scale) / 2), (int)((getHeight() - videoFrame.getHeight() * scale) / 2));
		}
		
		BetterMouseEvent.translation = translation;
		g.translate(translation.x, translation.y);
		g.scale(scale, scale);
		
		g.setColor(Color.white);
		if (videoFrame != null)
			g.drawImage(videoFrame, 0, 0, Color.white, null);

		if (currentFrame > 0)
			framesData.frames[currentFrame - 1].drawDashed(g);
		framesData.frames[currentFrame].draw(g);

		if (operationManager.currentOperation != null)
			operationManager.currentOperation.draw(g);
	}

	Frame getCurrentFrame() {
		return framesData.frames[currentFrame];
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		if ((keyEvent.getKeyCode() == KeyEvent.VK_Z) && keyEvent.isControlDown()) {
			undo();
		} else if ((keyEvent.getKeyCode() == KeyEvent.VK_Y) && keyEvent.isControlDown()) {
			redo();
		} else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT && currentFrame < framesData.frames.length - 1) {
			goToFrame(currentFrame + 1);
		} else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT && currentFrame > 0) {
			goToFrame(currentFrame - 1);
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
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {
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