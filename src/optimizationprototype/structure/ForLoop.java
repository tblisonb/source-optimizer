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
public class ForLoop extends CodeElement {
    
    public ForLoop(String header) {
        super(header, ElementType.FOR_LOOP, true, 2);
    }

    public ForLoop(String header, State state) {
        super(header, ElementType.FOR_LOOP, true, state, 2);
    }

}
