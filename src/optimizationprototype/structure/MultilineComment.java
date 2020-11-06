package optimizationprototype.structure;

public class MultilineComment extends CodeElement {

    public MultilineComment(String header) {
        this(header, State.UNCHANGED);
    }

    public MultilineComment(String header, State state) {
        super(header, ElementType.MULTILINE_COMMENT, true, state, 2);
        this.addChildElement(new EmptyLine(" */"));
    }

    @Override
    public void addChildElement(CodeElement elem) {
        elem.setIndent(this.getIndentLevel());
        elem.setParentElement(this);
        super.childElements.add((super.childElements.size() > 0) ? super.childElements.size() - 1 : 0, elem);
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
