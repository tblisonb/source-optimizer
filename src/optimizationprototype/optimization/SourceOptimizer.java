/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizationprototype.optimization;

import optimizationprototype.structure.SourceFile;

/**
 *
 * @author tblisonb
 */
public class SourceOptimizer {
    
    private SourceFile file;
    
    public SourceOptimizer(SourceFile file) {
        this.file = file;
    }
    
    public void setSourceFile(SourceFile file) {
        this.file = file;
    }
    
    public SourceFile getOptimizedFile() {
        assembleOptimzations();
        return this.file;
    }
    
    private boolean assembleOptimzations() {
        // TBD (check which optimizations are enabled/possible)
        //optimizeDelay();
        optimizeExternalInterrupts();
        return true;
    }
    
    private boolean optimizeDelay() {
        DelayOptimizer delayOp = new DelayOptimizer(file, true); // needs to be changed to take isTimeSensitive from user
        delayOp.applyDelayOptimization();
        return true;
    }
    
    private boolean optimizeExternalInterrupts() {
        InterruptOptimizer interOp = new InterruptOptimizer(file);
        interOp.applyInterruptOptimization();
        return true;
    }
    
}
