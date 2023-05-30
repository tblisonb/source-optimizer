package optimizationprototype.util;

import optimizationprototype.config.MCUData;
import optimizationprototype.config.OptimizationTarget;
import optimizationprototype.config.Peripheral;
import optimizationprototype.config.Target;
import optimizationprototype.optimization.OptimizationState;
import optimizationprototype.optimization.SourceOptimizerBuilder;
import optimizationprototype.structure.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceHandler extends SubjectBase {
    
    private Vector<MCUData> supportedMCUs;
    private static SourceHandler instance = new SourceHandler();
    private Vector<OptimizationTarget> targets;
    private String optimizedCode;
    private SourceFile originalFile, optimizedFile;
    private Vector<String> originalCode;
    private boolean suggestionsEnabled;
    private int defaultFrequency;
    private String cwd;
    private Vector<File> sourceFiles;
    private Vector<File> headerFiles;

    private SourceHandler() {
        super();
        supportedMCUs = new Vector<>();
        targets = new Vector<>();
        optimizedCode = null;
        originalFile = new SourceFile();
        optimizedFile = null;
        originalCode = null;
        suggestionsEnabled = true;
        defaultFrequency = 1000000;
        cwd = "";
        sourceFiles = new Vector<>();
        headerFiles = new Vector<>();
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
        defaultFrequency = 1000000;
        cwd = "";
        sourceFiles = new Vector<>();
        headerFiles = new Vector<>();
    }

    public boolean parseFile(String fileName) {
        if (!fileName.substring(fileName.length() - 2).equalsIgnoreCase(".c")) {
            Logger.getInstance().log(new Message("Unsupported file type: " + fileName, Message.Type.ERROR));
            return false;
        }
        if (!readFile(fileName) || originalCode == null)
            return false;
        ElementType typeBeingParsed;
        for (int i = 0; i < originalCode.size(); i++) {
            typeBeingParsed = getType(originalCode.get(i));
            switch (typeBeingParsed) {
                case EMPTY_LINE:
                    this.originalFile.addElement(new EmptyLine(originalCode.get(i)));
                    break;
                case MACRO:
                    Macro m;
                    Matcher matcher = Pattern.compile("#include\\s*\"([^\"]+)\"").matcher(originalCode.get(i));
                    if (matcher.find()) {
                        m = new IncludeStatement(originalCode.get(i), matcher.group(1));
                    }
                    else
                        m = new Macro(originalCode.get(i));
                    this.originalFile.addElement(m);
                    break;
                case STATEMENT:
                    this.originalFile.addElement(new Statement(originalCode.get(i)));
                    break;
                case MULTILINE_COMMENT:
                    CodeElement comment = new MultilineComment(originalCode.get(i++));
                    int j;
                    for (j = i; j < originalCode.size() && !originalCode.get(j).contains("*/"); j++) {
                        comment.addChildElement(new EmptyLine(originalCode.get(j)));
                    }
                    if (j == originalCode.size())
                        return false;
                    comment.addChildElement(new EmptyLine(originalCode.get(j)));
                    this.originalFile.addElement(comment);
                    i = j;
                    break;
                case FUNCTION:
                    String header = originalCode.get(i++);
                    Function f = new Function(header);
                    Vector<String> functionContents = new Vector<>();
                    int numOpenBraces = (header.contains("{") ? 1 : 0);
                    boolean isSplit = numOpenBraces == 0;
                    if (isSplit) {
                        if (originalCode.get(i + 1).contains("{"))
                            numOpenBraces++;
                        if (numOpenBraces > 0) {
                            functionContents.add(originalCode.get(i));
                        }
                    }
                    if (header.contains("}"))
                        numOpenBraces--;
                    // add all code lines for the current function
                    for (j = i + (isSplit ? 1 : 0); j < originalCode.size() && (numOpenBraces > 0 || isSplit); j++) {
                        if (originalCode.get(j).contains("{")) {
                            numOpenBraces++;
                            isSplit = false;
                        }
                        if (originalCode.get(j).contains("}"))
                            numOpenBraces--;
                        if (numOpenBraces > 0) {
                            functionContents.add(originalCode.get(j));
                        }
                    }
                    f.init(functionContents);
                    this.originalFile.addElement(f);
                    i = j - 1;
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
        boolean needsUpdate = updateFrequencyDefine();
        if (state.getPWMOptimizationState()) {
            op.optimizePWM(state.getInvertedPWM(), state.getPreserveFrequency());
        }
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
        if (needsUpdate) {
            optimizedFile.insertElement(new Macro("#define F_CPU " + defaultFrequency, CodeElement.State.ADDED), 0);
            Logger.getInstance().log(new Message("Inserted F_CPU definition based on the default frequency of " + defaultFrequency + " Hz.", Message.Type.GENERAL));
        }
        optimizedFile.updateLineNumbers();
        optimizedCode = "";
        // write optimized file (TBD)
        for (CodeElement elem : optimizedFile.getElements()) {
            optimizedCode += elem + "\n";
        }
        Logger.getInstance().log(new Message("Finished applying targeted optimizations.", Message.Type.GENERAL));
        signal();
    }

    private boolean updateFrequencyDefine() {
        int frequency = 0;
        for (CodeElement elem : originalFile.getElements()) {
            if (elem.getType() == ElementType.MACRO && elem.getCode().contains("define") && elem.getCode().contains("F_CPU")) {
                String freq = elem.getCode().substring(elem.getCode().indexOf("F_CPU") + 5).trim();
                if (freq.contains("UL"))
                    freq = freq.substring(0, freq.indexOf("UL"));
                frequency = Integer.parseInt(freq);
            }
        }
        if (frequency > 0) {
            this.defaultFrequency = frequency;
            Logger.getInstance().log(new Message("Updated default frequency to " + frequency + " Hz.", Message.Type.GENERAL));
            return false;
        }
        return true;
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
        // variables
        else if (line.contains(";")) {
            return ElementType.STATEMENT;
        }
        // functions
        else if (line.contains("(") && line.contains(")")) {
            return ElementType.FUNCTION;
        }
        // multiline comments
        else if (line.contains("/*") && !line.contains("*/")) {
            return ElementType.MULTILINE_COMMENT;
        }
        // single line comment or unidentified line
        else {
            return ElementType.EMPTY_LINE;
        }
    }

    public int getDefaultFrequency() {
        return defaultFrequency;
    }

    public void setDefaultFrequency(int defaultFrequency) {
        this.defaultFrequency = defaultFrequency;
    }

    public Vector<String> getIncludeFiles() {
        Vector<String> includeFiles = new Vector<>();
        List<CodeElement> elements = this.originalFile.getElementsOfType(ElementType.MACRO);
        for (CodeElement e : elements) {
            if (e instanceof IncludeStatement) {
                includeFiles.add(((IncludeStatement) e).getIncludeFilePath());
            }
        }
        return includeFiles;
    }

    public void setCWD(String cwd) {
        this.cwd = cwd;
    }
    
    public String getCWD() {
        return this.cwd;
    }

    public void addSourceFiles(File[] files) {
        Collections.addAll(this.sourceFiles, files);
    }
    
    public Vector<File> getSourceFiles() {
        return this.sourceFiles;
    }

    public void addHeaderFiles(File[] files) {
        Collections.addAll(this.headerFiles, files);
    }
    
    public Vector<File> getHeaderFiles() {
        return this.headerFiles;
    }

}
