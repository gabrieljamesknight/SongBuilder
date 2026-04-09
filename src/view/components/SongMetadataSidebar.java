package view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * A dedicated area for global song settings like Capo position, Tempo (BPM), 
 * Time Signature, and scratchpad notes. Forms the Right Sidebar.
 */
public class SongMetadataSidebar extends JPanel {

    public SongMetadataSidebar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 0));
        setBackground(new Color(30, 32, 36));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(50, 52, 56)));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(30, 32, 36));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Song Metadata");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        contentPanel.add(createLabeledField("Capo:", new JSpinner(new SpinnerNumberModel(0, 0, 24, 1))));
        contentPanel.add(Box.createVerticalStrut(15));
        
        contentPanel.add(createLabeledField("Tempo (BPM):", new JSpinner(new SpinnerNumberModel(120, 30, 300, 1))));
        contentPanel.add(Box.createVerticalStrut(15));
        
        contentPanel.add(createLabeledField("Time Sig:", new JTextField("4/4")));
        contentPanel.add(Box.createVerticalStrut(20));

        JLabel scratchpadLabel = new JLabel("Scratchpad (Ideas)");
        scratchpadLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        scratchpadLabel.setForeground(new Color(180, 180, 180));
        scratchpadLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(scratchpadLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        JTextArea scratchpadArea = new JTextArea();
        scratchpadArea.setLineWrap(true);
        scratchpadArea.setWrapStyleWord(true);
        scratchpadArea.setBackground(new Color(40, 42, 46));
        scratchpadArea.setForeground(new Color(220, 220, 220));
        scratchpadArea.setCaretColor(Color.WHITE);
        scratchpadArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(scratchpadArea);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 62, 66)));
        contentPanel.add(scrollPane);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createLabeledField(String labelText, javax.swing.JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(new Color(30, 32, 36));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(180, 180, 180));
        label.setPreferredSize(new Dimension(80, 25));
        
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        panel.add(label);
        panel.add(field);
        return panel;
    }
}