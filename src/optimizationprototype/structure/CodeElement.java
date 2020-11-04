/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author tblisonb
 */
public abstract class CodeElement {
    
    private List<CodeElement> childElements;
    private String code;
    private String inlineComment;
    private boolean isBlock;
    private ElementType type;
    private int indentLevel;
    private State state;
    private int lineNum, numLines;
    
    public CodeElement(String header, ElementType type, boolean isBlock, int numLines) {
        this(header, type, isBlock, State.UNCHANGED, numLines);
    }

    public CodeElement(String header, ElementType type, boolean isBlock, State state, int numLines) {
        this.code = header.trim();
        this.childElements = new LinkedList<>();
        this.indentLevel = 0;
        this.type = type;
        this.isBlock = isBlock;
        this.state = state;
        this.lineNum = 0;
        this.numLines = numLines;
        if (code.contains("//")) {
            inlineComment = code.substring(code.indexOf('/'));
            code = code.substring(0, code.indexOf('/'));
        }
        else this.inlineComment = "";
    }
    
    public ElementType getType() {
        return type;
    }
    
    public List<CodeElement> getChildren() {
        return childElements;
    }
    
    public String getHeader() {
        return this.code + ((this.code.length() > 0) ? " " : "") + inlineComment;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public int getNumLines() {
        return this.numLines;
    }

    public void setIndent(int indent) {
        indentLevel = indent;
        for (CodeElement child : childElements) {
            child.setIndent(indent + 1);
        }
    }

    public int setLineNum(int lineNum) {
        this.lineNum = lineNum;
        int nextLineNum = this.lineNum + 1;
        for (int i = 0; i < childElements.size(); i++) {
            nextLineNum += childElements.get(i).setLineNum(nextLineNum);
        }
        return this.numLines;
    }

    public void setHeader(String header) {
        this.code = header;
        if (code.contains("//")) {
            inlineComment = code.substring(code.indexOf('/'));
            code = code.substring(0, code.indexOf('/'));
        }
        else this.inlineComment = "";
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

    public String getInlineComment() {
        return this.inlineComment;
    }
    
    public boolean isBlock() {
        return this.isBlock;
    }
    
    public void addChildElement(CodeElement elem) {
        elem.setIndent(this.indentLevel + 1);
        this.numLines += elem.getNumLines();
        this.childElements.add(elem);
        this.setLineNum(this.lineNum);
    }
    
    public void addAllChildElements(List<CodeElement> elems) {
        int lineNum = 1;
        for (CodeElement elem : elems) {
            elem.setIndent(this.indentLevel + 1);
            this.numLines += elem.getNumLines();
            this.childElements.add(elem);
            this.setLineNum(this.lineNum);
        }
    }
    
    public void insertChildElement(CodeElement elem, int idx) {
        elem.setIndent(this.indentLevel + 1);
        this.numLines += elem.getNumLines();
        this.childElements.add(idx, elem);
        this.setLineNum(this.lineNum);
    }

    public void removeChild(int i) {
        this.childElements.remove(i);
        this.setLineNum(this.lineNum);
    }

    public CodeElement getEquivalentElement(CodeElement element) {
        if (this.equals(element))
            return this;
        else if (element.isBlock) {
            for (CodeElement elem : this.childElements) {
                CodeElement match = elem.getEquivalentElement(element);
                if (match != null)
                    return match;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        String indent = "";
        for (int i = 0; i < indentLevel; i++) {
            indent += "    ";
        }
        String result = indent + code + ((code.length() > 0) ? " " : "") + inlineComment;
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
            switch (typeBeingParsed) {
                case EMPTY_LINE:
                    addChildElement(new EmptyLine(contents.get(i)));
                    break;
                case MACRO:
                    addChildElement(new Macro(contents.get(i)));
                    break;
                case STATEMENT:
                    addChildElement(new Statement(contents.get(i)));
                    break;
                case FOR_LOOP:
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
                case WHILE_LOOP:
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
                case IF_STATEMENT:
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
            }
        }
    }
    
    private ElementType getType(String line) {
        // preprocessor directives
        if (line.contains("#")) {
            return ElementType.MACRO;
        }
        // loops/blocks
        else if (line.contains("for") && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.FOR_LOOP;
        }
        else if (line.contains("while") && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.WHILE_LOOP;
        }
        else if ((line.contains("if") || line.contains("else")) && line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.IF_STATEMENT;
        }
        // statements
        else if (line.contains(";")) {
            return ElementType.STATEMENT;
        }
        else {
            return ElementType.EMPTY_LINE;
        }
    }

    public CodeElement deepCopy() {
        CodeElement element = null;
        switch (this.getType()) {
            case MACRO:
                element = new Macro(this.code);
                break;
            case FOR_LOOP:
                element = new ForLoop(this.code);
                break;
            case WHILE_LOOP:
                element = new WhileLoop(this.code);
                break;
            case IF_STATEMENT:
                element = new IfStatement(this.code);
                break;
            case FUNCTION:
                element = new Function(this.code);
                break;
            case EMPTY_LINE:
                element = new EmptyLine();
                break;
            case STATEMENT:
                element = new Statement(this.code);
        }
        if (element != null && element.isBlock) {
            for (CodeElement child : childElements) {
                element.addChildElement(child.deepCopy());
            }
        }
        element.inlineComment = this.inlineComment;
        element.lineNum = this.lineNum;
        element.numLines = this.numLines;
        element.isBlock = this.isBlock;
        element.indentLevel = this.indentLevel;
        element.state = this.state;
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        CodeElement other;
        if (!(obj instanceof CodeElement)) return false;
        else other = (CodeElement) obj;
        if (!this.code.equals(other.code)) return false;
        if (!this.inlineComment.equals(other.inlineComment)) return false;
        if (this.isBlock != other.isBlock) return false;
        if (this.type != other.type) return false;
        if (this.indentLevel != other.indentLevel) return false;
        if (this.state != other.state) return false;
        if (this.isBlock) {
            if (this.childElements.size() != other.childElements.size()) return false;
            for (int i = 0; i < this.childElements.size(); i++) {
                if (!this.childElements.get(i).equals(other.childElements.get(i))) return false;
            }
        }
        return true;
    }

    public enum State {
        ADDED,
        REMOVED,
        MODIFIED,
        UNCHANGED
    }
    
}
