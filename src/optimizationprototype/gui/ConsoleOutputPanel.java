package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ConsoleOutputPanel extends JPanel {

    private JTextArea logAll, logGeneral, logError, logSuggestion, logCompiler;
    private JTabbedPane tabbedPane;
    private JScrollPane scrollPaneAll, scrollPaneGeneral, scrollPaneError, scrollPaneSuggestion, scrollPaneCompiler;
    private JButton clearButton;
    private boolean suggestionsEnabled;

    public ConsoleOutputPanel() {
        tabbedPane = new JTabbedPane(SwingConstants.TOP);
        suggestionsEnabled = true;
        initTabs();
        initClearButton();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Console Output");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        this.setLayout(new BorderLayout());
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(clearButton, BorderLayout.SOUTH);
    }

    public void appendMessage(Message message) {
        if (message.type == Message.Type.SUGGESTION && !suggestionsEnabled)
            return;
        logAll.append(message + "\n");
        switch (message.type) {
            case GENERAL:
                logGeneral.append(message + "\n");
                break;
            case ERROR:
                logError.append(message + "\n");
                break;
            case SUGGESTION:
                logSuggestion.append(message + "\n");
                break;
            case COMPILER:
                logCompiler.append(message + "\n");
                break;
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void initTabs() {
        logAll = new JTextArea();
        logAll.setEditable(false);
        logAll.setLineWrap(true);
        logAll.setWrapStyleWord(true);
        scrollPaneAll = new JScrollPane(logAll);
        scrollPaneAll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("All", scrollPaneAll);
        logGeneral = new JTextArea();
        logGeneral.setEditable(false);
        logGeneral.setLineWrap(true);
        logGeneral.setWrapStyleWord(true);
        scrollPaneGeneral = new JScrollPane(logGeneral);
        scrollPaneGeneral.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("General", scrollPaneGeneral);
        logError = new JTextArea();
        logError.setEditable(false);
        logError.setLineWrap(true);
        logError.setWrapStyleWord(true);
        scrollPaneError = new JScrollPane(logError);
        scrollPaneError.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Errors", scrollPaneError);
        logSuggestion = new JTextArea();
        logSuggestion.setEditable(false);
        logSuggestion.setLineWrap(true);
        logSuggestion.setWrapStyleWord(true);
        scrollPaneSuggestion = new JScrollPane(logSuggestion);
        scrollPaneSuggestion.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Suggestions", scrollPaneSuggestion);
        logCompiler = new JTextArea();
        logCompiler.setEditable(false);
        logCompiler.setLineWrap(true);
        logCompiler.setWrapStyleWord(true);
        scrollPaneCompiler = new JScrollPane(logCompiler);
        scrollPaneCompiler.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Compiler Output", scrollPaneCompiler);
    }

    public void initClearButton() {
        clearButton = new JButton("Clear Log Entries");
        clearButton.setFont(GuiOptions.BUTTON_FONT);
        clearButton.addActionListener(e -> {
            Logger.getInstance().clear();
            logAll.setText("");
            logGeneral.setText("");
            logError.setText("");
            logSuggestion.setText("");
            logCompiler.setText("");
        });
    }

    public void setSuggestionsEnabled(boolean selected) {
        suggestionsEnabled = selected;
    }
}
