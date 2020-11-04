/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.structure;

import java.util.List;
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

    public Vector<CodeElement> getElements() {
        return this.elements;
    }

    public void addElement(CodeElement elem) {
        if (elem == null)
            return;
        if (elements.size() == 0)
            elem.setLineNum(1);
        else {
            System.out.println(elements.get(elements.size() - 1).getLineNum() + ", " + elements.get(elements.size() - 1).getNumLines());
            elem.setLineNum(elements.get(elements.size() - 1).getLineNum() + elements.get(elements.size() - 1).getNumLines());
        }
        this.elements.add(elem);
    }

    public void insertElement(CodeElement elem, int idx) {
        if (elem == null || idx >= elements.size())
            return;
        if (idx == 0)
            elem.setLineNum(1);
        elements.insertElementAt(elem, idx);
        for (int i = idx; i < elements.size(); i++) {
            elements.get(i).setLineNum(elements.get(i - 1).getLineNum() + elements.get(i - 1).getNumLines());
        }
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

    public CodeElement getEquivalentElement(CodeElement element) {
        for (CodeElement elem : elements) {
            CodeElement match = elem.getEquivalentElement(element);
            if (match != null)
                return match;
        }
        return null;
    }

    private CodeElement getNestedElement(CodeElement element) {
        if (element.equals(element)) {
            return element;
        }
        else if (element.isBlock()) {
            for (CodeElement child : element.getChildren()) {
                return getNestedElement(child);
            }
        }
        return null;
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
            if (line.getType() != ElementType.EMPTY_LINE)
                data += line + ((line.getInlineComment() != null) ? line.getInlineComment() : "") + "\n";
        }
        return data + '\n';
    }

    public SourceFile deepCopy() {
        SourceFile fileCopy = new SourceFile();
        for (CodeElement element : elements) {
            fileCopy.addElement(element.deepCopy());
        }
        return fileCopy;
    }
    
}
