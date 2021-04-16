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
    private boolean invertedDutyCycle, preserveFrequency, isValidPin;

    protected PWMOptimizer(SourceFile file, boolean invertedDutyCycle, boolean preserveFrequency) {
        super(file);
        this.invertedDutyCycle = invertedDutyCycle;
        this.preserveFrequency = preserveFrequency;
        this.isValidPin = true;
    }

    public void applyOptimization() {
        List<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            delayValues = new HashMap<>();
            toggles = new Vector<>();
            getDelayOccurrences(whileLoops.get(0));
            if (delayValues.size() == 0) {
                Logger.getInstance().log(new Message("Could not apply PWM optimization; no delay values found.", Message.Type.ERROR));
                return;
            }
            getPinToggle(whileLoops.get(0));
            List<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            boolean result = false;
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    result = insertTimerDefines(func);
                }
            }
            if (result)
                removeDelayOccurrences(whileLoops.get(0));
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
        if (pre + post <= 0)
            return -1;
        int frequency = 1000000 / (pre + post);
        Logger.getInstance().log(new Message("Pin toggle found with a frequency of " + frequency + "Hz.", Message.Type.GENERAL));
        return frequency;
    }

    private int calcDutyCycle(int frequency) {
        int pre = 0, post = 0;
        if (toggles.size() == 2) {
            for (Integer line : delayValues.keySet()) {
                if (line > toggles.get(0).getLineNum())
                    pre += delayValues.get(line);
                else if (line > toggles.get(1).getLineNum() || line < toggles.get(0).getLineNum())
                    post += delayValues.get(line);
            }
        }
        int dutyCycle;
        if (preserveFrequency) {
            dutyCycle = (int) (((invertedDutyCycle) ? ((pre + post > 0) ? ((double) post / (double) (pre + post)) : 0d) : ((pre + post > 0) ? ((double) pre / (double) (pre + post)) : 0d)) * (double) frequency);
            Logger.getInstance().log(new Message("Setting up PWM instance with a duty cycle of " + ((dutyCycle * 100) / frequency) + "% and frequency of " + SourceHandler.getInstance().getDefaultFrequency() / frequency + " Hz.", Message.Type.GENERAL));
        }
        else {
            dutyCycle = (invertedDutyCycle) ? ((pre + post > 0) ? ((post * 255) / (pre + post)) : 0) : ((pre + post > 0) ? ((pre * 255) / (pre + post)) : 0);
            Logger.getInstance().log(new Message("Setting up PWM instance with a duty cycle of " + ((dutyCycle * 100) / 255) + "%.", Message.Type.GENERAL));
        }
        return dutyCycle;
    }

    private void getPinToggle(CodeElement element) {
        if (element.getType() == ElementType.STATEMENT && element.getCode().contains("PORT") && element.getCode().contains("=") &&
                hasValidPin(element.getCode().substring(element.getCode().indexOf('=') + 1))) {
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
        if (preserveFrequency) {
            boolean isValid = (substring.contains("PB1") || substring.contains("PB2"));
            if (!isValid && isValidPin) {
                Logger.getInstance().log(new Message("Option \"Preserve Frequency\" requires PWM output to be on either pin PB1 or PB2.", Message.Type.ERROR));
                isValidPin = false;
            }
            return isValid;
        }
        return (substring.contains("PB3") || substring.contains("PD3") || substring.contains("PD5") ||
                substring.contains("PD6") || substring.contains("PB1") || substring.contains("PB2"));
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
        else if(substring.contains("PB1")) {
            Logger.getInstance().log(new Message("Pin PB1 maps to OC1A, which can be used for PWM output with preserved frequency.", Message.Type.GENERAL));
            result = "OCR1A";
        }
        else if (substring.contains("PB2")) {
            Logger.getInstance().log(new Message("Pin PB2 maps to OC1B, which can be used for PWM output with preserved frequency.", Message.Type.GENERAL));
            result = "OCR1B";
        }
        else if(substring.contains("PD5")) {
            Logger.getInstance().log(new Message("Pin PD5 maps to OC0A, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR0B";
        }
        else if (substring.contains("PD6")) {
            Logger.getInstance().log(new Message("Pin PD6 maps to OC0B, which can be used for PWM output.", Message.Type.GENERAL));
            result = "OCR0A";
        }
        else {
            Logger.getInstance().log(new Message("Output pin cannot be used for PWM output. Supported pins include: PB1 or PB2 (required when 'Preserve Frequency' is selected) and PB3, PD3, PD5, and PD6 otherwise.", Message.Type.ERROR));
        }
        return result;
    }

    private boolean insertTimerDefines(CodeElement element) {
        if (reg != null) {
            if (preserveFrequency) {
                if (!reg.equals("OCR1A") && !reg.equals("OCR1B")) {
                    Logger.getInstance().log(new Message("The 'Preserve Frequency' option only works on pins PB1 and PB2 due to the requirement of utilizing the 16-bit timer instance.", Message.Type.ERROR));
                    return false;
                }
                int frequency = calcFrequency();
                if (frequency <= 0) {
                    Logger.getInstance().log(new Message("Frequency of the PWM signal cannot be below 1 Hz. Try shrinking the delay values if possible, or use the counter/timer optimization instead.", Message.Type.ERROR));
                    return false;
                }
                int top = SourceHandler.getInstance().getDefaultFrequency() / frequency;
                if (top > 65536) {
                    Logger.getInstance().log(new Message("The provided delay values result in a timer top value of greater than 2^16 which will not provide the intended result. Consider reducing the set target frequency of the microcontroller.", Message.Type.ERROR));
                    return false;
                }
                else if (top < 0) {
                    Logger.getInstance().log(new Message("Could not apply PWM optimization; no delay values found. Consider disabling the Counter/Timer optimization if being used in conjunction with the PWM optimization.", Message.Type.ERROR));
                    return false;
                }
                int dutyCycle = calcDutyCycle(top);
                element.insertChildElement(new Statement("ICR1 = 0x" + Integer.toString(top, 16).toUpperCase() + "; // Sets the top limit for the 16-bit timer instance", CodeElement.State.ADDED), 0);
                element.insertChildElement(new Statement(reg + " = 0x" + Integer.toString(dutyCycle, 16).toUpperCase() + "; // Sets duty cycle for pin toggle", CodeElement.State.ADDED), 1);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << COM" + reg.charAt(3) + reg.charAt(4) + "1); // Set none-inverted mode", CodeElement.State.ADDED), 2);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << WGM" + reg.charAt(3) + "1) | (1 << WGM" + reg.charAt(3) + "0); // Set fast PWM mode", CodeElement.State.ADDED), 3);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "B = (1 << WGM" + reg.charAt(3) + "3) | (1 << WGM" + reg.charAt(3) + "2); // Additional PWM flags", CodeElement.State.ADDED), 4);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "B = (1 << CS" + reg.charAt(3) + "0); // Set no prescaler (1:1 w/ clock)", CodeElement.State.ADDED), 5);
                return true;
            }
            else {
                element.insertChildElement(new Statement(reg + " = 0x" + Integer.toString(calcDutyCycle(0), 16).toUpperCase() + "; // Sets duty cycle for pin toggle", CodeElement.State.ADDED), 0);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << COM" + reg.charAt(3) + reg.charAt(4) + "1); // Set none-inverted mode", CodeElement.State.ADDED), 1);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "A = (1 << WGM" + reg.charAt(3) + "1) | (1 << WGM" + reg.charAt(3) + "0); // Set fast PWM mode", CodeElement.State.ADDED), 2);
                element.insertChildElement(new Statement("TCCR" + reg.charAt(3) + "B = (1 << CS" + reg.charAt(3) + "1); // Set a prescaler value of clk/8", CodeElement.State.ADDED), 3);
                return true;
            }
        }
        return false;
    }

    private void getDelayOccurrences(CodeElement element) {
        try {
            if ((element.getType() == ElementType.STATEMENT) && (element.getCode().contains("_delay_us")) && element.getState() != CodeElement.State.REMOVED) {
                int delayValue = Integer.parseInt(element.getHeader().substring(element.getHeader().indexOf('(') + 1, element.getHeader().indexOf(')')));
                delayValues.put(element.getLineNum(), delayValue);
            } else if ((element.getType() == ElementType.STATEMENT) && (element.getCode().contains("_delay_ms")) && element.getState() != CodeElement.State.REMOVED) {
                // multiply value by 1000 to get the adjusted number of cycles
                int delayValue = 1000 * Integer.parseInt(element.getHeader().substring(element.getHeader().indexOf('(') + 1, element.getHeader().indexOf(')')));
                delayValues.put(element.getLineNum(), delayValue);
            } else if (element.isBlock()) {
                for (CodeElement elem : element.getChildren()) {
                    getDelayOccurrences(elem);
                }
            }
        }
        catch (NumberFormatException e) {
            Logger.getInstance().log(new Message("Could not apply PWM optimization on line " +
                    element.getLineNum() + " delay value argument must be an immediate value.", Message.Type.ERROR));
        }
    }

    private void removeDelayOccurrences(CodeElement element) {
        if ((element.getType() == ElementType.STATEMENT) && (element.getCode().contains("_delay_us")) && element.getState() != CodeElement.State.REMOVED) {
            element.setHeader("// Removed: " + element.getHeader());
            element.setState(CodeElement.State.REMOVED);
        }
        else if ((element.getType() == ElementType.STATEMENT) && (element.getCode().contains("_delay_ms")) && element.getState() != CodeElement.State.REMOVED) {
            element.setHeader("// Removed: " + element.getHeader());
            element.setState(CodeElement.State.REMOVED);
        }
        else if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                removeDelayOccurrences(elem);
            }
        }
    }

}
