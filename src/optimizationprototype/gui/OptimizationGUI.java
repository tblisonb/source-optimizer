package optimizationprototype.gui;

import optimizationprototype.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import optimizationprototype.config.GuiOptions;

public class OptimizationGUI extends JFrame implements IGuiObserver {

    private CodePreviewPanel originalCodePanel;
    private CodePreviewPanel optimizedCodePanel;
    private ConsoleOutputPanel consolePanel;
    private OptimizationOptionsPanel optionsPanel;
    private OptimizerMenuBar menuBar;
    private FileIncludePanel includePanel;
    private File currentlySelectedFile = null;
    
    public OptimizationGUI() {
        try {
            String sysType = System.getProperty("os.name");
            if (sysType.startsWith("Windows")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            }
        } catch (Exception ex) {
            Logger.getInstance().log(new Message("Couldn't apply system look and feel. Reverting to default.", Message.Type.GENERAL));
        }
        originalCodePanel = new CodePreviewPanel("Original Code");
        optimizedCodePanel = new CodePreviewPanel("Optimized Code");
        consolePanel = new ConsoleOutputPanel();
        optionsPanel = new OptimizationOptionsPanel();
        includePanel = new FileIncludePanel();
        menuBar = new OptimizerMenuBar();
    }

    public void initGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1920, 1200);
        this.setTitle("Embedded C Source Code Optimizer");
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.gridheight= 2;
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        includePanel.setPreferredSize(new Dimension(300, 2));
        this.add(includePanel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridheight= 1;
        c.weightx = 1.0;
        c.gridx = 1;
        c.gridy = 0;
        originalCodePanel.setPreferredSize(new Dimension(2, 2));
        this.add(originalCodePanel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        optimizedCodePanel.setPreferredSize(new Dimension(2, 2));
        this.add(optimizedCodePanel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        optionsPanel.setPreferredSize(new Dimension(2, 2));
        this.add(optionsPanel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 1;
        consolePanel.setPreferredSize(new Dimension(2, 2));
        this.add(consolePanel, c);
        initImportAction();
        initOptimizeAction();
        initOutputAction();
        initAnalyzeAction();
        initAddSourceAction();
        initAddHeaderAction();
        menuBar.initMenuBar(optionsPanel.importButton.getActionListeners()[0], optionsPanel.outputButton.getActionListeners()[0], includePanel.addSourceButton.getActionListeners()[0], includePanel.addHeaderButton.getActionListeners()[0], originalCodePanel, optimizedCodePanel, consolePanel);
        this.setJMenuBar(menuBar);
    }

    private void initImportAction() {
        optionsPanel.importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("C Source Files (*.c)", "c");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);
            int successValue = fileChooser.showDialog(optionsPanel.importButton, "Open");
            if (successValue == JFileChooser.APPROVE_OPTION) {
                optionsPanel.outputButton.setEnabled(false);
                originalCodePanel.clearText();
                optimizedCodePanel.clearText();
                currentlySelectedFile = fileChooser.getSelectedFile();
                SourceHandler.getInstance().reset();
                SourceHandler.getInstance().setCWD(currentlySelectedFile.getPath().substring(0, currentlySelectedFile.getPath().lastIndexOf(System.getProperty("file.separator"))));
                if (SourceHandler.getInstance().parseFile(currentlySelectedFile.getPath())) {
                    // reset lists of headers and source files in UI
                    includePanel.clearHeaderFiles();
                    includePanel.clearSourceFiles();
                    optionsPanel.optimizeButton.setEnabled(true);
                    if (SourceHandler.getInstance().getOriginalCode() != null) {
                        originalCodePanel.displayCode(SourceHandler.getInstance().getOriginalFile());
                    }
                    menuBar.getAddSourceMenuItem().setEnabled(true);
                    menuBar.getAddSourceMenuItem().setToolTipText(GuiOptions.TOOL_TIP_ADD_SOURCE_ENB);
                    includePanel.addHeaderButton.setEnabled(true);
                    menuBar.getAddHeaderMenuItem().setEnabled(true);
                    menuBar.getAddHeaderMenuItem().setToolTipText(GuiOptions.TOOL_TIP_ADD_HEADER_ENB);
                    includePanel.addSourceButton.setEnabled(true);
                    for (String s : SourceHandler.getInstance().getIncludeFiles()) {
                        includePanel.addHeaderFile(s);
                    }
                    this.includePanel.addSourceFile(currentlySelectedFile.getName());
                }
            }
        });
    }

    private void initOptimizeAction() {
        optionsPanel.optimizeButton.addActionListener(e -> {
            optionsPanel.outputButton.setEnabled(true);
            optionsPanel.analyzeButton.setEnabled(true);
            SourceHandler.getInstance().generateOptimizedFile(optionsPanel.getOptimizationState());
        });
    }

    private void initOutputAction() {
        optionsPanel.outputButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("C Source Files (*.c)", "c");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);
            int successValue = fileChooser.showDialog(optionsPanel.importButton, "Save");
            if (successValue == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()));
                    writer.write(SourceHandler.getInstance().getOptimizedCode());
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(getFrame(), "Could not write file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void initAnalyzeAction() {
        optionsPanel.analyzeButton.addActionListener(e -> {
            Logger.getInstance().log(new Message("Compiling and running \"avr-size\" on both the unoptimized and optimized code.", Message.Type.GENERAL));
            Vector<String> result = ProcessManager.getInstance().executeCommands(this);
            if (result != null && result.size() > 0 && result.get(0).length() > 0 && result.get(1).length() > 0) {
                Object[] options = { "OK", "Save Optimized ELF", "Save Unoptimized ELF" };
                result.set(0, result.get(0).substring(0, result.get(0).lastIndexOf("filename")) + result.get(0).substring(result.get(0).lastIndexOf("filename") + 8, result.get(0).lastIndexOf("\t")));
                result.set(1, result.get(1).substring(0, result.get(1).lastIndexOf("filename")) + result.get(1).substring(result.get(1).lastIndexOf("filename") + 8, result.get(1).lastIndexOf("\t")));
                String[] result0 = result.get(0).substring(result.get(0).indexOf("\n")).split("\t");
                String[] result1 = result.get(1).substring(result.get(1).indexOf("\n")).split("\t");
                for (int i = 0; i < result0.length; i++) {
                    result0[i] = result0[i].trim();
                    result1[i] = result1[i].trim();
                }
                int textDiff = Integer.parseInt(result1[0]) - Integer.parseInt(result0[0]);
                int dataDiff = Integer.parseInt(result1[1]) - Integer.parseInt(result0[1]);
                int bssDiff = Integer.parseInt(result1[2]) - Integer.parseInt(result0[2]);
                int decDiff = Integer.parseInt(result1[3]) - Integer.parseInt(result0[3]);
                String decDiffPercent = Math.abs((((int)(((double) (Integer.parseInt(result1[3]) * 10000)) / ((double) Integer.parseInt(result0[3])))) / 100d)) + "";
                String resultDiff = "Unoptimized -> Optimized\n   Text:  " + ((textDiff >= 0) ? "+" : "") + textDiff +
                        " bytes\n   Data:  " + ((dataDiff >= 0) ? "+" : "") + dataDiff + " bytes\n   BSS:   " +
                        ((bssDiff >= 0) ? "+" : "") + bssDiff + " bytes\n   Total: " + ((decDiff >= 0) ? "+" : "") +
                        decDiff + " bytes\n\n\nThe total size of the optimized firmware is\n" + decDiffPercent + "% of the original (unoptimized) firmware.";
                JTextArea label = new JTextArea("Unoptimized Code Size:\n" + result.get(0) + "\nOptimized Code Size:\n" + result.get(1) + "\n\n" + resultDiff);
                label.setFont(GuiOptions.DEFAULT_CODE_FONT);
                label.setEditable(false);
                label.setBackground(UIManager.getColor("Panel.background"));
                int choice = JOptionPane.showOptionDialog(getFrame(), label, "Size Analysis", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choice == 1) {
                    ProcessManager.getInstance().writeBin(this, true);
                }
                else if (choice == 2) {
                    ProcessManager.getInstance().writeBin(this, false);
                }
                for (int i = 2; i < result.size(); i++) {
                    Logger.getInstance().log(new Message("Compiler output for file:\n" + result.get(i), Message.Type.COMPILER));
                }
            }
            else {
                for (int i = 2; result != null && i < result.size(); i++) {
                    Logger.getInstance().log(new Message("Compiler output for file:\n" + result.get(i), Message.Type.COMPILER));
                }
                JOptionPane.showMessageDialog(getFrame(), "Could not compile source code. See log output for details", "Size Analysis", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void initAddSourceAction() {
        includePanel.addSourceButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("C Source Files (*.c)", "c");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(true);
            int successValue = fileChooser.showDialog(includePanel.addSourceButton, "Add");
            if (successValue == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                SourceHandler.getInstance().addSourceFiles(files);
                for (File f : files) {
                    this.includePanel.addSourceFile(f.getName());
                }
            }
        });
    }
    
     private void initAddHeaderAction() {
        includePanel.addHeaderButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("C Header Files (*.h)", "h");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(true);
            int successValue = fileChooser.showDialog(includePanel.addHeaderButton, "Add");
            if (successValue == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                SourceHandler.getInstance().addHeaderFiles(files);
                for (File f : files) {
                    this.includePanel.addHeaderFile(f.getName());
                }
            }
        });
    }

    public JFrame getFrame() {
        return this;
    }

    @Override
    public void update(SubjectBase subject) {
        if (subject instanceof Logger)
            consolePanel.appendMessage(Logger.getInstance().getLatestMessage());
        else if (subject instanceof SourceHandler && SourceHandler.getInstance().getOptimizedCode() != null)
            optimizedCodePanel.displayCode(SourceHandler.getInstance().getOptimizedFile());
        SwingUtilities.updateComponentTreeUI(this);
    }

}
