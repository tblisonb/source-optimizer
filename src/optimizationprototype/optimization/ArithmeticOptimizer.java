package optimizationprototype.optimization;

import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.SourceFile;
import optimizationprototype.structure.Statement;
import optimizationprototype.util.Logger;

import java.util.Vector;

public class ArithmeticOptimizer extends OptimizerBase {

    private final int MAX_MULTIPLY_UNROLL = 8;

    protected ArithmeticOptimizer(SourceFile file) {
        super(file);
    }

    @Override
    public void applyOptimization() {
        Vector<CodeElement> targets = new Vector<>();
        for (CodeElement elem : file.getElements()) {
            targets.addAll(getTargetStatement(elem));
        }
        for (CodeElement elem : targets) {
            generateUnrolledMultiply(elem);
        }
    }

    private void generateUnrolledMultiply(CodeElement element) {
        String str1 = element.getHeader().substring(element.getHeader().indexOf("*") + 1),
                str2 = element.getHeader().substring(0, element.getHeader().indexOf("*"));
        int index1 = 0, index2 = 0;
        String operand1 = "", operand2 = "";
        boolean flag1 = true, flag2 = true;
        for (int i = 0; i < str1.length(); i++) {
            if (str1.charAt(i) == ' ' && flag1);
            else if (Character.isJavaIdentifierPart(str1.charAt(i))) {
                operand1 += str1.charAt(i);
                flag1 = false;
            }
            else {
                index1 = element.getHeader().length() - i + operand1.length();
                i = str1.length();
            }
        }
        for (int i = str2.length() - 1; i >= 0; i--) {
            if (str2.charAt(i) == ' ' && flag2);
            else if (Character.isJavaIdentifierPart(str2.charAt(i))) {
                operand2 = str2.charAt(i) + operand2;
                flag2 = false;
            }
            else {
                index2 = i;
                i = -1;
            }
        }
        insertUnrolledMultiply(element, index1, index2, operand1, operand2);
    }

    private void insertUnrolledMultiply(CodeElement element, int index1, int index2, String operand1, String operand2) {
        boolean isOperand1Numeric = true, isOperand2Numeric;
        Integer operand1Value = null, operand2Value = null;
        try {
            operand1Value = Integer.parseInt(operand1);
            isOperand1Numeric = true;
        }
        catch (Exception ex) {
            isOperand1Numeric = false;
        }
        try {
            operand2Value = Integer.parseInt(operand2);
            isOperand2Numeric = true;
        }
        catch (Exception ex) {
            isOperand2Numeric = false;
        }
        if (isOperand1Numeric && isOperand2Numeric && Math.min(operand1Value, operand2Value) <= MAX_MULTIPLY_UNROLL) {
            String replacement = "";
            int min = Math.min(operand1Value, operand2Value);
            int max = Math.max(operand1Value, operand2Value);
            for (int i = 0; i < min - 1; i++) {
                replacement += max + " + ";
            }
            replacement += max;
            element.setHeader(element.getHeader().substring(0, index2 + 1) + replacement + element.getHeader().substring(index1));
            element.setState(CodeElement.State.MODIFIED);
            Logger.getInstance().log("Unrolled multiply statement with value " + operand1 + " for " + operand2 + " iterations.");
        }
        else if (isOperand1Numeric && !isOperand2Numeric && operand1Value <= MAX_MULTIPLY_UNROLL) {
            String replacement = "";
            for (int i = 0; i < operand1Value - 1; i++) {
                replacement += operand2 + " + ";
            }
            replacement += operand2;
            element.setHeader(element.getHeader().substring(0, index2 + 1) + replacement + element.getHeader().substring(index1));
            element.setState(CodeElement.State.MODIFIED);
            Logger.getInstance().log("Unrolled multiply statement with variable \"" + operand2 + "\" for " + operand1 + " iterations.");
        }
        else if (!isOperand1Numeric && isOperand2Numeric && operand2Value <= MAX_MULTIPLY_UNROLL) {
            String replacement = "";
            for (int i = 0; i < operand2Value - 1; i++) {
                replacement += operand1 + " + ";
            }
            replacement += operand1;
            element.setHeader(element.getHeader().substring(0, index2 + 1) + replacement + element.getHeader().substring(index1));
            element.setState(CodeElement.State.MODIFIED);
            Logger.getInstance().log("Unrolled multiply statement with variable \"" + operand1 + "\" for " + operand2 + " iterations.");
        }
        else {
            Logger.getInstance().log("Could not apply arithmetic substitution. Either both the operands are non-immediate " +
                    "integer values, or the minimum value between both operands exceeded the maximum threshold of 8.");
        }
    }

    private Vector<CodeElement> getTargetStatement(CodeElement element) {
        Vector<CodeElement> targets = new Vector<>();
        if (element instanceof Statement && element.getHeader().contains("=") && element.getHeader().contains("*") ) {
            targets.add(element);
        }
        else if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                targets.addAll(getTargetStatement(elem));
            }
        }
        return targets;
    }

}
