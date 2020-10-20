package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;

public class CheckBoxNode extends JCheckBox {

    private Vector<JCheckBox> children;
    private static final int INDENT = 20;

    public CheckBoxNode(String name) {
        super(name);
        this.setFont(GuiOptions.CHECKBOX_NODE_LIST_FONT);
        children = new Vector<>();
    }

    public void addChildNode(CheckBoxNode node) {
        node.setBorder(new EmptyBorder(0, INDENT, 0, 0));
        node.setEnabled(this.isSelected());
        children.add(node);
    }

    public void addChildLeaf(JCheckBox leaf) {
        leaf.setBorder(new EmptyBorder(0, INDENT, 0, 0));
        leaf.setFont(GuiOptions.CHECKBOX_LEAF_LIST_FONT);
        leaf.setEnabled(this.isSelected());
        children.add(leaf);
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
        for (JCheckBox node : children) {
            node.setEnabled(this.isSelected());
            node.setSelected(this.isSelected());
        }
    }
}
