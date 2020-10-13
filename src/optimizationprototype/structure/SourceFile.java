/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.structure;

import java.util.Vector;

/**
 *
 * @author tblisonb
 */
public class SourceFile {
    
    private Vector<CodeElement> elements;
    
    public SourceFile() {
        this.elements = new Vector<>();
    }
    
    public void addElement(CodeElement element) {
        this.elements.add(element);
    }
    
    public Vector<CodeElement> getElements() {
        return this.elements;
    }
    
    public Vector<CodeElement> getElementsOfType(ElementType type) {
        Vector<CodeElement> result = new Vector<>();
        for (CodeElement elem : elements) {
            Vector<CodeElement> elemResult = searchElementForType(type, elem);
            if (elemResult != null) {
                result.addAll(elemResult);
            }
        }
        return result;
    }
    
    private Vector<CodeElement> searchElementForType(ElementType type, CodeElement elem) {
        Vector<CodeElement> result = new Vector<>();
        // base case
        if (elem.getType() == type) {
            result.add(elem);
        }
        // recursive case
        else if (elem.isBlock()) {
            for (CodeElement child : elem.getChildren()) {
                result.addAll(searchElementForType(type, child));
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        String data = "";
        for (CodeElement line : elements) {
            data += line;
        }
        return data + '\n';
    }
    
}
