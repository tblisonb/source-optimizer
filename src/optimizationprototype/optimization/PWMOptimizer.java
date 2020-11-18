package optimizationprototype.optimization;

import optimizationprototype.structure.*;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;
import optimizationprototype.util.SourceHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PWMOptimizer extends OptimizerBase {

    private Map<Integer, Integer> delayValues;
    private Vector<CodeElement> toggles;
    private String reg;
    private boolean invertedDutyCycle;

    protected PWMOptimizer(SourceFile file, boolean invertedDutyCycle) {
        super(file);
        this.invertedDutyCycle = invertedDutyCycle;
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

    private int calcFrequency() {
        int pre = 0, post = 0;
        if (toggles.size() == 2) {
            for (Integer line : delayValues.keySet()) {
                if (line > toggles.get(0).getLineNum())
                    pre += delayValues.get(line);
                else if (line > toggles.get(1).getLineNum() || line < toggles.get(0).getLineNum())
                    post += delayValues.get(line);
            }
        }
        int frequency = SourceHandler.getInstance().getDefaultFrequency() / (pre + post);
        Logger.getInstance().log(new Message("Pin toggle found with a frequency of " + frequency + "Hz.", Message.Type.GENERAL));
        return frequency;
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
        int dutyCycle = (invertedDutyCycle) ? ((pre + post > 0) ? ((post * 255) / (pre + post)) : 0) : ((pre + post > 0) ? ((pre * 255) / (pre + post)) : 0);
        Logger.getInstance().log(new Message("Setting up PWM instance with a duty cycle of " + ((dutyCycle * 100) / 255) + "%.", Message.Type.GENERAL));
        return dutyCycle;
    }

    private void getPinToggle(CodeElement element) {
        if (element.getType() == ElementType.STATEMENT && element.getCode().contains("PORT") && element.getCode().contains("=") && hasValidPin(element.getCode().substring(element.getCode().indexOf('=') + 1))) {
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

    private boolean hasValidPin(String substring) {
        return (substring.contains("PB3") || substring.contains("PD3") || substring.contains("PD5") || substring.contains("PD6"));
    }

    private String getValidPin(String substring) {
        String result = null;
        if (substring.contains("PB3")) {
            Logger.getInstance().log(new Message("Pin PB3 maps to OC2A, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR2A";
        }
        else if (substring.contains("PD3")) {
            Logger.getInstance().log(new Message("Pin PD3 maps to OC2B, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR2B";
        }
        else if(substring.contains("PD5")) {
            Logger.getInstance().log(new Message("Pin PD5 maps to OC0A, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR0B";
        }
        else if (substring.contains("PD6")) {
            Logger.getInstance().log(new Message("Pin PD6 maps to PC0B, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR0A";
        }
        else {
            Logger.getInstance().log(new Message("Output pin cannot be used for PWM output. Supported pins include: PB3, PD3, PD5, and PD6.", Message.Type.ERROR));
        }
        return result;
    }

    private void insertTimerDefines(CodeElement element) {
        if (reg != null) {
            calcFrequency();
            element.insertChildElement(new Statement(reg + " = 0x" + Integer.toString(calcDutyCycle(), 16).toUpperCase() + "; // Sets duty cycle for pin toggle", CodeElement.State.ADDED), 0);
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
