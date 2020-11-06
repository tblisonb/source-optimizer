package optimizationprototype.structure;

public class MultilineComment extends CodeElement {

    public MultilineComment(String header) {
        this(header, State.UNCHANGED);
    }

    public MultilineComment(String header, State state) {
        super(header, ElementType.MULTILINE_COMMENT, true, state, 1);
    }

    @Override
    public void addChildElement(CodeElement elem) {
        elem.setIndent(this.getIndentLevel());
        elem.setParentElement(this);
        super.childElements.add(elem);
        this.setLineNum(this.getLineNum());
        this.updateNumLines();
        if (parentElement != null)
            parentElement.updateNumLines();
    }

    @Override
    public String toString() {
        String result = getHeader();
        for (CodeElement elem : getChildren()) {
            result += "\n" + elem;
        }
        return result;
    }
}
