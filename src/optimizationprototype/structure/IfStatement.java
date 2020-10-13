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
public class IfStatement extends CodeElement {
    
    public IfStatement(String header) {
        super(header, ElementType.IfStatement, true);
    }
    
    @Override
    public String toString() {
        String indent = "";
        for (int i = 0; i < getIndentLevel(); i++) {
            indent += "    ";
        }
        String result = indent + getHeader();
        for (CodeElement elem : getChildren()) {
            result += "\n" + elem;
        }
        if (isBlock() && getHeader().contains("{")) {
            result += "\n" + indent + "}";
        }
        return result;
    }

}
