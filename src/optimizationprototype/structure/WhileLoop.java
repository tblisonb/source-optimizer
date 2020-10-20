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
public class WhileLoop extends CodeElement {

    public WhileLoop(String header) {
        super(header, ElementType.WHILE_LOOP, true);
    }

    public WhileLoop(String header, State state) {
        super(header, ElementType.WHILE_LOOP, true, state);
    }

}
