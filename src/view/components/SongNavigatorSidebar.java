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
import javax.swing.border.EmptyBorder;

/**
 * A dynamic index of user-defined song sections (Intro, Verse, Chorus)
 * that can be clicked through. Forms the Left Sidebar of the application.
 */
public class SongNavigatorSidebar extends JPanel {
    private final JPanel sectionsPanel;

    public SongNavigatorSidebar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));
        setBackground(new Color(30, 32, 36));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 52, 56)));

        JLabel titleLabel = new JLabel("Song Navigator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        add(titleLabel, BorderLayout.NORTH);

        sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setBackground(new Color(30, 32, 36));
        
        // Initial placeholder items, to be populated dynamically later
        addSection("Intro");
        addSection("Verse 1");
        addSection("Chorus");
        addSection("Verse 2");
        addSection("Bridge");
        addSection("Chorus");
        addSection("Outro");
        
        JScrollPane scrollPane = new JScrollPane(sectionsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 32, 36));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Adds a section label to the navigator list.
     * @param name The name of the section.
     */
    public void addSection(String name) {
        JLabel sectionLabel = new JLabel("  " + name);
        sectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionLabel.setForeground(new Color(180, 180, 180));
        sectionLabel.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        // Wrap in panel for full width highlight on hover (future feature)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(30, 32, 36));
        wrapper.add(sectionLabel, BorderLayout.WEST);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        sectionsPanel.add(wrapper);
        sectionsPanel.add(Box.createVerticalStrut(2));
    }
}