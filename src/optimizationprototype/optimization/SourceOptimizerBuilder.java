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
        OptimizerBase delayOp = new DelayOptimizer(file, isTimeSensitive);
        delayOp.applyOptimization();
        return true;
    }

    public boolean optimizeExternalInterrupts() {
        OptimizerBase interOp = new InterruptOptimizer(file);
        interOp.applyOptimization();
        return true;
    }

    public boolean optimizeBuiltinFunctions() {
        OptimizerBase builtinOp = new BuiltinFunctionOptimizer(file);
        builtinOp.applyOptimization();
        return true;
    }

    public boolean optimizeArithmetic() {
        OptimizerBase arithmeticOp = new ArithmeticOptimizer(file);
        arithmeticOp.applyOptimization();
        return true;
    }

    public boolean optimizePWM(boolean invertedDutyCycle) {
        OptimizerBase pwmOp = new PWMOptimizer(file, invertedDutyCycle);
        pwmOp.applyOptimization();
        return true;
    }
    
}
