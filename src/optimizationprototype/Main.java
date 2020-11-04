package optimizationprototype;

import optimizationprototype.gui.OptimizationGUI;
import optimizationprototype.util.Logger;
import optimizationprototype.util.SourceHandler;

public class Main {
    
    public static void main(String[] args) {

        OptimizationGUI gui = new OptimizationGUI();

        SourceHandler.getInstance().attach(gui);
        Logger.getInstance().attach(gui);

        gui.initGUI();

        gui.setVisible(true);

    }

}
