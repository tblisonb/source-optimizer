package optimizationprototype.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Vector;

public class OptimizationOptionsPanel extends JPanel {

    private JScrollPane pane;
    public final JPanel optionsPanel, buttonsPanel;
    private Vector<JCheckBox> options;
    public final JButton importButton, optimizeButton;

    public OptimizationOptionsPanel() {
        pane = new JScrollPane();
        optionsPanel = new JPanel();
        options = new Vector<>();
        buttonsPanel = new JPanel(new GridLayout(1, 2));
        importButton = new JButton("Import File");
        optimizeButton = new JButton("Optimize Code");
        options.add(new JCheckBox("TEST 1"));
        options.add(new JCheckBox("TEST 2"));
        options.add(new JCheckBox("TEST 3"));
        options.add(new JCheckBox("TEST 4"));
        this.setLayout(new BorderLayout());
        this.setBorder(new TitledBorder(new EtchedBorder(), "Optimization Options"));
        for (JCheckBox box : options) {
            optionsPanel.add(box);
        }
        pane.add(optionsPanel);
        buttonsPanel.add(importButton);
        optimizeButton.setEnabled(false);
        buttonsPanel.add(optimizeButton);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.add(pane, BorderLayout.CENTER);
    }

}
