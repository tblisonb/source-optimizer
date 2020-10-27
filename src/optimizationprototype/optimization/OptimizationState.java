package optimizationprototype.optimization;

public class OptimizationState {

    private boolean timerOptimization;
    private boolean timeSensitiveTimer;
    private boolean interruptOptimization;
    private boolean builtinOptimization;
    private boolean arithmeticOptimization;

    public OptimizationState() {
        timerOptimization = false;
        timeSensitiveTimer = false;
        interruptOptimization = false;
        builtinOptimization = false;
        arithmeticOptimization = false;
    }

    public void setTimerOptimization(boolean timerOptimization) {
        this.timerOptimization = timerOptimization;
        this.timeSensitiveTimer = true;
    }

    public void setTimeSensitiveTimer(boolean isTimeSensitive) {
        this.timeSensitiveTimer = isTimeSensitive;
    }

    public void setInterruptOptimization(boolean interruptOptimization) {
        this.interruptOptimization = interruptOptimization;
    }

    public void setBuiltinOptimization(boolean builtinOptimization) {
        this.builtinOptimization = builtinOptimization;
    }

    public void setArithmeticOptimization(boolean arithmeticOptimization) {
        this.arithmeticOptimization = arithmeticOptimization;
    }

    public boolean getTimerOptimizationState() {
        return timerOptimization;
    }

    public boolean getTimeSensitiveExecutionState() {
        return timeSensitiveTimer;
    }

    public boolean getInterruptOptimizationState() {
        return interruptOptimization;
    }

    public boolean getBuiltinOptimizationState() {
        return builtinOptimization;
    }

    public boolean getArithmeticOptimizationState() {
        return arithmeticOptimization;
    }

    @Override
    public String toString() {
        return "Counter/Timer: " + timerOptimization + " w/ Time-Sensitive OoE: " + timeSensitiveTimer + "\nInterrupts: " + interruptOptimization + "\n";
    }

}
