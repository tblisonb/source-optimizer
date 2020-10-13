package optimizationprototype.util;

import optimizationprototype.config.MCUData;
import optimizationprototype.config.OptimizationTarget;
import optimizationprototype.config.Peripheral;
import optimizationprototype.config.Target;
import optimizationprototype.optimization.SourceOptimizer;
import optimizationprototype.structure.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class SourceHandler extends SubjectBase {
    
    private Vector<MCUData> supportedMCUs;
    private static SourceHandler instance = null;
    private Vector<OptimizationTarget> targets;
    private SourceFile originalFile;
    private Vector<String> originalCode = null;
    private String optimizedCode = null;

    private SourceHandler() {
        super();
    }

    public static SourceHandler getInstance() {
        if (instance == null)
            instance = new SourceHandler();
        return instance;
    }

    public boolean parseFile(String fileName) {
        readFile(fileName);
        originalFile = new SourceFile();
        if (originalCode == null)
            return false;
        ElementType typeBeingParsed = null;
        for (int i = 0; i < originalCode.size(); i++) {
            typeBeingParsed = getType(originalCode.get(i));
            if (null != typeBeingParsed) switch (typeBeingParsed) {
                case Macro:
                    this.originalFile.addElement(new Macro(originalCode.get(i)));
                    break;
                case Statement:
                    this.originalFile.addElement(new Statement(originalCode.get(i)));
                    break;
                case Function:
                    String header = originalCode.get(i++);
                    Function f = new Function(header);
                    Vector<String> functionContents = new Vector<>();
                    int numOpenBraces = 1, j;
                    if (header.contains("}"))
                        numOpenBraces--;
                    // add all code lines for the current function
                    for (j = i; j < originalCode.size() && numOpenBraces > 0; j++) {
                        if (originalCode.get(j).contains("{"))
                            numOpenBraces++;
                        if (originalCode.get(j).contains("}"))
                            numOpenBraces--;
                        if (numOpenBraces > 0) {
                            functionContents.add(originalCode.get(j));
                        }
                    }
                    f.init(functionContents);
                    this.originalFile.addElement(f);
                    i = j;
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public void setTargetEnabled(Target target, boolean flag) {
        // TBD
    }

    public void importConfig(String fileName) {
        // TBD
    }
    
    public boolean writeOptimizedFile() {
        // apply optimizations
        SourceOptimizer op = new SourceOptimizer(originalFile);
        SourceFile optimized = op.getOptimizedFile();
        optimizedCode = "";
        // write optimized file (TBD)
        for (CodeElement elem : optimized.getElements()) {
            optimizedCode += elem + "\n";
        }
        signal();
        return true;
    }

    public String getOptimizedCode() {
        return optimizedCode;
    }

    public Vector<String> getOriginalCode() {
        return originalCode;
    }

    private void readFile(String fileName) {
        boolean headerFlag = true;  // determines if the header is being read
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String lineBuffer = reader.readLine();
            originalCode = new Vector<String>();
            while (lineBuffer != null) {
                originalCode.add(lineBuffer);
                lineBuffer = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getInstance().log("Error, could not find file: " + fileName);
        } catch (IOException ex) {
            Logger.getInstance().log("Error, could not read file: " + fileName);
        }
    }
    
    private void initSupportedTargets(String fileName) {
        Vector<String> sourceTargets = new Vector<>();
        Vector<Peripheral> peripherals = new Vector<>();
        sourceTargets.add("_delay_ms");
        sourceTargets.add("_delay_us");
        peripherals.add(Peripheral.Timer);
        OptimizationTarget target = new OptimizationTarget(sourceTargets, peripherals, Target.Delay);
        this.targets.add(target);
    }
    
    private void initSupportedMCUs(String fileName) {
        // TBD
    }
    
    private ElementType getType(String line) {
        // preprocessor directives
        if (line.contains("#")) {
            return ElementType.Macro;
        }
        // functions
        else if (line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.Function;
        }
        // variables
        else if (line.contains(";") || line.contains("//")) {
            return ElementType.Statement;
        }
        else {
            return null;
        }
    }

}
