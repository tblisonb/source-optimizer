package optimizationprototype.structure;

public class EmptyLine extends CodeElement {

    public EmptyLine() {
        super("", ElementType.EMPTY_LINE, false);
    }

    public EmptyLine(State state) {
        super("", ElementType.EMPTY_LINE, false, state);
    }

}
