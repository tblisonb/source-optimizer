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
public class Statement extends CodeElement {
    
    public Statement(String codeLine) {
        super(codeLine, ElementType.Statement, false);
    }

    public Statement(String codeLine, State state) {
        super(codeLine, ElementType.Statement, false, state);
    }

}
