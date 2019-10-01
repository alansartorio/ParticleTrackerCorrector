package org.particle_tracker.corrector;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;

interface OperationFinishedListener {
    void onOperationFinish(Operation operation);
}

class OperationManager implements BetterMouseListener {

    Stack<Operation> operationsStack = new Stack<>();
    Stack<Operation> redoOperationsStack = new Stack<>();
    //int operationsStackPosition = 0;
    Operation currentOperation = null;
    boolean canOperate = true;
    boolean inMouseCycle = false;
    MyCanvas component;
    ArrayList<OperationFinishedListener> operationFinishListeners = new ArrayList<>();
    BetterMouseHandler mouseHandler;
    Class<? extends Operation>[] operations;
    
    public OperationManager(MyCanvas component, Class<? extends Operation>[] operations) {
        mouseHandler = new BetterMouseHandler(component);
        mouseHandler.addBetterMouseListener(this);
        this.component = component;
        this.operations = operations;
    }
    
    void startOperation(Operation op) {
        
        if (currentOperation != null) {
            return;
        }
        
        op.setManager(this);
        
        if (inMouseCycle) {
            canOperate = false;
        }
        
        component.addKeyListener(op);
        
        currentOperation = op;
        currentOperation.init();
    }
    
    public void addOperationFinishListener(OperationFinishedListener listener) {
        operationFinishListeners.add(listener);
    }

    public void removeOperationFinishListener(OperationFinishedListener listener) {
        operationFinishListeners.remove(listener);
    }
    
    public void dispose() {
        if (currentOperation != null) {
            cancel();
        }
        mouseHandler.removeBetterMouseListener(this);
    }
    
    public void undo() {
        if (operationsStack.isEmpty()) {
            return;
        }
        Operation op = operationsStack.pop();
        redoOperationsStack.push(op);
        op.undo();
        if (op.repaintAtFinish) {
            repaint();
        }
    }
    
    public void redo() {
        if (redoOperationsStack.isEmpty()) {
            return;
        }
        Operation op = redoOperationsStack.pop();
        operationsStack.push(op);
        op.redo();
        if (op.repaintAtFinish) {
            repaint();
        }
    }
    
    protected void finish() {
        redoOperationsStack.clear();
        operationsStack.push(currentOperation);
        if (currentOperation.repaintAtFinish) {
            repaint();
        }
        Operation finishedOperation = currentOperation;
        cancel();
        operationFinishListeners.forEach(l -> l.onOperationFinish(finishedOperation));
    }
    
    protected void cancel() throws NullPointerException {
        if (currentOperation == null) {
            throw new NullPointerException();
        }
        component.removeKeyListener(currentOperation);
        currentOperation = null;
    }
    
    protected void repaint() {
        component.repaint();
    }
    
    public void callEvents(String methodName, BetterMouseEvent mouseEvent) {
        if (!canOperate) {
            return;
        }
        
        for (Class<? extends Operation> operation : operations) {
            try {
                Method method = operation.getMethod(methodName, OperationManager.class, BetterMouseEvent.class,
                        FramesData.class, int.class);
                method.invoke(null, this, mouseEvent, component.framesData, component.currentFrame);
                if (!canOperate) {
                    return;
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void mouseMoved(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseMoved", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mouseMoved(mouseEvent);
        }
    }
    
    @Override
    public void mouseDragStart(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseDragStart", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mouseDragStart(mouseEvent);
        }
    }
    
    @Override
    public void mouseDragEnd(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseDragEnd", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mouseDragEnd(mouseEvent);
        }
    }
    
    @Override
    public void mouseDragged(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseDragged", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mouseDragged(mouseEvent);
        }
    }
    
    @Override
    public void mousePressed(BetterMouseEvent mouseEvent) {
        inMouseCycle = true;
        
        callEvents("checkMousePressed", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mousePressed(mouseEvent);
        }
    }
    
    @Override
    public void mouseClicked(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseClicked", mouseEvent);
        if (currentOperation != null) {
            currentOperation.mouseClicked(mouseEvent);
        }
    }
    
    @Override
    public void mouseReleased(BetterMouseEvent mouseEvent) {
        callEvents("checkMouseReleased", mouseEvent);
        inMouseCycle = false;
        if (currentOperation != null) {
            currentOperation.mouseReleased(mouseEvent);
        }
        if (currentOperation == null && !canOperate) {
            canOperate = true;
        }
    }
    
}

abstract class Operation implements KeyListener, BetterMouseListener {

    OperationManager operationManager;
    boolean repaintAtFinish = true;
    
    void setManager(OperationManager operationManager) {
        this.operationManager = operationManager;
    }
    
    void finish() {
        operationManager.finish();
    }
    
    void cancel() {
        operationManager.cancel();
    }
    
    void repaint() {
        operationManager.repaint();
    }
    
    abstract void undo();
    
    abstract void redo();
    
    abstract void init();
    
    abstract void draw(Graphics2D g);
    
    public void mouseMoved(BetterMouseEvent mouseEvent) {
    }
    
    public void mouseDragStart(BetterMouseEvent mouseEvent) {
    }
    
    public void mouseDragEnd(BetterMouseEvent mouseEvent) {
    }
    
    public void mouseDragged(BetterMouseEvent mouseEvent) {
    }
    
    public void mousePressed(BetterMouseEvent mouseEvent) {
    }
    
    public void mouseReleased(BetterMouseEvent mouseEvent) {
    }
    
    public void mouseClicked(BetterMouseEvent mouseEvent) {
    }
    
    public void keyPressed(KeyEvent keyEvent) {
    }
    
    public void keyReleased(KeyEvent keyEvent) {
    }
    
    public void keyTyped(KeyEvent keyEvent) {
    }
    
    public static void checkMouseMoved(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMouseDragStart(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMouseDragEnd(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMouseDragged(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMousePressed(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMouseReleased(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
    
    public static void checkMouseClicked(OperationManager operationManager, BetterMouseEvent mouseEvent, FramesData frames,
            int currentFrame) {
    }
}

abstract class InstantOperation extends Operation {

    @Override
    void draw(Graphics2D g) {
        //Instant Operations dont need to draw anything
    }
}
