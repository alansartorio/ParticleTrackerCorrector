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
        if (identity != null) {
            drawIdentity(g, identity, position);
        }
    }

    public void drawDashedPath(Graphics2D g, Point posB) {

        g.setColor(identity != null ? identity.color : Color.white);
        drawDashedPath(g, position, posB);
    }

    public static void drawDashedPath(Graphics2D g, Point posA, Point posB) {
        g.setStroke(
                new BasicStroke(strokeWeight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));

        double angle = Math.atan2(posB.y - posA.y, posB.x - posA.x);
        int angleInt = (int) (-angle * 180 / Math.PI);

        g.drawArc(posA.x - (int) radius, posA.y - (int) radius, (int) (radius * 2), (int) (radius * 2), angleInt + 90, 180);

        int dx = (int) (Math.cos(angle + Math.PI / 2) * radius);
        int dy = (int) (Math.sin(angle + Math.PI / 2) * radius);

        g.drawLine(posA.x - dx, posA.y - dy, posB.x - dx, posB.y - dy);
        g.drawLine(posA.x + dx, posA.y + dy, posB.x + dx, posB.y + dy);
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
                new BasicStroke(strokeWeight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        g.drawOval(pos.x - (int) radius, pos.y - (int) radius, (int) (radius * 2), (int) (radius * 2));
    }

    public static void drawIdentity(Graphics2D g, Identity identity, Point pos) {
        Identity.draw(g, identity.id, new Point(pos.x, pos.y - (int) radius));
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
    static int nextIdentityId;
    static Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    public int id;
    public final Color color;

    static {
        clearIdentities();
    }

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
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        String strID = String.valueOf(id);

        Dimension textSize = new Dimension(metrics.stringWidth(strID), metrics.getHeight());

        Rectangle rect = new Rectangle(pos.x, pos.y, textSize.width, textSize.height);
        rect.translate(-rect.width / 2, -rect.height);

        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        Point textPos = new Point(pos);
        textPos.translate(-textSize.width / 2, -textSize.height + metrics.getAscent());

        g.setColor(Color.white);
        g.drawString(String.valueOf(id), textPos.x, textPos.y);
    }
}
