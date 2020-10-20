package optimizationprototype.gui;

import optimizationprototype.optimization.OptimizationState;
import optimizationprototype.util.Logger;
import optimizationprototype.util.SourceHandler;
import optimizationprototype.util.SubjectBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class OptimizationGUI implements IGuiObserver {

    private JFrame frame;
    private CodePreviewPanel originalCodePanel;
    private CodePreviewPanel optimizedCodePanel;
    private ConsoleOutputPanel consolePanel;
    private OptimizationOptionsPanel optionsPanel;
    private File currentlySelectedFile = null;
    
    public OptimizationGUI() {
        SourceHandler.getInstance().attach(this);
        Logger.getInstance().attach(this);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getInstance().log("Couldn't apply system look and feel. Reverting to default.");
        }
        frame = new JFrame();
        originalCodePanel = new CodePreviewPanel("Original Code");
        optimizedCodePanel = new CodePreviewPanel("Optimized Code");
        consolePanel = new ConsoleOutputPanel();
        optionsPanel = new OptimizationOptionsPanel();
        initGUI();
    }
    
    private void initGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 1200);
        frame.setTitle("Embedded C Source Code Optimizer");
        frame.setLayout(new GridLayout(2, 2));
        frame.add(originalCodePanel);
        frame.add(optimizedCodePanel);
        frame.add(optionsPanel);
        frame.add(consolePanel);
        initImportAction();
        initOptimizeAction();
        initOutputAction();
        frame.setVisible(true);
    }

    private void initImportAction() {
        optionsPanel.importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int successValue = fileChooser.showDialog(optionsPanel.importButton, "Open");
                if (successValue == JFileChooser.APPROVE_OPTION) {
                    optionsPanel.outputButton.setEnabled(false);
                    originalCodePanel.clearText();
                    optimizedCodePanel.clearText();
                    currentlySelectedFile = fileChooser.getSelectedFile();
                    if (SourceHandler.getInstance().parseFile(currentlySelectedFile.getPath())) {
                        optionsPanel.optimizeButton.setEnabled(true);
                        if (SourceHandler.getInstance().getOriginalCode() != null) {
                            for (String line : SourceHandler.getInstance().getOriginalCode()) {
                                originalCodePanel.appendText(line + '\n');
                            }
                        }
                    }
                }
            }
        });
    }

    private void initOptimizeAction() {
        optionsPanel.optimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionsPanel.outputButton.setEnabled(true);
                SourceHandler.getInstance().generateOptimizedFile(optionsPanel.getOptimizationState());
            }
        });
    }

    private void initOutputAction() {
        optionsPanel.outputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
    }

    public JFrame getFrame() {
        return frame;
    }

    @Override
    public void update(SubjectBase subject) {
        if (subject instanceof Logger)
            consolePanel.log.append(Logger.getInstance().getLatest() + '\n');
        else if (subject instanceof SourceHandler && SourceHandler.getInstance().getOptimizedCode() != null)
            optimizedCodePanel.displayCode(SourceHandler.getInstance().getOptimizedFile());
        SwingUtilities.updateComponentTreeUI(frame);
    }

}
