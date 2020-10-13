package optimizationprototype.gui;

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
        this.setBorder(new TitledBorder(new EtchedBorder(), title));
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(pane, BorderLayout.CENTER);
    }

}
