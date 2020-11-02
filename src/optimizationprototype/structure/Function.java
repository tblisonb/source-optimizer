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
public class Function extends CodeElement {
    
    public Function(String header) {
        super(header, ElementType.FUNCTION, true, 2);
    }

    public Function(String header, State state) {
        super(header, ElementType.FUNCTION, true, state, 2);
    }

}
