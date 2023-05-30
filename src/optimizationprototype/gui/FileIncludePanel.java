package optimizationprototype.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import optimizationprototype.config.GuiOptions;

/**
 *
 * @author tlisonbee
 */
public class FileIncludePanel extends JPanel {
    
    private Vector<String> headerFiles, sourceFiles;
    private JList<String> headerList, sourceList;
    private JScrollPane headerScrollPane, sourceScrollPane;
    private JPanel sourcePanel, headerPanel;
    public JButton addSourceButton, addHeaderButton;

    public FileIncludePanel() {
        headerFiles = new Vector<>();
        sourceFiles = new Vector<>();
        headerList = new JList<>();
        sourceList = new JList<>();
        headerScrollPane = new JScrollPane();
        sourceScrollPane = new JScrollPane();
        sourcePanel = new JPanel();
        headerPanel = new JPanel();
        addSourceButton = new JButton("Add Source File");
        addHeaderButton = new JButton("Add Header File");
        initFileIncludePanel();
    }
    
    public void initFileIncludePanel() {
        headerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sourceScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.setLayout(new GridLayout(2, 1));
        headerScrollPane.getViewport().add(headerList);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerScrollPane, BorderLayout.CENTER);
        addHeaderButton.setFont(GuiOptions.BUTTON_FONT);
        headerPanel.add(addHeaderButton, BorderLayout.SOUTH);
        addHeaderButton.setEnabled(false);
        this.add(headerPanel);
        sourceScrollPane.getViewport().add(sourceList);
        sourcePanel.setLayout(new BorderLayout());
        sourcePanel.add(sourceScrollPane, BorderLayout.CENTER);
        addSourceButton.setFont(GuiOptions.BUTTON_FONT);
        sourcePanel.add(addSourceButton, BorderLayout.SOUTH);
        addSourceButton.setEnabled(false);
        this.add(sourcePanel);
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Files");
        border.setTitleFont(GuiOptions.PANEL_HEADER_FONT);
        this.setBorder(border);
    }
    
    public void addHeaderFile(String headerFilePath) {
        headerFiles.add(headerFilePath);
        headerList.setListData(headerFiles);
    }
    
    public void removeHeaderFile(String headerFilePath) {
        for (int i = 0; i < headerFiles.size(); i++) {
            if (headerFiles.get(i).equals(headerFilePath)) {
                headerFiles.remove(i);
                break;
            }
        }
        headerList.setListData(headerFiles);
    }
    
    public void clearHeaderFiles() {
        headerFiles.clear();
        headerList.setListData(headerFiles);
    }
    
    public void addSourceFile(String sourceFilePath) {
        sourceFiles.add(sourceFilePath);
        sourceList.setListData(sourceFiles);
    }
    
    public void removeSourceFile(String sourceFilePath) {
        for (int i = 0; i < sourceFiles.size(); i++) {
            if (sourceFiles.get(i).equals(sourceFilePath)) {
                sourceFiles.remove(i);
                break;
            }
        }
        sourceList.setListData(sourceFiles);
    }
    
    public void clearSourceFiles() {
        sourceFiles.clear();
        sourceList.setListData(sourceFiles);
    }
    
}
