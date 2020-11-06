package optimizationprototype.optimization;

import optimizationprototype.structure.*;
import optimizationprototype.util.SourceHandler;

import java.util.List;
import java.util.Vector;

public class PWMOptimizer extends OptimizerBase {

    private Vector<Integer> delayValues;

    protected PWMOptimizer(SourceFile file) {
        super(file);
    }

    public void applyOptimization() {/*
        List<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            delayValues = getDelayOccurrences(whileLoops.get(0));
            insertGlobals();
            insertCounterVector();
            for (CodeElement loop : whileLoops) {
                insertLimitCheck(loop);
            }
            List<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    insertTimerDefines(func, delayValues);
                }
            }
        }*/
    }
    /*
    private void insertCounterVector() {
        file.addElement(new EmptyLine("// Declare timer interrupt vector as being called by the internal interrupt", CodeElement.State.ADDED));
        Statement counterVisibility = new Statement("void __vector_14(void) __attribute__ ((signal, used, externally_visible));", CodeElement.State.ADDED);
        Function counterVector = new Function("void __vector_14(void) { // Timer interrupt vector which will be called by the timer hardware on compare match", CodeElement.State.ADDED);
        for (int i = 0; (i < delayValues.size() + 1 && isTimeSensitive) || (i < delayValues.size() && !isTimeSensitive); i++) {
            IfStatement limitCheck = new IfStatement("if (count[" + i + "] < limit[" + i + "]) { // keep count less than limit if the timer has expired", CodeElement.State.ADDED);
            Statement countInc = new Statement("count[" + i + "]++; // add 'tick' to count, indicating a 1ms increase", CodeElement.State.ADDED);
            limitCheck.addChildElement(countInc);
            counterVector.addChildElement(limitCheck);
        }
        file.addElement(counterVisibility);
        file.addElement(counterVector);
    }

    private void insertGlobals() {
        int insertIndex = 0;
        while (file.getElements().get(insertIndex).getType() == ElementType.MACRO) {
            insertIndex++;
        }
        Statement count = new Statement("volatile unsigned int count[" + ((isTimeSensitive) ? delayValues.size() + 1 : delayValues.size()) + "]; // Timer instances for each of the blocks of code previously separated by software delays", CodeElement.State.ADDED);
        Statement limit = new Statement("volatile unsigned int limit[" + ((isTimeSensitive) ? delayValues.size() + 1 : delayValues.size()) + "]; // Timer limit instances to compare 'count' instances against to determine delay expiry", CodeElement.State.ADDED);
        file.insertElement(count, insertIndex++);
        file.insertElement(limit, insertIndex);
    }

    private void insertLimitCheck(CodeElement element) {
        int delayIndex = 0;
        for (int i = 0; i < element.getChildren().size() && delayIndex < delayValues.size(); i++) {
            if (element.getChildren().get(i).isBlock()) {
                insertLimitCheck(element.getChildren().get(i));
            }
            if ((element.getChildren().get(i).getType() == ElementType.STATEMENT) &&
                    (element.getChildren().get(i).getHeader().contains("_delay_us"))) {
                element.removeChild(i);
                IfStatement limitCheck = new IfStatement("if (count[" + delayIndex + "] == limit[" + delayIndex + "]) {", CodeElement.State.ADDED);
                limitCheck.setIndent(element.getChildren().get(i).getIndentLevel()+1);
                Statement sregSave = new Statement("unsigned char state = SREG; // Retrieve SREG state", CodeElement.State.ADDED);
                Statement interruptDisable = new Statement("__builtin_avr_cli(); // Disable global interrupts to verify timing integrity of the following code", CodeElement.State.ADDED);
                Statement resetCount = new Statement("count = 0; // Reset counter instance which will effectively reset the timer", CodeElement.State.ADDED);
                Statement sregRestore = new Statement("SREG = state; // Restore previous state of SREG", CodeElement.State.ADDED);
                limitCheck.addChildElement(sregSave);
                limitCheck.addChildElement(interruptDisable);
                limitCheck.addAllChildElements(element.getChildren().subList(delayIndex, i));
                limitCheck.addChildElement(resetCount);
                limitCheck.addChildElement(sregRestore);
                for (int j = 0; j < i; j++) {
                    element.removeChild(delayIndex);
                }
                element.insertChildElement(limitCheck, delayIndex++);
            }
        }
        if (isTimeSensitive && delayIndex == delayValues.size()) {
            IfStatement finalLimitCheck = new IfStatement("if (count[" + delayIndex + "] == limit[" + delayIndex + "]) {", CodeElement.State.ADDED);
            Statement sregSave2 = new Statement("unsigned char state = SREG; // Retrieve SREG state", CodeElement.State.ADDED);
            Statement interruptDisable2 = new Statement("__builtin_avr_cli(); // Disable global interrupts to verify timing integrity of the following code", CodeElement.State.ADDED);
            Statement resetCount2 = new Statement("count[" + delayIndex + "] = 0; // Reset counter instance which will effectively reset the timer", CodeElement.State.ADDED);
            Statement sregRestore2 = new Statement("SREG = state; // Restore previous state of SREG", CodeElement.State.ADDED);
            finalLimitCheck.addChildElement(sregSave2);
            finalLimitCheck.addChildElement(interruptDisable2);
            for (CodeElement elem : element.getChildren().subList(delayIndex, element.getChildren().size())) {
                elem.setIndent(finalLimitCheck.getIndentLevel() + 1);
            }
            finalLimitCheck.addAllChildElements(element.getChildren().subList(delayIndex, element.getChildren().size()));
            finalLimitCheck.addChildElement(resetCount2);
            finalLimitCheck.addChildElement(sregRestore2);
            for (int j = delayIndex - 1; j < element.getChildren().size(); j++) {
                if (delayIndex < element.getChildren().size())
                    element.getChildren().remove(delayIndex);
            }
            element.addChildElement(finalLimitCheck);
        }
    }

    private void insertTimerDefines(CodeElement element, Vector<Integer> delayValues) {
        element.insertChildElement(new Statement("OCR0A  = 0xF9; // Sets an output compare value for a 1ms tick", CodeElement.State.ADDED), 0);
        element.insertChildElement(new Statement("TCCR0A = 0x02; // Clear compare register on compare match", CodeElement.State.ADDED), 1);
        element.insertChildElement(new Statement("TCCR0B = 0x83; // Force output compare A, and sets a prescaler value of 1/64 for a 1ms tick", CodeElement.State.ADDED), 2);
        element.insertChildElement(new Statement("TIMSK0 = 0x03; // Enables timer interrupts for compare match A", CodeElement.State.ADDED), 3);
        if (this.isTimeSensitive) {
            int totalDelay = 0, prevDelay = 0;
            for (Integer i : delayValues) {
                totalDelay += i;
            }
            int i;
            for (i = 0; i < delayValues.size(); i++) {
                element.insertChildElement(new Statement("count[" + i + "] = " + (totalDelay - prevDelay) + ";", CodeElement.State.ADDED), 4 + i);
                element.insertChildElement(new Statement("limit[" + i + "] = " + totalDelay + ";", CodeElement.State.ADDED), 5 + i);
                prevDelay += delayValues.get(i);
            }
            element.insertChildElement(new Statement("count[" + i + "] = " + (totalDelay - prevDelay) + ";", CodeElement.State.ADDED), 4 + i);
            element.insertChildElement(new Statement("limit[" + i + "] = " + totalDelay + ";", CodeElement.State.ADDED), 5 + i);
        }
        else {
            for (int i = 0; i < delayValues.size(); i++) {
                element.insertChildElement(new Statement("count[" + i + "] = " + delayValues.get(i) + ";", CodeElement.State.ADDED), 4 + i);
                element.insertChildElement(new Statement("limit[" + i + "] = " + delayValues.get(i) + ";", CodeElement.State.ADDED), 5 + i);
            }
        }
    }

    private Vector<Integer> getDelayOccurrences(CodeElement element) {
        Vector<Integer> result = new Vector<>();
        for (int i = 0; i < element.getChildren().size(); i++) {
            if (element.getChildren().get(i).isBlock()) {
                getDelayOccurrences(element.getChildren().get(i));
            }
            if ((element.getChildren().get(i).getType() == ElementType.STATEMENT) &&
                    (element.getChildren().get(i).getHeader().contains("_delay_ms"))) {
                int delayValue = Integer.parseInt(element.getChildren().get(i).getHeader().substring(
                        element.getChildren().get(i).getHeader().indexOf('(') + 1,
                        element.getChildren().get(i).getHeader().indexOf(')')));
                result.add(delayValue);
            }
        }
        return result;
    }*/

}
