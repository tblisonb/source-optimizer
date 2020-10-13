package optimizationprototype.util;

import optimizationprototype.gui.IGuiObserver;

import java.util.Vector;

public abstract class SubjectBase {

    private Vector<IGuiObserver> observers;
    private boolean state;

    protected SubjectBase() {
        observers = new Vector<>();
        state = false;
    }

    public void attach(IGuiObserver observer) {
        observers.add(observer);
    }

    public void detach(IGuiObserver observer) {
        observers.remove(observer);
    }

    public void signal() {
        state = true;
        for (IGuiObserver ob : observers) {
            ob.update();
        }
    }

    public boolean getState() {
        return state;
    }

    public void resetState() {
        state = false;
    }

}
