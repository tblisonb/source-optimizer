package optimizationprototype.gui;

import optimizationprototype.config.GuiOptions;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class ConsoleOutputPanel extends JPanel {

    public final JTextArea log;
    private JScrollPane pane;

    public ConsoleOutputPanel() {
        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        ((DefaultCaret) log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        pane = new JScrollPane(log);
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Console Output");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
        this.setLayout(new BorderLayout());
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(pane);
    }

}
