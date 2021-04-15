package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;
import optimizationprototype.util.Logger;
import optimizationprototype.util.Message;
import optimizationprototype.util.SourceHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

    public void initMenuBar(ActionListener importActionListener, ActionListener exportActionListener, CodePreviewPanel originalPanel, CodePreviewPanel optimizedPanel, ConsoleOutputPanel consoleOutputPanel) {
        JMenuItem importItem = new JMenuItem("Import File");
        importItem.addActionListener(importActionListener);
        fileMenu.add(importItem);
        JMenuItem exportItem = new JMenuItem("Export File");
        exportItem.addActionListener(exportActionListener);
        fileMenu.add(exportItem);

        // options menu
        JCheckBoxMenuItem enableSuggestionsCheckbox = new JCheckBoxMenuItem("Enable suggestions");
        enableSuggestionsCheckbox.setSelected(true);
        SourceHandler.getInstance().setSuggestionsEnabled(true);
        enableSuggestionsCheckbox.addActionListener(e -> {
            consoleOutputPanel.setSuggestionsEnabled(enableSuggestionsCheckbox.isSelected());
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
            if (enableLineNumbers.isSelected())
                Logger.getInstance().log(new Message("Line numbers enabled.", Message.Type.GENERAL));
            else
                Logger.getInstance().log(new Message("Line numbers disabled.", Message.Type.GENERAL));
        });
        optionsMenu.add(enableLineNumbers);
        JCheckBoxMenuItem enableColor = new JCheckBoxMenuItem("Enable diff color");
        enableColor.setSelected(true);
        optimizedPanel.setLineNumbersEnabled(true);
        originalPanel.setLineNumbersEnabled(true);
        enableColor.addActionListener(e -> {
            optimizedPanel.setColorEnabled(enableColor.isSelected());
            if (optimizedPanel.getSourceFile() != null)
                optimizedPanel.displayCode(optimizedPanel.getSourceFile());
            originalPanel.setColorEnabled(enableColor.isSelected());
            if (originalPanel.getSourceFile() != null)
                originalPanel.displayCode(originalPanel.getSourceFile());
            if (enableColor.isSelected())
                Logger.getInstance().log(new Message("Colored diff output enabled.", Message.Type.GENERAL));
            else
                Logger.getInstance().log(new Message("Colored diff output disabled.", Message.Type.GENERAL));
        });
        optionsMenu.add(enableColor);
        JMenu fontMenu = new JMenu("Font");
        JRadioButtonMenuItem defaultFontItem = new JRadioButtonMenuItem("Courier New");
        defaultFontItem.setSelected(true);
        defaultFontItem.setEnabled(GuiOptions.fontFlag);
        fontMenu.add(defaultFontItem);
        JRadioButtonMenuItem customFontItem = new JRadioButtonMenuItem("JetBrains Mono");
        customFontItem.setSelected(false);
        customFontItem.setEnabled(GuiOptions.fontFlag);
        fontMenu.add(customFontItem);
        defaultFontItem.addActionListener(e -> {
            defaultFontItem.setSelected(true);
            customFontItem.setSelected(false);
            originalPanel.setCustomFont(customFontItem.isSelected());
            optimizedPanel.setCustomFont(customFontItem.isSelected());
            SwingUtilities.updateComponentTreeUI(originalPanel);
            SwingUtilities.updateComponentTreeUI(optimizedPanel);
        });
        customFontItem.addActionListener(e -> {
            customFontItem.setSelected(true);
            defaultFontItem.setSelected(false);
            originalPanel.setCustomFont(customFontItem.isSelected());
            optimizedPanel.setCustomFont(customFontItem.isSelected());
            SwingUtilities.updateComponentTreeUI(originalPanel);
            SwingUtilities.updateComponentTreeUI(optimizedPanel);
        });
        optionsMenu.add(fontMenu);

        // config menu
        JMenu targetMenu = new JMenu("Target MCU");
        ButtonGroup targetButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem atmega168Item = new JRadioButtonMenuItem("ATmega168");
        atmega168Item.setSelected(true);
        atmega168Item.setEnabled(false);
        targetButtonGroup.add(atmega168Item);
        targetMenu.add(atmega168Item);
        configMenu.add(targetMenu);
        JMenuItem frequencyItem = new JMenuItem("Set Frequency");
        frequencyItem.addActionListener(e -> {
            String result = JOptionPane.showInputDialog(this.getParent(), "Enter Desired Frequency:\nCurrent Value = " + SourceHandler.getInstance().getDefaultFrequency() + " Hz.", "Frequency", JOptionPane.QUESTION_MESSAGE);
            try {
                if (result != null && result.length() > 0)
                SourceHandler.getInstance().setDefaultFrequency(Integer.parseInt(result));
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this.getParent(), "Input must be an integer value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        configMenu.add(frequencyItem);

        // help menu
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
