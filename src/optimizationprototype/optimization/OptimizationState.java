package optimizationprototype.optimization;

public class OptimizationState {

    private boolean timerOptimization;
    private boolean timeSensitiveTimer;
    private boolean interruptOptimization;
    private boolean builtinOptimization;
    private boolean arithmeticOptimization;
    private boolean pwmOptimization;
    private boolean invertedPwm;

    public OptimizationState() {
        timerOptimization = false;
        timeSensitiveTimer = false;
        interruptOptimization = false;
        builtinOptimization = false;
        arithmeticOptimization = false;
        pwmOptimization = false;
        invertedPwm = false;
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

    public void setPwmOptimization(boolean pwmOptimization) {
        this.pwmOptimization = pwmOptimization;
        invertedPwm = false;
    }

    public void setInvertedPwm(boolean invertedPwm) {
        this.invertedPwm = invertedPwm;
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

    public boolean getPWMOptimizationState() {
        return pwmOptimization;
    }

    public boolean getInvertedPWM() {
        return invertedPwm;
    }

}
