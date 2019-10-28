package org.particle_tracker.corrector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

class MoveParticle extends Operation {

    FramesData framesData;
    int currentFrame;
    Particle source;
    Point oldPosition;
    Point newPosition;

    public MoveParticle(FramesData framesData, int currentFrame, Particle source, Point newPosition) {
        this.framesData = framesData;
        this.currentFrame = currentFrame;
        this.source = source;
        oldPosition = new Point(source.position);
        this.newPosition = new Point(newPosition);
    }

    @Override
    void undo() {
        source.position.setLocation(oldPosition);
    }

    @Override
    void redo() {
        source.position.setLocation(newPosition);
    }

    @Override
    void draw(Graphics2D g) {
        g.setColor(Color.white);
        Particle.draw(g, newPosition);
    }

    @Override
    public void mouseReleased(BetterMouseEvent mouseEvent) {
        if (!mouseEvent.leftButton) return;
        Particle clickedParticle;
        if ((clickedParticle = framesData.frames[currentFrame].getParticleInPosition(mouseEvent.position)) != null && clickedParticle != source) {
            cancel();
            operationManager.startOperation(
                    new SwapIdentities(framesData, currentFrame, source.identity, clickedParticle.identity));
        } else {
            redo();
            finish();
        }
    }

    @Override
    public void mouseDragged(BetterMouseEvent mouseEvent) {
        newPosition.setLocation(mouseEvent.position);
        repaint();
    }

    @Override
    void init() {
    }

    /*
	public static void checkMouseClicked(OperationManager operationManager, BetterMouseEvent mouseEvent,
			FramesData frames, int currentFrame) {
		startOperation(operationManager, mouseEvent, frames, currentFrame);
	}

	public static void checkMouseDragStart(OperationManager operationManager, BetterMouseEvent mouseEvent,
			FramesData frames, int currentFrame) {
		startOperation(operationManager, mouseEvent, frames, currentFrame);
	}
     */
    public static void checkMousePressed(OperationManager operationManager, BetterMouseEvent mouseEvent,
            FramesData frames, int currentFrame) {
        startOperation(operationManager, mouseEvent, frames, currentFrame);
    }

    static void startOperation(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
        // Left button
        if (mouseEvent.leftButton) {
            Particle clickedParticle;

            if ((clickedParticle = frames.frames[currentFrame].getParticleInPosition(mouseEvent.position)) != null) {
                operationManager.startOperation(new MoveParticle(frames, currentFrame, clickedParticle, mouseEvent.position));
            }
        }
    }

}

class SwapIdentities extends InstantOperation {

    FramesData framesData;
    int currentFrame;
    Identity identityA;
    Identity identityB;

    ArrayList<Particle> affectedA = new ArrayList<>();
    ArrayList<Particle> affectedB = new ArrayList<>();

    SwapIdentities(FramesData framesData, int currentFrame, Identity identityA, Identity identityB) {
        this.framesData = framesData;
        this.currentFrame = currentFrame;
        this.identityA = identityA;
        this.identityB = identityB;
    }

    @Override
    void undo() {
        affectedA.forEach((a) -> {
            a.identity = identityA;
        });
        affectedB.forEach((b) -> {
            b.identity = identityB;
        });
    }

    @Override
    void redo() {
        affectedA.forEach((a) -> {
            a.identity = identityB;
        });
        affectedB.forEach((b) -> {
            b.identity = identityA;
        });
    }

    @Override
    void init() {
        for (int i = currentFrame; i < framesData.frames.length; i++) {
            Particle a = framesData.frames[i].searchByIdentity(identityA);
            if (a != null) {
                affectedA.add(a);
            }
            Particle b = framesData.frames[i].searchByIdentity(identityB);
            if (b != null) {
                affectedB.add(b);
            }
        }
        redo();
        finish();
    }

}

class SymmetricalCopy extends Operation {

    Particle center;
    Particle created;
    Point newPosition;
    Point centerPosition;
    Frame frame;

    Point getOppositePosition() {
        return new Point(centerPosition.x * 2 - newPosition.x, centerPosition.y * 2 - newPosition.y);
    }

    SymmetricalCopy(Frame frame, Particle center) {
        this.center = center;
        this.frame = frame;
        centerPosition = new Point(center.position);
        newPosition = new Point(center.position);
    }

    @Override
    public void mouseDragEnd(BetterMouseEvent mouseEvent) {
        created = new Particle(getOppositePosition());
        redo();
        finish();
    }

    @Override
    public void mouseDragged(BetterMouseEvent mouseEvent) {
        newPosition.setLocation(mouseEvent.position);
        repaint();
    }

    @Override
    void draw(Graphics2D g) {
        g.setColor(Color.white);
        Particle.draw(g, newPosition);
        Particle.draw(g, getOppositePosition());
    }

    @Override
    void undo() {
        frame.removeParticle(created);
        center.position.setLocation(centerPosition);
    }

    @Override
    void redo() {
        frame.addParticle(created);
        center.position.setLocation(newPosition);
    }

    @Override
    void init() {

    }

    public static void checkMouseDragStart(OperationManager operationManager, BetterMouseEvent mouseEvent,
            FramesData frames, int currentFrame) {
        Frame frame = frames.frames[currentFrame];

        // Click derecho
        if (mouseEvent.rightButton) {

            Particle clickedParticle = frame.getParticleInPosition(mouseEvent.position);
            if (clickedParticle != null) {
                operationManager.startOperation(new SymmetricalCopy(frame, clickedParticle));
            }
        }
    }

}

class BringParticle extends Operation {

    Frame frame;
    Particle particle;
    Point newPosition;
    Particle created;
    FramesData framesData;
    int currentFrame;

