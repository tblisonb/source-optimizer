/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.optimization;

import optimizationprototype.structure.*;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author tblisonb
 */
public class InterruptOptimizer extends OptimizerBase {

    public InterruptOptimizer(SourceFile file) {
        super(file);
    }
    
    public void applyOptimization() {
        List<CodeElement> whileLoops = file.getElementsOfType(ElementType.WHILE_LOOP);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            Vector<CodeElement> element = removePinCheck((WhileLoop) whileLoops.get(0));
            if (element.size() == 0)
                return;
            int pcIntX;
            if (element.get(0).getHeader().contains("PB"))
                pcIntX = element.get(0).getHeader().charAt(element.get(0).getHeader().indexOf("PB") + 2) - '0';
            else if (element.get(0).getHeader().contains("PC"))
                pcIntX = 8 + element.get(0).getHeader().charAt(element.get(0).getHeader().indexOf("PC") + 2) - '0';
            else
                pcIntX = 16 + element.get(0).getHeader().charAt(element.get(0).getHeader().indexOf("PD") + 2) - '0';
            int pciX = -1;
            List<CodeElement> functions = file.getElementsOfType(ElementType.FUNCTION);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    pciX = insertPinChangeDefines(func, pcIntX);
                }
            }
            insertButtonVector(element, pciX);
        }
    }
    
    private Vector<CodeElement> removePinCheck(WhileLoop loop) {
        Vector<CodeElement> result = new Vector<>();
        boolean hasElse = false;
        int start = 0, end = 0;
        for (int i = 0; i < loop.getChildren().size(); i++) {
            CodeElement elem = loop.getChildren().get(i);
            if (elem.getType() == ElementType.IF_STATEMENT && elem.getHeader().contains("PIN") &&
                    elem.getHeader().contains("&") && elem.getHeader().contains("<<")) {
                result.add(elem);
                start = i;
                if (i < loop.getChildren().size() - 1 && loop.getChildren().get(i + 1).getCode().contains("else")) {
                    hasElse = true;
                }
                else {
                    end = i + 1;
                }
            }
            else if (hasElse) {
                result.add(loop.getChildren().get(i));
                if (i < loop.getChildren().size() && loop.getChildren().get(i).getCode().contains("else")) {
                    hasElse = false;
                    i = loop.getChildren().size();
                    end = i;
                }
            }
        }
        for (int j = start; j < end - 1; j++) {
            loop.removeChild(start);
        }
        return result;
    }
    
    private void insertButtonVector(Vector<CodeElement> contents, int pcIntX) {
        file.addElement(new EmptyLine("// Declare external interrupt vector as being called by the internal interrupt", CodeElement.State.ADDED));
        Statement interruptVisibility = new Statement("void __vector_" + (pcIntX + 3) + "(void) __attribute__ ((signal, used, externally_visible));", CodeElement.State.ADDED);
        Function interruptVector = new Function("void __vector_" + (pcIntX + 3) + "(void) { // Interrupt vector for external pin change", CodeElement.State.ADDED);
        for (CodeElement elem : contents) {
            interruptVector.addChildElement(elem);
        }
        file.addElement(interruptVisibility);
        file.addElement(interruptVector);
        Logger.getInstance().log(new Message("Created button interrupt vector.", Message.Type.GENERAL));
    }
    
    private int insertPinChangeDefines(CodeElement element, int pcIntX) {
        int result;
        String pciXX, pcMskX, pcIntBit;
        if (pcIntX >= 0 && pcIntX <= 7) {
            pciXX = "0x01";
            pcMskX = "PCMSK0";
            pcIntBit = "0x" + (1 << pcIntX);
            result = 0;
        }
        else if (pcIntX >= 8 && pcIntX <= 15) {
            pciXX = "0x02";
            pcMskX = "PCMSK1";
            pcIntBit = "0x" + (1 << (pcIntX - 8));
            result = 1;
        }
        else {
            pciXX = "0x04";
            pcMskX = "PCMSK2";
            pcIntBit = "0x" + (1 << (pcIntX - 16));
            result = 2;
        }
        element.insertChildElement(new Statement("__builtin_avr_sei(); // Enable global interrupts", CodeElement.State.ADDED), 0);
        element.insertChildElement(new Statement("PCICR  = " + pciXX + "; // Sets the appropriate pin bank for the pin being used", CodeElement.State.ADDED), 1);
        element.insertChildElement(new Statement(pcMskX + " = " + pcIntBit + "; // Set the pin being used for external interrupt", CodeElement.State.ADDED), 2);
        Logger.getInstance().log(new Message("Added register defines to configure external interrupts on Pin " + pcIntX, Message.Type.GENERAL));
        return result;
    }
    
}
