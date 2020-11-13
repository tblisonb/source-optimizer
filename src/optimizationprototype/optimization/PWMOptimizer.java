package optimizationprototype.optimization;

import optimizationprototype.structure.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PWMOptimizer extends OptimizerBase {

    private Map<Integer, Integer> delayValues;
    private Vector<CodeElement> toggles;
    private String reg;

    protected PWMOptimizer(SourceFile file) {
        super(file);
    }

    public void applyOptimization() {
        List<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            delayValues = new HashMap<>();
            toggles = new Vector<>();
            getDelayOccurrences(whileLoops.get(0));
            getPinToggle(whileLoops.get(0));
            List<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    insertTimerDefines(func);
                }
            }
        }
    }

    private int calcDutyCycle() {
        int pre = 0, post = 0;
        if (toggles.size() == 2) {
            for (Integer line : delayValues.keySet()) {
                if (line > toggles.get(0).getLineNum())
                    pre += delayValues.get(line);
                else if (line > toggles.get(1).getLineNum() || line < toggles.get(0).getLineNum())
                    post += delayValues.get(line);
            }
        }
        return (pre + post > 0) ? ((pre * 255) / (pre + post)) : 0;
    }

    private void getPinToggle(CodeElement element) {
        if (element.getType() == ElementType.STATEMENT && element.getCode().contains("PORT") && element.getCode().contains("=") && getValidPin(element.getCode().substring(element.getCode().indexOf('=') + 1)) != null) {
            toggles.add(element);
            reg = getValidPin(element.getCode().substring(element.getCode().indexOf('=') + 1));
            element.setHeader("// Removed: " + element.getHeader());
            element.setState(CodeElement.State.REMOVED);
        }
        else if (element.isBlock()) {
            for (CodeElement child : element.getChildren()) {
                getPinToggle(child);
            }
        }
    }

    private String getValidPin(String substring) {
        String result = null;
        if (substring.contains("PB3")) {
            result = "OCR2A";
        }
        else if (substring.contains("PD3")) {
            result = "OCR2B";
        }
        else if(substring.contains("PD5")) {
            result = "OCR0A";
        }
        else if (substring.contains("PD6")) {
            result = "OCR0B";
        }
        return result;
    }

    private void insertTimerDefines(CodeElement element) {
        if (reg != null) {
            element.insertChildElement(new Statement(reg + " = 0x" + Integer.toString(calcDutyCycle(), 16) + "; // Sets duty cycle for pin toggle", CodeElement.State.ADDED), 0);
            element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << COM" + reg.charAt(3) + reg.charAt(4) + "1); // Set none-inverted mode", CodeElement.State.ADDED), 1);
            element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << WGM" + reg.charAt(3) + "1) | (1 << WGM" + reg.charAt(3) + "0); // Set fast PWM mode", CodeElement.State.ADDED), 2);
            element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "B = (1 << CS" + reg.charAt(3) + "1); // Set a prescaler value of clk/8", CodeElement.State.ADDED), 3);
        }
    }

    private void getDelayOccurrences(CodeElement element) {
        if ((element.getType() == ElementType.STATEMENT) && (element.getHeader().contains("_delay_us"))) {
            int delayValue = Integer.parseInt(element.getHeader().substring(element.getHeader().indexOf('(') + 1, element.getHeader().indexOf(')')));
            delayValues.put(element.getLineNum(), delayValue);
            element.setHeader("// Removed: " + element.getHeader());
            element.setState(CodeElement.State.REMOVED);
        }
        else if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                getDelayOccurrences(elem);
            }
        }
    }

}
