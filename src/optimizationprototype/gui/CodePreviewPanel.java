package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.structure.CodeElement;
import optimizationprototype.structure.ElementType;
import optimizationprototype.structure.SourceFile;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;

public class CodePreviewPanel extends JPanel {

    private JTextPane text;
    private JScrollPane pane;
    private boolean lineNumbersEnabled, colorEnabled;
    private SourceFile currentFile;
    private StyledDocument doc;
    private Style style, defaultStyle;
    private final Color COLOR_ADDED = new Color(205, 255, 216);
    private final Color COLOR_REMOVED = new Color(255, 220, 224);
    private final Color COLOR_MODIFIED = new Color(255, 245, 184);

    public CodePreviewPanel(String title) {
        text = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
        doc  = this.text.getStyledDocument();
        style = this.text.addStyle("Color Style", null);
        defaultStyle = this.text.addStyle("Color Style", null);
        pane = new JScrollPane(text);
        text.setFont(new Font("Courier New", Font.PLAIN, 16));
        text.setEditable(false);
        this.setLayout(new BorderLayout());
        TitledBorder border = new TitledBorder(new EtchedBorder(), title);
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(pane, BorderLayout.CENTER);
        lineNumbersEnabled = true;
        colorEnabled = true;
        currentFile = null;
    }

    public void clearText() {
        text.setText("");
    }

    public void appendText(String line) {
        try {
            StyleConstants.setBackground(style, Color.WHITE);
            doc.insertString(doc.getLength(), line, style);
        } catch (BadLocationException e) {
            Logger.getInstance().log(new Message(e.getLocalizedMessage(), Message.Type.ERROR));
        }
    }

    public String getText() {
        return text.getText();
    }

    public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
        this.lineNumbersEnabled = lineNumbersEnabled;
    }

    public void setColorEnabled(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public SourceFile getSourceFile() {
        return currentFile;
    }

    public void displayCode(SourceFile file) {
        this.currentFile = file;
        this.text.setText("");
        try {
            for (CodeElement elem : file.getElements()) {
                if (elem.isBlock()) {
                    String indent = "";
                    for (int i = 0; i < elem.getIndentLevel(); i++) {
                        indent += "    ";
                    }
                    switch (elem.getState()) {
                        case ADDED:
                            StyleConstants.setBackground(style, COLOR_ADDED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "") + "+ " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        case REMOVED:
                            StyleConstants.setBackground(style, COLOR_REMOVED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        case MODIFIED:
                            StyleConstants.setBackground(style, COLOR_MODIFIED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        default:
                            StyleConstants.setBackground(style, Color.WHITE);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                    }
                    displayCode(elem);
                    if (elem.getType() != ElementType.MULTILINE_COMMENT) {
                        switch (elem.getState()) {
                            case ADDED:
                                StyleConstants.setBackground(style, COLOR_ADDED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "+ " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            case REMOVED:
                                StyleConstants.setBackground(style, COLOR_REMOVED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "- " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            case MODIFIED:
                                StyleConstants.setBackground(style, COLOR_MODIFIED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "* " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            default:
                                StyleConstants.setBackground(style, Color.WHITE);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "  " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                        }
                    }
                }
                else {
                    switch (elem.getState()) {
                        case ADDED:
                            StyleConstants.setBackground(style, COLOR_ADDED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "+ " + elem + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        case REMOVED:
                            StyleConstants.setBackground(style, COLOR_REMOVED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + elem + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        case MODIFIED:
                            StyleConstants.setBackground(style, COLOR_MODIFIED);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + elem + "\n"), (colorEnabled) ? style : defaultStyle);
                            break;
                        default:
                            StyleConstants.setBackground(style, Color.WHITE);
                            doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + elem + "\n"), (colorEnabled) ? style : defaultStyle);
                    }
                }
            }
        }
        catch (BadLocationException ex) {
            Logger.getInstance().log(new Message("Bad location; could not display code.", Message.Type.ERROR));
        }
    }

    private void displayCode(CodeElement element) throws BadLocationException {
        if (element.isBlock()) {
            for (CodeElement elem : element.getChildren()) {
                String indent = "";
                for (int i = 0; i < elem.getIndentLevel(); i++) {
                    indent += "    ";
                }
                switch (elem.getState()) {
                    case ADDED:
                        StyleConstants.setBackground(style, COLOR_ADDED);
                        doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "+ " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                        break;
                    case REMOVED:
                        StyleConstants.setBackground(style, COLOR_REMOVED);
                        doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "- " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                        break;
                    case MODIFIED:
                        StyleConstants.setBackground(style, COLOR_MODIFIED);
                        doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "* " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                        break;
                    default:
                        StyleConstants.setBackground(style, Color.WHITE);
                        doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (elem.getLineNum() + getIndentForLineNum(elem.getLineNum())) : "")  + "  " + indent + elem.getHeader() + "\n"), (colorEnabled) ? style : defaultStyle);
                }
                if (elem.isBlock()) {
                    displayCode(elem);
                    if (elem.getType() != ElementType.MULTILINE_COMMENT) {
                        switch (elem.getState()) {
                            case ADDED:
                                StyleConstants.setBackground(style, COLOR_ADDED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "+ " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            case REMOVED:
                                StyleConstants.setBackground(style, COLOR_REMOVED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "- " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            case MODIFIED:
                                StyleConstants.setBackground(style, COLOR_MODIFIED);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "* " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                                break;
                            default:
                                StyleConstants.setBackground(style, Color.WHITE);
                                doc.insertString(doc.getLength(), ((lineNumbersEnabled ? ((elem.getLineNum() + elem.getNumLines() - 1) + getIndentForLineNum(elem.getLineNum())) : "") + "  " + indent + "}\n"), (colorEnabled) ? style : defaultStyle);
                        }
                    }
                }
            }
        }
        else {
            switch (element.getState()) {
                case ADDED:
                    StyleConstants.setBackground(style, COLOR_ADDED);
                    doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "+ " + element + "\n"), (colorEnabled) ? style : defaultStyle);
                    break;
                case REMOVED:
                    StyleConstants.setBackground(style, COLOR_REMOVED);
                    doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "- " + element + "\n"), (colorEnabled) ? style : defaultStyle);
                    break;
                case MODIFIED:
                    StyleConstants.setBackground(style, COLOR_MODIFIED);
                    doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "* " + element + "\n"), (colorEnabled) ? style : defaultStyle);
                    break;
                default:
                    StyleConstants.setBackground(style, Color.WHITE);
                    doc.insertString(doc.getLength(), ((lineNumbersEnabled ? (element.getLineNum() + getIndentForLineNum(element.getLineNum())) : "")  + "  " + element + "\n"), (colorEnabled) ? style : defaultStyle);
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
