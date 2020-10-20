package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.optimization.OptimizationState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Vector;

public class OptimizationOptionsPanel extends JPanel {

    private JPanel buttonsPanel;
    private OptimizationState selectedOptions;
    private Vector<JCheckBox> options;
    public final JButton importButton, optimizeButton, outputButton;

    public OptimizationOptionsPanel() {
        buttonsPanel = new JPanel(new GridLayout(1, 2));
        importButton = new JButton("Import File");
        importButton.setFont(GuiOptions.BUTTON_FONT);
        optimizeButton = new JButton("Optimize Code");
        optimizeButton.setFont(GuiOptions.BUTTON_FONT);
        outputButton = new JButton("Output File");
        outputButton.setFont(GuiOptions.BUTTON_FONT);
        this.setLayout(new BorderLayout());
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Optimization Options");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        buttonsPanel.add(importButton);
        optimizeButton.setEnabled(false);
        buttonsPanel.add(optimizeButton);
        outputButton.setEnabled(false);
        buttonsPanel.add(outputButton);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        selectedOptions = new OptimizationState();
        options = new Vector<>();
        initCheckBoxView();
        initCheckboxListeners();
    }

    public OptimizationState getOptimizationState() {
        return selectedOptions;
    }

    private void initCheckBoxView() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        CheckBoxNode counterTimerNode = new CheckBoxNode("Counter/Timer");
        options.add(counterTimerNode);
        JCheckBox timeSensitiveLeaf = new JCheckBox("Time-Sensitive Order of Execution");
        options.add(timeSensitiveLeaf);
        CheckBoxNode interruptNode = new CheckBoxNode("Interrupts");
        options.add(interruptNode);
        counterTimerNode.addChildLeaf(timeSensitiveLeaf);
        optionsPanel.add(counterTimerNode);
        optionsPanel.add(timeSensitiveLeaf);
        optionsPanel.add(interruptNode);
        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void initCheckboxListeners() {
        for (JCheckBox box : options) {
            box.addActionListener(e -> {
                if (box.getText().equals("Counter/Timer")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setTimerOptimization(box.isSelected());
                }
                else if (box.getText().equals("Time-Sensitive Order of Execution")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setTimeSensitiveTimer(box.isSelected());
                }
                else if (box.getText().equals("Interrupts")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setInterruptOptimization(box.isSelected());
                }
            });
        }
    }

}
