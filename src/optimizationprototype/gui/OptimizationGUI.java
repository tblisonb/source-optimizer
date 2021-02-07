package optimizationprototype.gui;

import optimizationprototype.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

public class OptimizationGUI extends JFrame implements IGuiObserver {

    private CodePreviewPanel originalCodePanel;
    private CodePreviewPanel optimizedCodePanel;
    private ConsoleOutputPanel consolePanel;
    private OptimizationOptionsPanel optionsPanel;
    private OptimizerMenuBar menuBar;
    private File currentlySelectedFile = null;
    
    public OptimizationGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getInstance().log(new Message("Couldn't apply system look and feel. Reverting to default.", Message.Type.GENERAL));
        }
        originalCodePanel = new CodePreviewPanel("Original Code");
        optimizedCodePanel = new CodePreviewPanel("Optimized Code");
        consolePanel = new ConsoleOutputPanel();
        optionsPanel = new OptimizationOptionsPanel();
        menuBar = new OptimizerMenuBar();
    }

    public void initGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1600, 1200);
        this.setTitle("Embedded C Source Code Optimizer");
        this.setLayout(new GridLayout(2, 2));
        this.add(originalCodePanel);
        this.add(optimizedCodePanel);
        this.add(optionsPanel);
        this.add(consolePanel);
        initImportAction();
        initOptimizeAction();
        initOutputAction();
        initAnalyzeAction();
        menuBar.initMenuBar(optionsPanel.importButton.getActionListeners()[0], optionsPanel.outputButton.getActionListeners()[0], originalCodePanel, optimizedCodePanel, consolePanel);
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
                if (SourceHandler.getInstance().parseFile(currentlySelectedFile.getPath())) {
                    optionsPanel.optimizeButton.setEnabled(true);
                    if (SourceHandler.getInstance().getOriginalCode() != null) {
                        originalCodePanel.displayCode(SourceHandler.getInstance().getOriginalFile());
                    }
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
            String[] result = ProcessManager.getInstance().executeCommands(this);
            if (result != null) {
                Object[] options = { "OK", "Save Optimized ELF", "Save Unoptimized ELF" };
                result[0] = result[0].substring(0, result[0].lastIndexOf("filename")) + result[0].substring(result[0].lastIndexOf("filename") + 8, result[0].lastIndexOf("\t"));
                result[1] = result[1].substring(0, result[1].lastIndexOf("filename")) + result[1].substring(result[1].lastIndexOf("filename") + 8, result[1].lastIndexOf("\t"));
                String[] result0 = result[0].substring(result[0].indexOf("\n")).split("\t");
                String[] result1 = result[1].substring(result[1].indexOf("\n")).split("\t");
                for (int i = 0; i < result0.length; i++) {
                    result0[i] = result0[i].trim();
                    result1[i] = result1[i].trim();
                }
                int textDiff = Integer.parseInt(result1[0]) - Integer.parseInt(result0[0]);
                int dataDiff = Integer.parseInt(result1[1]) - Integer.parseInt(result0[1]);
                int bssDiff = Integer.parseInt(result1[2]) - Integer.parseInt(result0[2]);
                int decDiff = Integer.parseInt(result1[3]) - Integer.parseInt(result0[3]);
                String decDiffPercent = Math.abs((((int)(((double) (decDiff * 10000)) / ((double) Integer.parseInt(result0[3])))) / 100d)) + "";
                String resultDiff = "Unoptimized -> Optimized\n   Text:  " + ((textDiff >= 0) ? "+" : "") + textDiff +
                        " bytes\n   Data:  " + ((dataDiff >= 0) ? "+" : "") + dataDiff + " bytes\n   BSS:   " +
                        ((bssDiff >= 0) ? "+" : "") + bssDiff + " bytes\n   Total: " + ((decDiff >= 0) ? "+" : "") +
                        decDiff + " bytes\n\n\nThe total size of the optimized firmware is\n" + decDiffPercent + "% of the original (unoptimized) firmware.";
                JTextArea label = new JTextArea("Unoptimized Code Size:\n" + result[0] + "\nOptimized Code Size:\n" + result[1] + "\n\n" + resultDiff);
                label.setFont(new Font("Courier New", Font.PLAIN, 16));
                label.setEditable(false);
                label.setBackground(UIManager.getColor("Panel.background"));
                int choice = JOptionPane.showOptionDialog(getFrame(), label, "Size Analysis", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choice == 1) {
                    ProcessManager.getInstance().writeBin(this, true);
                }
                else if (choice == 2) {
                    ProcessManager.getInstance().writeBin(this, false);
                }
            }
            else {
                JOptionPane.showMessageDialog(getFrame(), "Could not compile source code.", "Size Analysis", JOptionPane.ERROR_MESSAGE);
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
