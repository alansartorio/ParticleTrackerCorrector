package org.particle_tracker.corrector;

import java.awt.*;
import java.util.Random;

class Particle {
	static int strokeWeight = 2;
	static float dragRadius = 20;
	static float radius = 15;
	Point position;
	Identity identity;

	public Particle(Point position, Identity identity) {
		this.position = position;
		this.identity = identity;
	}

	public Particle(Point position) {
		this(position, null);
	}

	public void draw(Graphics2D g) {
		g.setColor(identity != null ? identity.color : Color.white);
		draw(g, position);
	}

	public void drawIdentity(Graphics2D g) {
		if (identity != null)
			drawIdentity(g, identity, position);
	}

	public void drawDashed(Graphics2D g) {
		g.setColor(identity != null ? identity.color : Color.white);
		drawDashed(g, position);
	}

	public static void draw(Graphics2D g, Point pos) {
		g.setStroke(new BasicStroke(strokeWeight));
		g.drawOval(pos.x - (int) radius, pos.y - (int) radius, (int) (radius * 2), (int) (radius * 2));
	}

	public static void drawDashed(Graphics2D g, Point pos) {
		g.setStroke(
				new BasicStroke(strokeWeight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));
		g.drawOval(pos.x - (int) radius, pos.y - (int) radius, (int) (radius * 2), (int) (radius * 2));
	}

	public static void drawIdentity(Graphics2D g, Identity identity, Point pos) {
		Identity.draw(g, identity.id, new Point(pos.x, pos.y - (int) radius - 10));
	}

	public boolean onDragPosition(Point point) {
		return distanceToCenter(point) <= radius;
	}

	public double distanceToCenter(Point point) {
		return point.distance(position);
	}
}

class Identity {
	static final Random rng = new Random();
	static int nextIdentityId = 0;

	public final int id;
	public final Color color;

	static void clearIdentities() {
		nextIdentityId = 0;
	}

	public Identity(int id) {
		this.id = id;
		color = Color.getHSBColor(rng.nextFloat(), 1f, 1f);
		nextIdentityId = Math.max(nextIdentityId, id + 1);
	}

	public Identity() {
		this(nextIdentityId);
	}

	public static void draw(Graphics2D g, int id, Point pos) {
		FontMetrics metrics = g.getFontMetrics();
		String strID = String.valueOf(id);

		Dimension textSize = new Dimension(metrics.stringWidth(strID), metrics.getHeight());

		Rectangle rect = new Rectangle(pos.x, pos.y, textSize.width + 3, textSize.height + 1);
		rect.translate(-rect.width / 2, -rect.height / 2);

		g.setColor(new Color(0, 0, 0, 75));
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
                /*
		g.setColor(new Color(255, 255, 255, 150));
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
                */

		Point textPos = new Point(pos);
		textPos.translate(-textSize.width / 2, -textSize.height / 2 + metrics.getAscent());

		g.setColor(Color.white);
		g.drawString(String.valueOf(id), textPos.x, textPos.y);
	}
}