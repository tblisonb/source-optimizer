package optimizationprototype.structure;

public class EmptyLine extends CodeElement {

    public EmptyLine() {
        super("", ElementType.EMPTY_LINE, false, 1);
    }

    public EmptyLine(String header) {
        super(header, ElementType.EMPTY_LINE, false, 1);
    }

    public EmptyLine(State state) {
        super("", ElementType.EMPTY_LINE, false, state, 1);
    }

    public EmptyLine(String header, State state) {
        super(header, ElementType.EMPTY_LINE, false, state, 1);
    }

}