    BringParticle(FramesData framesData, int currentFrame, Frame frame, Particle particle, Point clickPosition) {
        this.framesData = framesData;
        this.currentFrame = currentFrame;
        this.frame = frame;
        this.particle = particle;
        newPosition = new Point(clickPosition);
    }

    @Override
    public void mouseDragged(BetterMouseEvent mouseEvent) {
        newPosition.setLocation(mouseEvent.position);
        repaint();
    }

    @Override
    public void mouseReleased(BetterMouseEvent mouseEvent) {
        if (!mouseEvent.leftButton) return;
        Particle clickedParticle;
        if ((clickedParticle = frame.getParticleInPosition(mouseEvent.position)) != null) {
            // Si se solto sobre una particula, se transfiere la identidad
            cancel();
            operationManager.startOperation(
                    new BringIdentity(framesData, currentFrame, particle.identity, clickedParticle.identity));
        } else {
            // Si se solto sobre vacio, se crea una particula
            created = new Particle(mouseEvent.position, particle.identity);
            redo();
            finish();
        }
    }

    @Override
    void undo() {
        frame.removeParticle(created);
    }

    @Override
    void redo() {
        frame.addParticle(created);
    }

    public static void checkMousePressed(OperationManager operationManager, BetterMouseEvent mouseEvent,
            FramesData frames, int currentFrame) {
        if (mouseEvent.leftButton) {
            Particle clickedParticle;
            if (currentFrame > 0
                    && (clickedParticle = frames.frames[currentFrame - 1]
                            .getParticleInPosition(mouseEvent.position)) != null
                    && frames.frames[currentFrame].searchByIdentity(clickedParticle.identity) == null) {
                operationManager.startOperation(
                        new BringParticle(frames, currentFrame, frames.frames[currentFrame], clickedParticle, mouseEvent.position));
            }
        }
    }

    @Override
    void draw(Graphics2D g) {
        g.setColor(Color.white);
        Particle.draw(g, newPosition);
    }

    @Override
    void init() {
    }

}

class BringIdentity extends InstantOperation {

    FramesData frameData;
    ArrayList<Particle> affectedParticles = new ArrayList<>();
    Identity from;
    Identity to;
    int currentFrame;

    BringIdentity(FramesData frameData, int currentFrame, Identity from, Identity to) {
        this.frameData = frameData;
        this.currentFrame = currentFrame;
        this.from = from;
        this.to = to;
    }

    @Override
    void undo() {
        for (Particle particle : affectedParticles) {
            particle.identity = to;
        }
    }

    @Override
    void redo() {
        for (Particle particle : affectedParticles) {
            particle.identity = from;
        }
    }

    @Override
    void init() {
        for (int i = currentFrame; i < frameData.frames.length; i++) {
            Particle p = frameData.frames[i].searchByIdentity(to);
            if (p != null) {
                affectedParticles.add(p);
            }
        }
        redo();
        finish();
    }

}

class CreateParticle extends InstantOperation {

    Frame currentFrame;
    Point position;
    Particle created;

    public CreateParticle(Frame currentFrame, Point position) {
        this.currentFrame = currentFrame;
        this.position = position;
    }

    @Override
    void undo() {
        currentFrame.removeParticle(created);
    }

    @Override
    void redo() {
        currentFrame.addParticle(created);
    }

    public static void checkMouseClicked(OperationManager operationManager, BetterMouseEvent mouseEvent,
            FramesData frames, int currentFrame) {
        if (mouseEvent.leftButton) {
            operationManager.startOperation(new CreateParticle(frames.frames[currentFrame], mouseEvent.position));
        }
    }

    @Override
    void init() {
        created = new Particle(position, new Identity());
        redo();
        finish();
    }

}

class RemoveParticle extends InstantOperation {

    FramesData framesData;
    int currentFrame;
    Particle particle;
    // post elimination identity modifications
    ArrayList<Particle> affectedParticles = new ArrayList<>();
    Identity from;
    Identity to;

    public RemoveParticle(FramesData framesData, int currentFrame, Particle particle) {
        this.framesData = framesData;
        this.currentFrame = currentFrame;
        this.particle = particle;
    }

    @Override
    void undo() {
        framesData.frames[currentFrame].addParticle(particle);

        affectedParticles.forEach((p) -> {
            p.identity = from;
        });
    }

    @Override
    void redo() {
        framesData.frames[currentFrame].removeParticle(particle);

        affectedParticles.forEach((p) -> {
            p.identity = to;
        });
    }

    public static void checkMouseClicked(OperationManager operationManager, BetterMouseEvent mouseEvent,
            FramesData frames, int currentFrame) {
        if (mouseEvent.rightButton) {
            Particle clickedParticle;
            if ((clickedParticle = frames.frames[currentFrame].getParticleInPosition(mouseEvent.position)) != null) {
                operationManager.startOperation(new RemoveParticle(frames, currentFrame, clickedParticle));
            }
        }
    }

    @Override
    void init() {
        if ((currentFrame > 0 && framesData.frames[currentFrame - 1].searchByIdentity(particle.identity) != null) && currentFrame < framesData.frames.length - 1) {
            from = particle.identity;
            to = new Identity();
            Particle p;
            for (int i = currentFrame; i < framesData.frames.length
                    && (p = framesData.frames[i].searchByIdentity(from)) != null; i++) {
                affectedParticles.add(p);
            }
        }

        redo();
        finish();
    }

}

interface Callback {

    void onEvent();
}

class CallbackOperation extends InstantOperation {

    Callback redo;
    Callback undo;

    public CallbackOperation(Callback execute, Callback undo) {
        this.redo = execute;
        this.undo = undo;
    }

    @Override
    void undo() {
        undo.onEvent();
    }

    @Override
    void redo() {
        redo.onEvent();
    }

    @Override
    void init() {
        redo();
        finish();
    }
}
