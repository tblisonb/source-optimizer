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
public class SourceOptimizerBuilder {
    
    private SourceFile file;
    
    public SourceOptimizerBuilder(SourceFile file) {
        this.file = file;
    }
    
    public SourceFile getOptimizedFile() {
        return this.file;
    }
    
    public boolean optimizeDelay(boolean isTimeSensitive) {
        DelayOptimizer delayOp = new DelayOptimizer(file, isTimeSensitive); // needs to be changed to take isTimeSensitive from user
        delayOp.applyDelayOptimization();
        return true;
    }

    public boolean optimizeExternalInterrupts() {
        InterruptOptimizer interOp = new InterruptOptimizer(file);
        interOp.applyInterruptOptimization();
        return true;
    }
    
}
