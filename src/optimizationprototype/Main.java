package optimizationprototype;

import optimizationprototype.gui.OptimizationGUI;
import optimizationprototype.util.Logger;
import optimizationprototype.util.ProcessManager;
import optimizationprototype.util.SourceHandler;

public class Main {
    
    public static void main(String[] args) {
        if (args.length == 1)
            ProcessManager.getInstance().setCompilerDir(args[0]);

        OptimizationGUI gui = new OptimizationGUI();

        SourceHandler.getInstance().attach(gui);
        Logger.getInstance().attach(gui);

        gui.initGUI();

        gui.setVisible(true);

    }

}
