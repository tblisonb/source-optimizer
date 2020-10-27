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
    private JTextArea helpArea;
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
        JPanel optionsPanel = new JPanel(),
                helpPanel = new JPanel(new BorderLayout()),
                parentPanel = new JPanel(new BorderLayout());
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        CheckBoxNode counterTimerNode = new CheckBoxNode("Counter/Timer");
        counterTimerNode.setToolTipText(GuiOptions.TOOL_TIP_COUNTER);
        options.add(counterTimerNode);
        JCheckBox timeSensitiveLeaf = new JCheckBox("Time-Sensitive Order of Execution");
        timeSensitiveLeaf.setToolTipText(GuiOptions.TOOL_TIP_TIME_SENSITIVE);
        options.add(timeSensitiveLeaf);
        CheckBoxNode interruptNode = new CheckBoxNode("Interrupts");
        interruptNode.setToolTipText(GuiOptions.TOOL_TIP_INTERRUPT);
        options.add(interruptNode);
        CheckBoxNode builtinLeaf = new CheckBoxNode("Builtin Function Substitution");
        builtinLeaf.setToolTipText(GuiOptions.TOOL_TIP_BUILTIN);
        options.add(builtinLeaf);
        CheckBoxNode arithmeticLeaf = new CheckBoxNode("Arithmetic Substitution");
        arithmeticLeaf.setToolTipText(GuiOptions.TOOL_TIP_ARITHMETIC);
        options.add(arithmeticLeaf);
        counterTimerNode.addChildLeaf(timeSensitiveLeaf);
        optionsPanel.add(counterTimerNode);
        optionsPanel.add(timeSensitiveLeaf);
        optionsPanel.add(interruptNode);
        optionsPanel.add(builtinLeaf);
        optionsPanel.add(arithmeticLeaf);
        parentPanel.add(optionsPanel, BorderLayout.WEST);
        initHelpPanel(helpPanel);
        parentPanel.add(helpPanel, BorderLayout.EAST);
        JScrollPane scrollPane = new JScrollPane(parentPanel);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void initCheckboxListeners() {
        for (JCheckBox box : options) {
            box.addActionListener(e -> {
                if (box.getText().equals("Counter/Timer")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setTimerOptimization(box.isSelected());
                    helpArea.setText(GuiOptions.TOOL_TIP_COUNTER + "\n\n\n" + GuiOptions.INFO_COUNTER);
                }
                else if (box.getText().equals("Time-Sensitive Order of Execution")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setTimeSensitiveTimer(box.isSelected());
                    helpArea.setText(GuiOptions.TOOL_TIP_TIME_SENSITIVE);
                }
                else if (box.getText().equals("Interrupts")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setInterruptOptimization(box.isSelected());
                    helpArea.setText(GuiOptions.TOOL_TIP_INTERRUPT);
                }
                else if (box.getText().equals("Builtin Function Substitution")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setBuiltinOptimization(box.isSelected());
                    helpArea.setText(GuiOptions.TOOL_TIP_BUILTIN);
                }
                else if (box.getText().equals("Arithmetic Substitution")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setArithmeticOptimization(box.isSelected());
                    helpArea.setText(GuiOptions.TOOL_TIP_ARITHMETIC);
                }
            });
        }
    }

    private void initHelpPanel(JPanel panel) {
        helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setText("");
        helpArea.setLineWrap(true);
        helpArea.setWrapStyleWord(true);
        helpArea.setBorder(new EmptyBorder(5, 5, 5 ,5));
        JScrollPane pane = new JScrollPane(helpArea);
        pane.setPreferredSize(new Dimension(350, 0));
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        TitledBorder border = new TitledBorder(new EtchedBorder(),"Optimization Info");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        pane.setBorder(border);
        panel.add(pane, BorderLayout.CENTER);
    }

}
