/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.structure;

/**
 *
 * @author tblisonb
 */
public class Macro extends CodeElement {
    
    public Macro(String codeLine) {
        this(codeLine, State.UNCHANGED);
    }

    public Macro(String codeLine, State state) {
        super(codeLine, ElementType.MACRO, false, state, 1);
    }

}
