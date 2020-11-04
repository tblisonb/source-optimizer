package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.util.Logger;
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
        enableSuggestionsCheckbox.addActionListener(e -> {
            SourceHandler.getInstance().setSuggestionsEnabled(enableSuggestionsCheckbox.isSelected());
            if (enableSuggestionsCheckbox.isSelected())
                Logger.getInstance().log("Suggestions enabled.");
            else
                Logger.getInstance().log("Suggestions disabled.");
        });
        optionsMenu.add(enableSuggestionsCheckbox);
        JCheckBoxMenuItem enableLineNumbers = new JCheckBoxMenuItem("Enable line numbers");
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
                    Logger.getInstance().log("Could not navigate to about page.");
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
