package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.SourceFile;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CodePreviewPanel extends JPanel {

    public final JTextArea text;
    private JScrollPane pane;

    public CodePreviewPanel(String title) {
        text = new JTextArea();
        pane = new JScrollPane(text);
        text.setFont(new Font("Courier New", Font.PLAIN, 16));
        text.setEditable(false);
        this.setLayout(new BorderLayout());
        TitledBorder border = new TitledBorder(new EtchedBorder(), title);
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(pane, BorderLayout.CENTER);
    }

    public void displayCode(SourceFile file) {
        this.text.setText("");
        for (CodeElement elem : file.getElements()) {
            switch (elem.getState()) {
                case ADDED:
                    this.text.append("+ " + elem + "\n");
                    break;
                case REMOVED:
                    this.text.append("- " + elem + "\n");
                    break;
                case MODIFIED:
                    this.text.append("* " + elem + "\n");
                    break;
                default:
                    this.text.append("  " + elem + "\n");
            }
            if (elem.isBlock())
                displayCode(elem);
        }
    }

    private void displayCode(CodeElement element) {
        if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                switch (elem.getState()) {
                    case ADDED:
                        this.text.append("+ " + elem + "\n");
                        break;
                    case REMOVED:
                        this.text.append("- " + elem + "\n");
                        break;
                    case MODIFIED:
                        this.text.append("* " + elem + "\n");
                        break;
                    default:
                        this.text.append("  " + elem + "\n");
                }
                displayCode(elem);
            }
        }
        else {
            switch (element.getState()) {
                case ADDED:
                    this.text.append("+ " + element + "\n");
                    break;
                case REMOVED:
                    this.text.append("- " + element + "\n");
                    break;
                case MODIFIED:
                    this.text.append("* " + element + "\n");
                    break;
                default:
                    this.text.append("  " + element + "\n");
            }
        }
    }

}
