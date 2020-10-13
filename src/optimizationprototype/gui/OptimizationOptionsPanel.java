package optimizationprototype.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Vector;

public class OptimizationOptionsPanel extends JPanel {

    public final JPanel buttonsPanel;
    private JTree optionsTree;
    private Vector<JCheckBox> options;
    public final JButton importButton, optimizeButton;

    public OptimizationOptionsPanel() {
        options = new Vector<>();
        buttonsPanel = new JPanel(new GridLayout(1, 2));
        importButton = new JButton("Import File");
        importButton.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        optimizeButton = new JButton("Optimize Code");
        optimizeButton.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        this.setLayout(new BorderLayout());
        this.setBorder(new TitledBorder(new EtchedBorder(), "Optimization Options"));
        buttonsPanel.add(importButton);
        optimizeButton.setEnabled(false);
        buttonsPanel.add(optimizeButton);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        initCheckBoxView();
    }

    private void initCheckBoxView() {
        CheckBoxNode counterElems[] = { new CheckBoxNode("Counter/Timer Optimization", false),
                                        new CheckBoxNode("Time-Sensitive Order of Execution", true) };
        CheckBoxNode interruptElems[] = { new CheckBoxNode("External Interrupt Optimization", false) };
        Vector counter = new NamedVector("Counter/Timer", counterElems);
        Vector interrupt = new NamedVector("External Interrupt", interruptElems);
        Object rootNodes[] = { counter, interrupt };
        Vector rootVector = new NamedVector("Root", rootNodes);
        optionsTree = new JTree(rootVector);

        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        optionsTree.setCellRenderer(renderer);

        optionsTree.setCellEditor(new CheckBoxNodeEditor(optionsTree));
        optionsTree.setEditable(true);

        JScrollPane scrollPane = new JScrollPane(optionsTree);
        this.add(scrollPane, BorderLayout.CENTER);
    }

}
