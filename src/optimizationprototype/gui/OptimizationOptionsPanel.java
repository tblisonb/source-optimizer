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
    public final JButton importButton, optimizeButton, outputButton, analyzeButton;

    public OptimizationOptionsPanel() {
        buttonsPanel = new JPanel(new GridLayout(1, 2));
        importButton = new JButton("Import File");
        importButton.setFont(GuiOptions.BUTTON_FONT);
        optimizeButton = new JButton("Optimize Code");
        optimizeButton.setFont(GuiOptions.BUTTON_FONT);
        outputButton = new JButton("Output File");
        outputButton.setFont(GuiOptions.BUTTON_FONT);
        analyzeButton = new JButton("Compile/Analyze");
        analyzeButton.setFont(GuiOptions.BUTTON_FONT);
        this.setLayout(new BorderLayout());
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Optimization Options");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        buttonsPanel.add(importButton);
        optimizeButton.setEnabled(false);
        buttonsPanel.add(optimizeButton);
        outputButton.setEnabled(false);
        buttonsPanel.add(outputButton);
        analyzeButton.setEnabled(false);
        buttonsPanel.add(analyzeButton);
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
                parentPanel = new JPanel(new GridLayout(1, 2));
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        CheckBoxNode selectAllOptimizationsNode = new CheckBoxNode("Select All Optimizations", false);
        options.add(selectAllOptimizationsNode);

        CheckBoxNode counterTimerNode = new CheckBoxNode("Counter/Timer", true);
        counterTimerNode.setToolTipText(GuiOptions.TOOL_TIP_COUNTER);
        options.add(counterTimerNode);
        selectAllOptimizationsNode.addChildNode(counterTimerNode);

        JCheckBox timeSensitiveLeaf = new JCheckBox("Time-Sensitive Order of Execution");
        timeSensitiveLeaf.setToolTipText(GuiOptions.TOOL_TIP_TIME_SENSITIVE);
        options.add(timeSensitiveLeaf);
        counterTimerNode.addChildLeaf(timeSensitiveLeaf);

        CheckBoxNode interruptNode = new CheckBoxNode("Interrupts", true);
        interruptNode.setToolTipText(GuiOptions.TOOL_TIP_INTERRUPT);
        options.add(interruptNode);
        selectAllOptimizationsNode.addChildNode(interruptNode);

        CheckBoxNode pwmNode = new CheckBoxNode("Pulse-Width Modulation", true);
        //pwmNode.setToolTipText();
        options.add(pwmNode);
        selectAllOptimizationsNode.addChildNode(pwmNode);

        JCheckBox invertedPwmLeaf = new JCheckBox("Invert Duty Cycle");
        //invertedPwmLeaf.setToolTipText();
        options.add(invertedPwmLeaf);
        pwmNode.addChildLeaf(invertedPwmLeaf);

        JCheckBox preserveFrequencyLeaf = new JCheckBox("Preserve Frequency");
        //preserveFrequencyLeaf.setToolTipText();
        options.add(preserveFrequencyLeaf);
        pwmNode.addChildLeaf(preserveFrequencyLeaf);

        CheckBoxNode builtinLeaf = new CheckBoxNode("Builtin Function Substitution", true);
        builtinLeaf.setToolTipText(GuiOptions.TOOL_TIP_BUILTIN);
        options.add(builtinLeaf);
        selectAllOptimizationsNode.addChildNode(builtinLeaf);

        CheckBoxNode arithmeticNode = new CheckBoxNode("Arithmetic Substitution", true);
        arithmeticNode.setToolTipText(GuiOptions.TOOL_TIP_ARITHMETIC);
        options.add(arithmeticNode);
        selectAllOptimizationsNode.addChildNode(arithmeticNode);

        for (JCheckBox box : options) {
            optionsPanel.add(box);
        }
        parentPanel.add(optionsPanel, 0);
        initHelpPanel(helpPanel);
        parentPanel.add(helpPanel, 1);
        JScrollPane scrollPane = new JScrollPane(parentPanel);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void initCheckboxListeners() {
        for (JCheckBox box : options) {
            box.addActionListener(e -> {
                if (box.getText().equals("Select All Optimizations")) {
                    box.setSelected(box.isSelected());
                    selectedOptions.setTimerOptimization(box.isSelected());
                    selectedOptions.setTimeSensitiveTimer(box.isSelected());
                    selectedOptions.setInterruptOptimization(box.isSelected());
                    selectedOptions.setPwmOptimization(box.isSelected());
                    selectedOptions.setInvertedPwm(box.isSelected());
                    selectedOptions.setPreserveFrequency(box.isSelected());
                    selectedOptions.setBuiltinOptimization(box.isSelected());
                    selectedOptions.setArithmeticOptimization(box.isSelected());
                }
                else if (box.getText().equals("Counter/Timer")) {
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
                else if (box.getText().equals("Pulse-Width Modulation")) {
                    ((CheckBoxNode) box).setSelected(box.isSelected() && box.isEnabled(), false);
                    selectedOptions.setPwmOptimization(box.isSelected());
                    //helpArea.setText();
                }
                else if (box.getText().equals("Invert Duty Cycle")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setInvertedPwm(box.isSelected());
                    //helpArea.setText();
                }
                else if (box.getText().equals("Preserve Frequency")) {
                    box.setSelected(box.isSelected() && box.isEnabled());
                    selectedOptions.setPreserveFrequency(box.isSelected());
                    //helpArea.setText();
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
