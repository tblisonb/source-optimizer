package optimizationprototype.optimization;

import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.SourceFile;
import optimizationprototype.structure.Statement;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;
import optimizationprototype.util.SourceHandler;

import java.util.Vector;

public class ArithmeticOptimizer extends OptimizerBase {

    private final int MAX_MULTIPLY_UNROLL = 8;

    protected ArithmeticOptimizer(SourceFile file) {
        super(file);
    }

    @Override
    public void applyOptimization() {
        Vector<CodeElement> targets = new Vector<>();
        Vector<Integer> suggestions = new Vector<>();
        for (CodeElement elem : file.getElements()) {
            targets.addAll(getTargetStatement(elem));
            if (SourceHandler.getInstance().isSuggestionsEnabled()) {
                suggestions.addAll(getSuggestions(elem));
            }
        }
        for (CodeElement elem : targets) {
            generateUnrolledMultiply(elem);
        }
        if (SourceHandler.getInstance().isSuggestionsEnabled()) {
            for (Integer i : suggestions) {
                Logger.getInstance().log(new Message("Division before multiplication on line: " + i + ", consider switching operands to avoid losing precision", Message.Type.SUGGESTION));
            }
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
            Logger.getInstance().log(new Message("Unrolled multiply statement with value " + operand1 + " for " + operand2 + " iterations.", Message.Type.GENERAL));
        }
        else if (isOperand1Numeric && !isOperand2Numeric && operand1Value <= MAX_MULTIPLY_UNROLL) {
            String replacement = "";
            for (int i = 0; i < operand1Value - 1; i++) {
                replacement += operand2 + " + ";
            }
            replacement += operand2;
            element.setHeader(element.getHeader().substring(0, index2 + 1) + replacement + element.getHeader().substring(index1));
            element.setState(CodeElement.State.MODIFIED);
            Logger.getInstance().log(new Message("Unrolled multiply statement with variable \"" + operand2 + "\" for " + operand1 + " iterations.", Message.Type.GENERAL));
        }
        else if (!isOperand1Numeric && isOperand2Numeric && operand2Value <= MAX_MULTIPLY_UNROLL) {
            String replacement = "";
            for (int i = 0; i < operand2Value - 1; i++) {
                replacement += operand1 + " + ";
            }
            replacement += operand1;
            element.setHeader(element.getHeader().substring(0, index2 + 1) + replacement + element.getHeader().substring(index1));
            element.setState(CodeElement.State.MODIFIED);
            Logger.getInstance().log(new Message("Unrolled multiply statement with variable \"" + operand1 + "\" for " + operand2 + " iterations.", Message.Type.GENERAL));
        }
        else {
            Logger.getInstance().log(new Message("Could not apply arithmetic substitution on line " + element.getLineNum() + ". Either both the operands are non-immediate " +
                    "integer values, or the minimum value between both operands exceeded the maximum threshold of 8.", Message.Type.ERROR));
        }
    }

    private Vector<CodeElement> getTargetStatement(CodeElement element) {
        Vector<CodeElement> targets = new Vector<>();
        if (element instanceof Statement && element.getCode().contains("=") && element.getCode().contains("*") ) {
            targets.add(element);
        }
        else if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                targets.addAll(getTargetStatement(elem));
            }
        }
        return targets;
    }

    private Vector<Integer> getSuggestions(CodeElement element) {
        Vector<Integer> lines = new Vector<>();
        if (element instanceof Statement && element.getCode().contains("=") && element.getCode().contains("*") &&
            element.getCode().contains("/") && (element.getCode().indexOf('/') < element.getCode().indexOf('*'))) {
            lines.add(element.getLineNum());
        }
        else if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                lines.addAll(getSuggestions(elem));
            }
        }
        return lines;
    }

}
