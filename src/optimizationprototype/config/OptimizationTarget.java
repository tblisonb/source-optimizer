package optimizationprototype.config;

import java.util.Vector;

public class OptimizationTarget {

	private Vector<String> sourceTargets;
	private Vector<Peripheral> supportedPeripherals;
	private Target target;
        
        public OptimizationTarget(/*JSONObject object*/) {
            this.sourceTargets = new Vector<>();
            this.supportedPeripherals = new Vector<>();
            //parseJSONObject();
        }
        
        public OptimizationTarget(Vector<String> sourceTargets, Vector<Peripheral> supportedPeripherals, Target target) {
            this.sourceTargets = sourceTargets;
            this.supportedPeripherals = supportedPeripherals;
            this.target = target;
        }

	public Vector<Peripheral> getSupportedPeripherals() {
            return this.supportedPeripherals;
	}
        
        public Target getTarget() {
            return this.target;
        }

}
