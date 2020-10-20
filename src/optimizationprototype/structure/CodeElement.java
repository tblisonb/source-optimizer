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
public abstract class CodeElement {
    
    private Vector<CodeElement> childElements;
    private String code;
    private boolean isBlock;
    private ElementType type;
    private int indentLevel;
    private State state;
    
    public CodeElement(String header, ElementType type, boolean isBlock) {
        this.code = header.trim();
        this.childElements = new Vector<>();
        this.indentLevel = 0;
        this.type = type;
        this.isBlock = isBlock;
        this.state = State.UNCHANGED;
    }
    
    public ElementType getType() {
        return type;
    }
    
    public Vector<CodeElement> getChildren() {
        return childElements;
    }
    
    public String getHeader() {
        return this.code;
    }
    
    public void setIndent(int indent) {
        indentLevel = indent;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    public int getIndentLevel() {
        return indentLevel;
    }
    
    public boolean isBlock() {
        return this.isBlock;
    }
    
    public void addChildElement(CodeElement elem) {
        elem.setIndent(this.indentLevel + 1);
        this.childElements.add(elem);
    }
    
    public void addAllChildElements(List<CodeElement> elems) {
        for (CodeElement elem : elems) {
            elem.setIndent(this.indentLevel + 1);
            this.childElements.add(elem);
        }
    }
    
    public void insertChildElement(CodeElement elem, int idx) {
        elem.setIndent(this.indentLevel + 1);
        this.childElements.insertElementAt(elem, idx);
    }
    
    @Override
    public String toString() {
        String indent = "";
        for (int i = 0; i < indentLevel; i++) {
            indent += "    ";
        }
        String result = indent + code;
        for (CodeElement elem : childElements) {
            result += "\n" + elem;
        }
        if (isBlock) {
            result += "\n" + indent + "}";
        }
        return result;
    }
    
    public void init(Vector<String> contents) {
        for (int i = 0; i < contents.size(); i++) {
            ElementType typeBeingParsed = getType(contents.get(i));
            if (null != typeBeingParsed) switch (typeBeingParsed) {
                case Macro:
                    addChildElement(new Macro(contents.get(i)));
                    break;
                case Statement:
                    addChildElement(new Statement(contents.get(i)));
                    break;
                case ForLoop:
                    String header = contents.get(i++);
                    ForLoop fl = new ForLoop(header);
                    Vector<String> forLoopContents = new Vector<>();
                    int numOpenBraces = 1, j;
                    if (header.contains("}"))
                        numOpenBraces--;
                    for (j = i; j < contents.size() && numOpenBraces > 0; j++) {
                        if (contents.get(j).contains("{"))
                            numOpenBraces++;
                        if (contents.get(j).contains("}"))
                            numOpenBraces--;
                        if (numOpenBraces > 0) {
                            forLoopContents.add(contents.get(j));
                        }
                    }
                    fl.init(forLoopContents);
                    addChildElement(fl);
                    i = j - 1;
                    break;
                case WhileLoop:
                    header = contents.get(i++);
                    WhileLoop wl = new WhileLoop(header);
                    Vector<String> whileLoopContents = new Vector<>();
                    numOpenBraces = 1;
                    if (header.contains("}"))
                        numOpenBraces--;
                    for (j = i; j < contents.size() && numOpenBraces > 0; j++) {
                        if (contents.get(j).contains("{"))
                            numOpenBraces++;
                        if (contents.get(j).contains("}"))
                            numOpenBraces--;
                        if (numOpenBraces > 0) {
                            whileLoopContents.add(contents.get(j));
                        }
                    }
                    wl.init(whileLoopContents);
                    addChildElement(wl);
                    i = j - 1;
                    break;
                case IfStatement:
                    header = contents.get(i++);
                    IfStatement is = new IfStatement(header);
                    Vector<String> ifStatementContents = new Vector<>();
                    numOpenBraces = 1;
                    if (header.contains("}"))
                        numOpenBraces--;
                    for (j = i; j < contents.size() && numOpenBraces > 0; j++) {
                        if (contents.get(j).contains("{"))
                            numOpenBraces++;
                        if (contents.get(j).contains("}"))
                            numOpenBraces--;
                        if (numOpenBraces > 0) {
                            ifStatementContents.add(contents.get(j));
                        }
                    }
                    is.init(ifStatementContents);
                    addChildElement(is);
                    i = j - 1;
                    break;
                default:
                    break;
            }
        }
    }
    
    private ElementType getType(String line) {
        // preprocessor directives
        if (line.contains("#")) {
            return ElementType.Macro;
        }
        // loops/blocks
        else if (line.contains("for") && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.ForLoop;
        }
        else if (line.contains("while") && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.WhileLoop;
        }
        else if ((line.contains("if") || line.contains("else")) && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.IfStatement;
        }
        // statememts
        else {
            return ElementType.Statement;
        }
    }

    public CodeElement deepCopy() {
        CodeElement element;
        switch (getType()) {
            case Macro:
                element = new Macro(this.code);
                break;
            case ForLoop:
                element = new ForLoop(this.code);
                break;
            case WhileLoop:
                element = new WhileLoop(this.code);
                break;
            case IfStatement:
                element = new IfStatement(this.code);
                break;
            default:
                element = new Statement(this.code);
        }
        if (element.isBlock) {
            for (CodeElement child : childElements) {
                element.addChildElement(child.deepCopy());
            }
        }
        return element;
    }

    public enum State {
        ADDED,
        REMOVED,
        MODIFIED,
        UNCHANGED
    }
    
}
