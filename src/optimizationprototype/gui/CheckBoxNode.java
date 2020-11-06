package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;

public class CheckBoxNode extends JCheckBox {

    private Vector<JCheckBox> children;
    private static final int INDENT = 30;
    private static final int VERTICAL_PADDING = 5;
    private int indentLevel;
    private boolean isStrongParent;

    public CheckBoxNode(String name, boolean isStrongParent) {
        super(name);
        this.setFont(GuiOptions.CHECKBOX_NODE_LIST_FONT);
        this.setFont(GuiOptions.CHECKBOX_NODE_PARENT_FONT);
        children = new Vector<>();
        indentLevel = 0;
        this.isStrongParent = isStrongParent;
    }

    public void addChildNode(CheckBoxNode node) {
        node.indentLevel++;
        node.setBorder(new EmptyBorder(VERTICAL_PADDING, INDENT * node.indentLevel, VERTICAL_PADDING, 0));
        node.setFont(GuiOptions.CHECKBOX_NODE_LIST_FONT);
        if (isStrongParent)
            node.setEnabled(this.isSelected());
        children.add(node);
    }

    public void addChildLeaf(JCheckBox leaf) {
        leaf.setBorder(new EmptyBorder(VERTICAL_PADDING, INDENT * ++indentLevel, VERTICAL_PADDING, 0));
        leaf.setFont(GuiOptions.CHECKBOX_LEAF_LIST_FONT);
        leaf.setEnabled(this.isSelected());
        if (isStrongParent)
            leaf.setEnabled(this.isSelected());
        children.add(leaf);
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
        for (JCheckBox node : children) {
            if (isStrongParent)
                node.setEnabled(this.isSelected());
            node.setSelected(this.isSelected());
        }
    }
}
