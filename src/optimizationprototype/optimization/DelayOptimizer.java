/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.optimization;

import optimizationprototype.structure.*;

import java.util.Vector;

/**
 *
 * @author tblisonb
 */
public class DelayOptimizer extends OptimizerBase {

    private boolean isTimeSensitive;
    private Vector<Integer> delayValues;

    public DelayOptimizer(SourceFile file, boolean isTimeSensitive) {
        super(file);
        this.isTimeSensitive = isTimeSensitive;
        this.delayValues = new Vector<>();
    }
    
    public void applyOptimization() {
        Vector<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            delayValues = getDelayOccurrences(whileLoops.get(0));
            insertGlobals();
            insertCounterVector();
            for (CodeElement loop : whileLoops) {
                insertLimitCheck(loop);
            }
            Vector<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    insertTimerDefines(func, delayValues);
                }
            }
        }
    }
    
    private void insertCounterVector() {
        Statement counterVisibility = new Statement("void __vector_14(void) __attribute__ ((signal, used, externally_visible));", CodeElement.State.ADDED);
        Function counterVector = new Function("void __vector_14(void) {", CodeElement.State.ADDED);
        for (int i = 0; (i < delayValues.size() + 1 && isTimeSensitive) || (i < delayValues.size() && !isTimeSensitive); i++) {
            IfStatement limitCheck = new IfStatement("if (count[" + i + "] < limit[" + i + "]) {", CodeElement.State.ADDED);
            Statement countInc = new Statement("count[" + i + "]++;", CodeElement.State.ADDED);
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
        Statement count = new Statement("volatile unsigned int count[" + ((isTimeSensitive) ? delayValues.size() + 1 : delayValues.size()) + "];", CodeElement.State.ADDED);
        Statement limit = new Statement("volatile unsigned int limit[" + ((isTimeSensitive) ? delayValues.size() + 1 : delayValues.size()) + "];", CodeElement.State.ADDED);
        file.getElements().insertElementAt(count, insertIndex++);
        file.getElements().insertElementAt(limit, insertIndex);
    }
    
    /**
     * 
     * @param element
     */
    private void insertLimitCheck(CodeElement element) {
        int delayIndex = 0;
        //System.out.println(element + "\n");
        for (int i = 0; i < element.getChildren().size() && delayIndex < delayValues.size(); i++) {
            if (element.getChildren().get(i).isBlock()) {
                insertLimitCheck(element.getChildren().get(i));
            }
            if ((element.getChildren().get(i).getType() == ElementType.STATEMENT) &&
                (element.getChildren().get(i).getHeader().contains("_delay_ms"))) {
                element.getChildren().remove(i);
                IfStatement limitCheck = new IfStatement("if (count[" + delayIndex + "] == limit[" + delayIndex + "]) {", CodeElement.State.ADDED);
                limitCheck.setIndent(element.getChildren().get(i).getIndentLevel()+1);
                Statement sregSave = new Statement("unsigned char state = SREG;", CodeElement.State.ADDED);
                Statement interruptDisable = new Statement("__builtin_avr_cli();", CodeElement.State.ADDED);
                Statement resetCount = new Statement("count = 0;", CodeElement.State.ADDED);
                Statement sregRestore = new Statement("SREG = state;", CodeElement.State.ADDED);
                limitCheck.addChildElement(sregSave);
                limitCheck.addChildElement(interruptDisable);
                limitCheck.addAllChildElements(element.getChildren().subList(delayIndex, i));
                limitCheck.addChildElement(resetCount);
                limitCheck.addChildElement(sregRestore);
                //System.out.println(limitCheck + "\n");
                for (int j = 0; j < i; j++) {
                    element.getChildren().remove(delayIndex);
                }
                element.insertChildElement(limitCheck, delayIndex++);
                //System.out.println(element + "\n");
            }
        }
        if (isTimeSensitive && delayIndex == delayValues.size()) {
            IfStatement finalLimitCheck = new IfStatement("if (count[" + delayIndex + "] == limit[" + delayIndex + "]) {", CodeElement.State.ADDED);
            finalLimitCheck.setIndent(element.getChildren().get(0).getIndentLevel());
            Statement sregSave2 = new Statement("unsigned char state = SREG;", CodeElement.State.ADDED);
            Statement interruptDisable2 = new Statement("__builtin_avr_cli();", CodeElement.State.ADDED);
            Statement resetCount2 = new Statement("count[1] = 0;", CodeElement.State.ADDED);
            Statement sregRestore2 = new Statement("SREG = state;", CodeElement.State.ADDED);
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
            //System.out.println(finalLimitCheck + "\n");
            element.insertChildElement(finalLimitCheck, element.getChildren().size());
        }
    }
    
    private void insertTimerDefines(CodeElement element, Vector<Integer> delayValues) {
        element.insertChildElement(new Statement("OCR0A  = 0xF9;", CodeElement.State.ADDED), 0);
        element.insertChildElement(new Statement("TCCR0A = 0x02;", CodeElement.State.ADDED), 1);
        element.insertChildElement(new Statement("TCCR0B = 0x83;", CodeElement.State.ADDED), 2);
        element.insertChildElement(new Statement("TIMSK0 = 0x03;", CodeElement.State.ADDED), 3);
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
    }
    
}
