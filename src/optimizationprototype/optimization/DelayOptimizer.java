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

    public DelayOptimizer(SourceFile file, boolean isTimeSensitive) {
        super(file);
        this.isTimeSensitive = isTimeSensitive;
    }
    
    public void applyDelayOptimization() {
        Vector<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            insertGlobals();
            insertCounterVector();
            int delayValue = -1;
            for (CodeElement loop : whileLoops) {
                delayValue = insertLimitCheck(loop);
            }
            Vector<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    insertTimerDefines(func, delayValue);
                }
            }
        }
    }
    
    private void insertCounterVector() {
        Statement counterVisibility = new Statement("void __vector_14(void) __attribute__ ((signal, used, externally_visible));", CodeElement.State.ADDED);
        Function counterVector = new Function("void __vector_14(void) {", CodeElement.State.ADDED);
        if (this.isTimeSensitive) {
            IfStatement limitCheck = new IfStatement("if (count[0] < limit[0])", CodeElement.State.ADDED);
            Statement countInc = new Statement("count[0]++;", CodeElement.State.ADDED);
            limitCheck.addChildElement(countInc);
            IfStatement limitCheck2 = new IfStatement("if (count[1] < limit[1])", CodeElement.State.ADDED);
            Statement countInc2 = new Statement("count[1]++;", CodeElement.State.ADDED);
            limitCheck2.addChildElement(countInc2);
            counterVector.addChildElement(limitCheck);
            counterVector.addChildElement(limitCheck2);
        }
        else {
            IfStatement limitCheck = new IfStatement("if (count < limit)", CodeElement.State.ADDED);
            Statement countInc = new Statement("count++;", CodeElement.State.ADDED);
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
        if (this.isTimeSensitive) {
            Statement count = new Statement("volatile unsigned int count[2];", CodeElement.State.ADDED);
            Statement limit = new Statement("volatile unsigned int limit[2];", CodeElement.State.ADDED);
            file.getElements().insertElementAt(count, insertIndex++);
            file.getElements().insertElementAt(limit, insertIndex);
        }
        else {
            Statement count = new Statement("volatile unsigned int count;", CodeElement.State.ADDED);
            Statement limit = new Statement("volatile unsigned int limit;", CodeElement.State.ADDED);
            file.getElements().insertElementAt(count, insertIndex++);
            file.getElements().insertElementAt(limit, insertIndex);
        }
    }
    
    /**
     * 
     * @param element
     * @return Delay value found
     */
    private int insertLimitCheck(CodeElement element) {
        int delayValue = -1;
        for (int i = 0; i < element.getChildren().size(); i++) {
            if (element.getChildren().get(i).isBlock()) {
                insertLimitCheck(element.getChildren().get(i));
            }
            if ((element.getChildren().get(i).getType() == ElementType.STATEMENT) &&
                    (element.getChildren().get(i).getHeader().contains("_delay_ms"))) {
                if (this.isTimeSensitive) {
                    // extract delay value from function call
                    delayValue = Integer.parseInt(element.getChildren().get(i).getHeader().substring(
                            element.getChildren().get(i).getHeader().indexOf('(') + 1, 
                            element.getChildren().get(i).getHeader().indexOf(')')));
                    // add first if block
                    IfStatement limitCheck = new IfStatement("if (count[0] == limit[0]) {", CodeElement.State.ADDED);
                    limitCheck.setIndent(element.getChildren().get(i).getIndentLevel()+1);
                    Statement sregSave = new Statement("unsigned char state = SREG;", CodeElement.State.ADDED);
                    Statement interruptDisable = new Statement("__builtin_avr_cli();", CodeElement.State.ADDED);
                    Statement resetCount = new Statement("count[0] = 0;", CodeElement.State.ADDED);
                    Statement sregRestore = new Statement("SREG = state;", CodeElement.State.ADDED);
                    limitCheck.addChildElement(sregSave);
                    limitCheck.addChildElement(interruptDisable);
                    limitCheck.addAllChildElements(element.getChildren().subList(0, i));
                    limitCheck.addChildElement(resetCount);
                    limitCheck.addChildElement(sregRestore);
                    for (int j = 0; j <= i && element.getChildren().size() > 0; j++) {
                        element.getChildren().remove(0);
                    }
                    // add second if block
                    IfStatement limitCheck2 = new IfStatement("if (count[1] == limit[1]) {", CodeElement.State.ADDED);
                    limitCheck2.setIndent(element.getChildren().get(0).getIndentLevel()+1);
                    Statement sregSave2 = new Statement("unsigned char state = SREG;", CodeElement.State.ADDED);
                    Statement interruptDisable2 = new Statement("__builtin_avr_cli();", CodeElement.State.ADDED);
                    Statement resetCount2 = new Statement("count[1] = 0;", CodeElement.State.ADDED);
                    Statement sregRestore2 = new Statement("SREG = state;", CodeElement.State.ADDED);
                    limitCheck2.addChildElement(sregSave2);
                    limitCheck2.addChildElement(interruptDisable2);
                    limitCheck2.addAllChildElements(element.getChildren().subList(i, element.getChildren().size()));
                    limitCheck2.addChildElement(resetCount2);
                    limitCheck2.addChildElement(sregRestore2);
                    for (int j = 0; j <= i && element.getChildren().size() > 0; j++) {
                        element.getChildren().remove(0);
                    }
                    element.insertChildElement(limitCheck, i-1);
                    element.insertChildElement(limitCheck2, i);
                }
                else {
                    // extract delay value from function call
                    delayValue = Integer.parseInt(element.getChildren().get(i).getHeader().substring(
                            element.getChildren().get(i).getHeader().indexOf('(') + 1, 
                            element.getChildren().get(i).getHeader().indexOf(')')));
                    IfStatement limitCheck = new IfStatement("if (count == limit) {", CodeElement.State.ADDED);
                    limitCheck.setIndent(element.getChildren().get(i).getIndentLevel()+1);
                    Statement sregSave = new Statement("unsigned char state = SREG;", CodeElement.State.ADDED);
                    Statement interruptDisable = new Statement("__builtin_avr_cli();", CodeElement.State.ADDED);
                    Statement resetCount = new Statement("count = 0;", CodeElement.State.ADDED);
                    Statement sregRestore = new Statement("SREG = state;", CodeElement.State.ADDED);
                    limitCheck.addChildElement(sregSave);
                    limitCheck.addChildElement(interruptDisable);
                    limitCheck.addAllChildElements(element.getChildren().subList(0, i));
                    limitCheck.addChildElement(resetCount);
                    limitCheck.addChildElement(sregRestore);
                    for (int j = 0; j <= i && element.getChildren().size() > 0; j++) {
                        element.getChildren().remove(0);
                    }
                    element.insertChildElement(limitCheck, i-1);
                }
            }
        }
        return delayValue;
    }
    
    private void insertTimerDefines(CodeElement element, int delayValue) {
        element.insertChildElement(new Statement("OCR0A  = 0xF9;", CodeElement.State.ADDED), 0);
        element.insertChildElement(new Statement("TCCR0A = 0x02;", CodeElement.State.ADDED), 1);
        element.insertChildElement(new Statement("TCCR0B = 0x83;", CodeElement.State.ADDED), 2);
        element.insertChildElement(new Statement("TIMSK0 = 0x03;", CodeElement.State.ADDED), 3);
        if (this.isTimeSensitive) {
            element.insertChildElement(new Statement("count[0] = " + delayValue + ";", CodeElement.State.ADDED), 4);
            element.insertChildElement(new Statement("count[1] = 0;", CodeElement.State.ADDED), 5);
            element.insertChildElement(new Statement("limit[0] = " + delayValue + ";", CodeElement.State.ADDED), 6);
            element.insertChildElement(new Statement("limit[1] = " + delayValue + ";", CodeElement.State.ADDED), 7);
        }
        else {
            element.insertChildElement(new Statement("count = " + delayValue + ";", CodeElement.State.ADDED), 4);
            element.insertChildElement(new Statement("limit = " + delayValue + ";", CodeElement.State.ADDED), 5);
        }
    }
    
}
