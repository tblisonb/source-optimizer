package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;
import optimizationprototype.util.SourceHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URI;

public class OptimizerMenuBar extends JMenuBar {

    private JMenu fileMenu;
    private JMenu optionsMenu;
    private JMenu configMenu;
    private JMenu helpMenu;

    public OptimizerMenuBar() {
        fileMenu = new JMenu("File");
        optionsMenu = new JMenu("Options");
        configMenu = new JMenu("Config");
        helpMenu = new JMenu("Help");
    }

    public void initMenuBar(ActionListener importActionListener, ActionListener exportActionListener, CodePreviewPanel originalPanel, CodePreviewPanel optimizedPanel) {
        JMenuItem importItem = new JMenuItem("Import File");
        importItem.addActionListener(importActionListener);
        fileMenu.add(importItem);
        JMenuItem exportItem = new JMenuItem("Export File");
        exportItem.addActionListener(exportActionListener);
        fileMenu.add(exportItem);
        JCheckBoxMenuItem enableSuggestionsCheckbox = new JCheckBoxMenuItem("Enable suggestions");
        enableSuggestionsCheckbox.setSelected(true);
        SourceHandler.getInstance().setSuggestionsEnabled(true);
        enableSuggestionsCheckbox.addActionListener(e -> {
            if (enableSuggestionsCheckbox.isSelected())
                Logger.getInstance().log(new Message("Suggestions enabled.", Message.Type.GENERAL));
            else
                Logger.getInstance().log(new Message("Suggestions disabled.", Message.Type.GENERAL));
        });
        optionsMenu.add(enableSuggestionsCheckbox);
        JCheckBoxMenuItem enableLineNumbers = new JCheckBoxMenuItem("Enable line numbers");
        enableLineNumbers.setSelected(true);
        optimizedPanel.setLineNumbersEnabled(true);
        originalPanel.setLineNumbersEnabled(true);
        enableLineNumbers.addActionListener(e -> {
            optimizedPanel.setLineNumbersEnabled(enableLineNumbers.isSelected());
            if (optimizedPanel.getSourceFile() != null)
                optimizedPanel.displayCode(optimizedPanel.getSourceFile());
            originalPanel.setLineNumbersEnabled(enableLineNumbers.isSelected());
            if (originalPanel.getSourceFile() != null)
                originalPanel.displayCode(originalPanel.getSourceFile());
        });
        optionsMenu.add(enableLineNumbers);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI(GuiOptions.HELP_LINK));
                } catch (Exception ex) {
                    Logger.getInstance().log(new Message("Could not navigate to about page.", Message.Type.ERROR));
                }
            }
        });
        helpMenu.add(aboutItem);
        this.add(fileMenu);
        this.add(optionsMenu);
        this.add(configMenu);
        this.add(helpMenu);
    }

}
