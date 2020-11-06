package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ConsoleOutputPanel extends JPanel {

    private JTextArea logAll, logGeneral, logError, logSuggestion;
    private JTabbedPane tabbedPane;
    private JScrollPane scrollPaneAll, scrollPaneGeneral, scrollPaneError, scrollPaneSuggestion;
    private JButton clearButton;

    public ConsoleOutputPanel() {
        tabbedPane = new JTabbedPane(SwingConstants.TOP);
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
        });
    }

}
