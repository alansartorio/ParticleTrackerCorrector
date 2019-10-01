package org.particle_tracker.corrector;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

class BetterMouseEvent {
	static float scale = 1;
	static Point translation = new Point(0, 0);
	Point position;
	boolean leftButton;
	boolean rightButton;
	boolean middleButton;
	
	BetterMouseEvent(MouseEvent mouseEvent) {
		Point noScaledPosition = mouseEvent.getPoint();
		position = new Point((int) ((noScaledPosition.x - translation.x) / scale), (int) ((noScaledPosition.y - translation.y) / scale));
		leftButton = SwingUtilities.isLeftMouseButton(mouseEvent);
		rightButton = SwingUtilities.isRightMouseButton(mouseEvent);
		middleButton = SwingUtilities.isMiddleMouseButton(mouseEvent);
	}
}

abstract interface BetterMouseListener extends EventListener {

	void mouseMoved(BetterMouseEvent mouseEvent);

	void mouseDragStart(BetterMouseEvent mouseEvent);

	void mouseDragEnd(BetterMouseEvent mouseEvent);

	void mouseDragged(BetterMouseEvent mouseEvent);

	void mousePressed(BetterMouseEvent mouseEvent);

	void mouseReleased(BetterMouseEvent mouseEvent);

	void mouseClicked(BetterMouseEvent mouseEvent);
}

class BetterMouseHandler implements MouseInputListener {
	static float grabDeadZone = 10f;
	Point pressedPosition;
	boolean dragging = false;
	CopyOnWriteArrayList<BetterMouseListener> listeners = new CopyOnWriteArrayList<>();

	BetterMouseHandler(Component component) {
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
	}

	public void addBetterMouseListener(BetterMouseListener listener) {
		listeners.add(listener);
	}

	public void removeBetterMouseListener(BetterMouseListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		BetterMouseEvent event = new BetterMouseEvent(mouseEvent);
		listeners.forEach((l) -> l.mouseMoved(event));
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		BetterMouseEvent event = new BetterMouseEvent(mouseEvent);
		if (pressedPosition.distance(mouseEvent.getPoint()) > grabDeadZone && !dragging) {
			dragging = true;
			listeners.forEach((l) -> l.mouseDragStart(event));
		}
		if (dragging)
			listeners.forEach((l) -> l.mouseDragged(event));
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		BetterMouseEvent event = new BetterMouseEvent(mouseEvent);
		pressedPosition = mouseEvent.getPoint();
		listeners.forEach((l) -> l.mousePressed(event));
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		BetterMouseEvent event = new BetterMouseEvent(mouseEvent);
		if (dragging) {
			listeners.forEach((l) -> l.mouseDragEnd(event));
			dragging = false;
		} else {
			listeners.forEach((l) -> {
				//System.out.println(listeners + " event called");
				l.mouseClicked(event);
			});
		}
		listeners.forEach((l) -> l.mouseReleased(event));
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent) {
	}

	@Override
	public void mouseExited(MouseEvent mouseEvent) {
	}
}