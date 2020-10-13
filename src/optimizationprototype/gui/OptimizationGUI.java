package optimizationprototype.gui;

import optimizationprototype.util.Logger;
import optimizationprototype.util.SourceHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class OptimizationGUI implements IGuiObserver {

    private JFrame frame;
    private CodePreviewPanel originalCodePanel;
    private CodePreviewPanel optimizedCodePanel;
    private ConsoleOutputPanel consolePanel;
    private OptimizationOptionsPanel optionsPanel;
    
    public OptimizationGUI() {
        SourceHandler.getInstance().attach(this);
        Logger.getInstance().attach(this);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Couldn't apply system look and feel. Reverting to default.");
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
        frame.setSize(1280, 1024);
        frame.setLayout(new GridLayout(2, 2));
        frame.add(originalCodePanel);
        frame.add(optimizedCodePanel);
        frame.add(optionsPanel);
        frame.add(consolePanel);
        initImportAction();
        initOptimizeAction();
        frame.setVisible(true);
    }

    private void initImportAction() {
        optionsPanel.importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int successValue = fileChooser.showDialog(optionsPanel.importButton, "Open");
                if (successValue == JFileChooser.APPROVE_OPTION) {
                    originalCodePanel.text.setText("");
                    optionsPanel.optimizeButton.setEnabled(true);
                    File f = fileChooser.getSelectedFile();
                    SourceHandler.getInstance().parseFile(f.getPath());
                    for (String line : SourceHandler.getInstance().getOriginalCode()) {
                        originalCodePanel.text.append(line + '\n');
                    }
                }
            }
        });
    }

    private void initOptimizeAction() {
        optionsPanel.optimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionsPanel.optimizeButton.setEnabled(false);
                SourceHandler.getInstance().writeOptimizedFile();
            }
        });
    }
    
    public JFrame getFrame() {
        return this.frame;
    }

    @Override
    public void update() {
        if (Logger.getInstance().getState()) {
            consolePanel.log.append(Logger.getInstance().getLatest());
            Logger.getInstance().resetState();
        }
        if (SourceHandler.getInstance().getState() && SourceHandler.getInstance().getOptimizedCode() != null) {
            optimizedCodePanel.text.setText(SourceHandler.getInstance().getOptimizedCode());
            SourceHandler.getInstance().resetState();
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

}
