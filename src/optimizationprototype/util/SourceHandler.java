package optimizationprototype.util;

import optimizationprototype.config.MCUData;
import optimizationprototype.config.OptimizationTarget;
import optimizationprototype.config.Peripheral;
import optimizationprototype.config.Target;
import optimizationprototype.optimization.OptimizationState;
import optimizationprototype.optimization.SourceOptimizerBuilder;
import optimizationprototype.structure.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class SourceHandler extends SubjectBase {
    
    private Vector<MCUData> supportedMCUs;
    private static SourceHandler instance = new SourceHandler();
    private Vector<OptimizationTarget> targets;
    private String optimizedCode;
    private SourceFile originalFile, optimizedFile;
    private Vector<String> originalCode;
    private boolean suggestionsEnabled;

    private SourceHandler() {
        super();
        supportedMCUs = new Vector<>();
        targets = new Vector<>();
        optimizedCode = null;
        originalFile = new SourceFile();
        optimizedFile = null;
        originalCode = null;
        suggestionsEnabled = true;
    }

    public static SourceHandler getInstance() {
        return instance;
    }

    public void reset() {
        supportedMCUs = new Vector<>();
        targets = new Vector<>();
        optimizedCode = null;
        originalFile = new SourceFile();
        optimizedFile = null;
        originalCode = null;
        suggestionsEnabled = true;
    }

    public boolean parseFile(String fileName) {
        if (!fileName.substring(fileName.length() - 2).equalsIgnoreCase(".c")) {
            Logger.getInstance().log(new Message("Unsupported file type: " + fileName, Message.Type.ERROR));
            return false;
        }
        if (!readFile(fileName))
            return false;
        if (originalCode == null)
            return false;
        ElementType typeBeingParsed = null;
        for (int i = 0; i < originalCode.size(); i++) {
            typeBeingParsed = getType(originalCode.get(i));
            switch (typeBeingParsed) {
                case EMPTY_LINE:
                    this.originalFile.addElement(new EmptyLine(originalCode.get(i)));
                    break;
                case MACRO:
                    this.originalFile.addElement(new Macro(originalCode.get(i)));
                    break;
                case STATEMENT:
                    this.originalFile.addElement(new Statement(originalCode.get(i)));
                    break;
                case FUNCTION:
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
            }
        }
        originalFile.updateLineNumbers();
        return true;
    }

    public boolean isSuggestionsEnabled() {
        return suggestionsEnabled;
    }

    public void setSuggestionsEnabled(boolean suggestionsEnabled) {
        this.suggestionsEnabled = suggestionsEnabled;
    }

    public void setTargetEnabled(Target target, boolean flag) {
        // TBD
    }

    public void importConfig(String fileName) {
        // TBD
    }
    
    public void generateOptimizedFile(OptimizationState state) {
        // apply optimizations
        SourceOptimizerBuilder op = new SourceOptimizerBuilder(originalFile.deepCopy());
        if (state.getInterruptOptimizationState()) {
            op.optimizeExternalInterrupts();
        }
        if (state.getTimerOptimizationState()) {
            op.optimizeDelay(state.getTimeSensitiveExecutionState());
        }
        if (state.getBuiltinOptimizationState()) {
            op.optimizeBuiltinFunctions();
        }
        if (state.getArithmeticOptimizationState()) {
            op.optimizeArithmetic();
        }
        optimizedFile = op.getOptimizedFile();
        optimizedFile.updateLineNumbers();
        /*
        for (CodeElement elem : optimizedFile.getElements()) {
            System.out.println("Element: \"" + elem.getHeader() + "\", Line Num: " + elem.getLineNum() + ", Num Lines: " + elem.getNumLines());
            if (elem.isBlock()) for (CodeElement child : elem.getChildren()) {
                System.out.println("  * Child: \"" + child.getHeader() + "\", Line Num: " + child.getLineNum() + ", Num Lines: " + child.getNumLines());
                if (elem.isBlock()) for (CodeElement grandChild : child.getChildren()) {
                    System.out.println("      - GrandChild: \"" + grandChild.getHeader() + "\", Line Num: " + grandChild.getLineNum() + ", Num Lines: " + grandChild.getNumLines());
                }
            }
        }
        System.out.println();
        */
        optimizedCode = "";
        // write optimized file (TBD)
        for (CodeElement elem : optimizedFile.getElements()) {
            optimizedCode += elem + "\n";
        }
        Logger.getInstance().log(new Message("Finished applying targeted optimizations.", Message.Type.GENERAL));
        signal();
    }

    public String getOptimizedCode() {
        return optimizedCode;
    }

    public SourceFile getOptimizedFile() {
        return optimizedFile;
    }

    public Vector<String> getOriginalCode() {
        return originalCode;
    }

    public SourceFile getOriginalFile() {
        return originalFile;
    }

    private boolean readFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String lineBuffer = reader.readLine();
            originalCode = new Vector<>();
            while (lineBuffer != null) {
                originalCode.add(lineBuffer);
                lineBuffer = reader.readLine();
            }
            Logger.getInstance().log(new Message("Successfully imported file: " + fileName, Message.Type.GENERAL));
            reader.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getInstance().log(new Message("Could not find file: " + fileName, Message.Type.ERROR));
            return false;
        } catch (IOException ex) {
            Logger.getInstance().log(new Message("Could not read file: " + fileName, Message.Type.ERROR));
            return false;
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
            return ElementType.MACRO;
        }
        // functions
        else if (line.contains("(") && line.contains(")") && line.contains("{")) {
            return ElementType.FUNCTION;
        }
        // variables
        else if (line.contains(";")) {
            return ElementType.STATEMENT;
        }
        else {
            return ElementType.EMPTY_LINE;
        }
    }

}
