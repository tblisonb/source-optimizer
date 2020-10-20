package optimizationprototype.optimization;

public class OptimizationState {

    private boolean timerOptimization;
    private boolean timeSensitiveTimer;
    private boolean interruptOptimization;

    public OptimizationState() {
        timerOptimization = false;
        timeSensitiveTimer = false;
        interruptOptimization = false;
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

    public boolean getTimerOptimizationState() {
        return timerOptimization;
    }

    public boolean getTimeSensitiveExecutionState() {
        return timeSensitiveTimer;
    }

    public boolean getInterruptOptimizationState() {
        return interruptOptimization;
    }

    @Override
    public String toString() {
        return "Counter/Timer: " + timerOptimization + " w/ Time-Sensitive OoE: " + timeSensitiveTimer + "\nInterrupts: " + interruptOptimization + "\n";
    }
}
