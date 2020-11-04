package optimizationprototype.gui;

import optimizationprototype.util.Logger;
import optimizationprototype.util.SourceHandler;
import optimizationprototype.util.SubjectBase;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            Logger.getInstance().log("Couldn't apply system look and feel. Reverting to default.");
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
        menuBar.initMenuBar(optionsPanel.importButton.getActionListeners()[0], optionsPanel.outputButton.getActionListeners()[0], optimizedCodePanel);
        this.setJMenuBar(menuBar);
    }

    private void initImportAction() {
        optionsPanel.importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
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
                        for (String line : SourceHandler.getInstance().getOriginalCode()) {
                            originalCodePanel.appendText(line + '\n');
                        }
                    }
                }
            }
        });
    }

    private void initOptimizeAction() {
        optionsPanel.optimizeButton.addActionListener(e -> {
            optionsPanel.outputButton.setEnabled(true);
            SourceHandler.getInstance().generateOptimizedFile(optionsPanel.getOptimizationState());
        });
    }

    private void initOutputAction() {
        optionsPanel.outputButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int successValue = fileChooser.showDialog(optionsPanel.importButton, "Open");
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

    public JFrame getFrame() {
        return this;
    }

    @Override
    public void update(SubjectBase subject) {
        if (subject instanceof Logger)
            consolePanel.log.append(Logger.getInstance().getLatest() + '\n');
        else if (subject instanceof SourceHandler && SourceHandler.getInstance().getOptimizedCode() != null)
            optimizedCodePanel.displayCode(SourceHandler.getInstance().getOptimizedFile());
        SwingUtilities.updateComponentTreeUI(this);
    }

}
