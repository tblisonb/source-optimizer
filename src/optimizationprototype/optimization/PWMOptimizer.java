package optimizationprototype.optimization;

import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.SourceFile;
import optimizationprototype.structure.Statement;

import java.util.Vector;

public class PWMOptimizer extends OptimizerBase {

    protected PWMOptimizer(SourceFile file) {
        super(file);
    }

    @Override
    public void applyOptimization() {
        Vector<CodeElement> enableStatements = new Vector<>();
        Vector<CodeElement> disableStatements = new Vector<>();
        for (CodeElement elem : file.getElements()) {
            enableStatements.addAll(getInterruptEnable(elem));
            disableStatements.addAll(getInterruptDisable(elem));
        }
        updateInterruptCalls(enableStatements, disableStatements);
    }

    private void updateInterruptCalls(Vector<CodeElement> enableStatements, Vector<CodeElement> disableStatements) {
        for (CodeElement enable : enableStatements) {
            enable = new Statement("__builtin_avr_sei();");
        }
        for (CodeElement disable : disableStatements) {
            disable = new Statement("__builtin_avr_cli();");
        }
    }

    private Vector<CodeElement> getInterruptEnable(CodeElement element) {
        Vector<CodeElement> result = new Vector<>();
        if (element instanceof Statement && element.getHeader().contains("SREG") && element.getHeader().contains("=")) {
            try {
                int sregValue = Integer.parseInt(element.getHeader().substring(element.getHeader().indexOf('=') + 1, element.getHeader().indexOf(';')).trim());
                if (sregValue == 0x80) {
                    result.add(element);
                }
            }
            catch (Exception ex) {

            }
        }
        else if (element.isBlock()) {
            for (CodeElement child : element.getChildren()) {
                result.addAll(getInterruptEnable(child));
            }
        }
        return result;
    }

    private Vector<CodeElement> getInterruptDisable(CodeElement element) {
        Vector<CodeElement> result = new Vector<>();
        if (element instanceof Statement && element.getHeader().contains("SREG") && element.getHeader().contains("=")) {
            try {
                int sregValue = Integer.parseInt(element.getHeader().substring(element.getHeader().indexOf('=') + 1, element.getHeader().indexOf(';')).trim());
                if (sregValue == 0x00) {
                    result.add(element);
                }
            }
            catch (Exception ex) {

            }
        }
        else if (element.isBlock()) {
            for (CodeElement child : element.getChildren()) {
                result.addAll(getInterruptEnable(child));
            }
        }
        return result;
    }

}
