package optimizationprototype.util;

import optimizationprototype.gui.IGuiObserver;
import optimizationprototype.structure.SourceFile;

import java.util.Vector;

public abstract class SubjectBase {

    private Vector<IGuiObserver> observers;

    protected SubjectBase() {
        observers = new Vector<>();
    }

    public void attach(IGuiObserver observer) {
        observers.add(observer);
    }

    public void detach(IGuiObserver observer) {
        observers.remove(observer);
    }

    public void signal() {
        for (IGuiObserver ob : observers) {
            ob.update(this);
        }
    }

}
