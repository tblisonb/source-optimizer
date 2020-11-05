/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.structure;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author tblisonb
 */
public class SourceFile {

    private List<CodeElement> elements;
    
    public SourceFile() {
        this.elements = new LinkedList<>();
    }

    public List<CodeElement> getElements() {
        return this.elements;
    }

    public void addElement(CodeElement elem) {
        if (elem == null)
            return;
        if (elements.size() == 0)
            elem.setLineNum(1);
        else {
            elem.setLineNum(elements.get(elements.size() - 1).getLineNum() + elements.get(elements.size() - 1).getNumLines());
        }
        this.elements.add(elem);
    }

    public void insertElement(CodeElement elem, int idx) {
        if (elem == null || idx >= elements.size())
            return;
        if (idx == 0)
            elem.setLineNum(1);
        elements.add(idx, elem);
        for (int i = idx; i < elements.size(); i++) {
            elements.get(i).setLineNum(elements.get(i - 1).getLineNum() + elements.get(i - 1).getNumLines());
        }
    }

    public void updateLineNumbers() {
        if (elements.size() > 0) {
            elements.get(0).setLineNum(1);
            for (int i = 1; i < elements.size(); i++) {
                elements.get(i).setLineNum(elements.get(i - 1).getLineNum() + elements.get(i - 1).getNumLines());
            }
        }
    }
    
    public List<CodeElement> getElementsOfType(ElementType type) {
        List<CodeElement> result = new LinkedList<>();
        for (CodeElement elem : elements) {
            List<CodeElement> elemResult = searchElementForType(type, elem);
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
    
    private List<CodeElement> searchElementForType(ElementType type, CodeElement elem) {
        List<CodeElement> result = new LinkedList<>();
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
