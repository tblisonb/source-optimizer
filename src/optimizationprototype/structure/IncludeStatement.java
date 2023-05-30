/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package optimizationprototype.structure;

/**
 *
 * @author tlisonbee
 */
public class IncludeStatement extends Macro {
    
    private final String opFilePath;
    
    public IncludeStatement(String codeLine, String filePath) {
        super(codeLine);
        this.opFilePath = filePath;
    }
    
    public String getIncludeFilePath() {
        return this.opFilePath;
    }
    
}
