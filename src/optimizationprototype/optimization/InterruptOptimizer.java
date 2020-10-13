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
public class InterruptOptimizer {
    
    private SourceFile file;
    
    public InterruptOptimizer(SourceFile file) {
        this.file = file;
    }
    
    public void applyInterruptOptimization() {
        Vector<CodeElement> whileLoops = file.getElementsOfType(ElementType.WhileLoop);
        // only insert optimizations if a single main while loop is found
        if (whileLoops.size() == 1) {
            CodeElement element = removePinCheck((WhileLoop) whileLoops.get(0));
            int pcIntX;
            if (element.getHeader().contains("PB"))
                pcIntX = element.getHeader().charAt(element.getHeader().indexOf("PB") + 2) - '0';
            else if (element.getHeader().contains("PC"))
                pcIntX = 8 + element.getHeader().charAt(element.getHeader().indexOf("PC") + 2) - '0';
            else
                pcIntX = 16 + element.getHeader().charAt(element.getHeader().indexOf("PD") + 2) - '0';
            int pciX = -1;
            Vector<CodeElement> functions = file.getElementsOfType(ElementType.Function);
            for (CodeElement func : functions) {
                if (func.getHeader().contains("main(")) {
                    pciX = insertPinChangeDefines(func, pcIntX);
                }
            }
            insertButtonVector(element, pciX);
        }
    }
    
    private CodeElement removePinCheck(WhileLoop loop) {
        CodeElement result = null;
        for (int i = 0; i < loop.getChildren().size(); i++) {
            CodeElement elem = loop.getChildren().get(i);
            if (elem.getType() == ElementType.IfStatement && elem.getHeader().contains("PIN") &&
                    elem.getHeader().contains("&") && elem.getHeader().contains("<<")) {
                result = elem;
                loop.getChildren().remove(i);
                i = loop.getChildren().size();
            }
        }
        return result;
    }
    
    private void insertButtonVector(CodeElement contents, int pcIntX) {
        Statement interruptVisibility = new Statement("void __vector_" + (pcIntX + 3) + "(void) __attribute__ ((signal, used, externally_visible));");
        Function interruptVector = new Function("void __vector_" + (pcIntX + 3) + "(void) {");
        interruptVector.addChildElement(contents);
        file.addElement(interruptVisibility);
        file.addElement(interruptVector);
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
        element.insertChildElement(new Statement("__builtin_avr_sei();"), 0);
        element.insertChildElement(new Statement("PCICR  = " + pciXX + ";"), 1);
        element.insertChildElement(new Statement(pcMskX + " = " + pcIntBit + ";"), 2);
        return result;
    }
    
}
