package optimizationprototype.config;

import java.util.Vector;

public class PeripheralData {

	private String name;

	private Vector<String> target;
        
        public PeripheralData(String name) {
            this.name = name;
            this.target = new Vector<>();
        }
        
        public void addTarget(String targetStr) {
            this.target.add(targetStr);
        }

}
