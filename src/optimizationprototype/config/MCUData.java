package optimizationprototype.config;

import java.util.Vector;

public class MCUData {

	private Vector<PeripheralData> supportedPeripherals;
	private String name;
	private String family;
	private String architecture;
        
        public MCUData(/*JSONObject object*/) {
            supportedPeripherals = new Vector<>();
            //parseJSONObject();
        }
        
        public MCUData(Vector<PeripheralData> supportedPeripherals, String name, String family, String architecture) {
            this.supportedPeripherals = supportedPeripherals;
            this.name = name;
            this.family = family;
            this.architecture = architecture;
        }
        
        

}
