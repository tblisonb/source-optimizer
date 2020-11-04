package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.SourceFile;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CodePreviewPanel extends JPanel {

    private JTextArea text;
    private JScrollPane pane;
    private boolean lineNumbersEnabled;
    private SourceFile currentFile;

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
        lineNumbersEnabled = false;
        currentFile = null;
    }

    public void clearText() {
        text.setText("");
    }

    public void appendText(String line) {
        text.append(line);
    }

    public String getText() {
        return text.getText();
    }

    public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
        this.lineNumbersEnabled = lineNumbersEnabled;
    }

    public SourceFile getSourceFile() {
        return currentFile;
    }

    public void displayCode(SourceFile file) {
        this.currentFile = file;
        this.text.setText("");
        for (CodeElement elem : file.getElements()) {
            if (elem.isBlock()) {
                String indent = "";
                for (int i = 0; i < elem.getIndentLevel(); i++) {
                    indent += "    ";
                }
                switch (elem.getState()) {
                    case ADDED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "") + "+ " + indent + elem.getHeader() + "\n");
                        break;
                    case REMOVED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + indent + elem.getHeader() + "\n");
                        break;
                    case MODIFIED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + indent + elem.getHeader() + "\n");
                        break;
                    default:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + elem.getHeader() + "\n");
                }
                displayCode(elem);
                this.text.append((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines()) + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + "}\n");
            }
            else {
                switch (elem.getState()) {
                    case ADDED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "+ " + elem + "\n");
                        break;
                    case REMOVED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + elem + "\n");
                        break;
                    case MODIFIED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + elem + "\n");
                        break;
                    default:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + elem + "\n");
                }
            }
        }
    }

    private void displayCode(CodeElement element) {
        if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                String indent = "";
                for (int i = 0; i < elem.getIndentLevel(); i++) {
                    indent += "    ";
                }
                switch (elem.getState()) {
                    case ADDED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "+ " + indent + elem.getHeader() + "\n");
                        break;
                    case REMOVED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + indent + elem.getHeader() + "\n");
                        break;
                    case MODIFIED:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + indent + elem.getHeader() + "\n");
                        break;
                    default:
                        this.text.append((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + elem.getHeader() + "\n");
                }
                if (elem.isBlock()) {
                    displayCode(elem);
                    this.text.append((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines()) + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + "}\n");
                }
            }
        }
        else {
            switch (element.getState()) {
                case ADDED:
                    this.text.append((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "+ " + element + "\n");
                    break;
                case REMOVED:
                    this.text.append((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "- " + element + "\n");
                    break;
                case MODIFIED:
                    this.text.append((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "* " + element + "\n");
                    break;
                default:
                    this.text.append((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "  " + element + "\n");
            }
        }
    }

    private String getIndentForLineNum(int lineNum) {
        String result = "    ";
        for (int i = lineNum; i > 0; i /= 10) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
