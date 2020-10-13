package optimizationprototype.gui;

import java.util.Vector;

/* REF: http://www.java2s.com/Tutorials/Java/Swing_How_to/JTree/Create_CheckBox_Node_JTree.htm */

class NamedVector extends Vector {
    String name;
    public NamedVector(String name) {
        this.name = name;
    }
    public NamedVector(String name, Object elements[]) {
        this.name = name;
        for (int i = 0, n = elements.length; i < n; i++) {
            add(elements[i]);
        }
    }
    public String toString() {
        return "[" + name + "]";
    }
}