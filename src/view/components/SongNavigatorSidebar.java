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
        
        int scrollBarWidth = (javax.swing.UIManager.get("ScrollBar.width") != null) 
            ? (Integer) javax.swing.UIManager.get("ScrollBar.width") 
            : new javax.swing.JScrollBar(javax.swing.JScrollBar.VERTICAL).getPreferredSize().width;
        setPreferredSize(new Dimension(250 + scrollBarWidth, 0));
        
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
        
        JScrollPane scrollPane = new JScrollPane(sectionsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(30, 32, 36));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Updates the navigator list with the currently defined sections.
     */
    public void updateSections(java.util.List<SongLinePanel> panels, java.util.function.Consumer<SongLinePanel> onSectionClick) {
        sectionsPanel.removeAll();
        for (SongLinePanel panel : panels) {
            String name = panel.getSectionLabelField().getText().trim();
            if (!name.isEmpty()) {
                addSection(name, panel, onSectionClick);
            }
        }
        sectionsPanel.revalidate();
        sectionsPanel.repaint();
    }

    /**
     * Adds a section label to the navigator list.
     * @param name The name of the section.
     */
    private void addSection(String name, SongLinePanel targetPanel, java.util.function.Consumer<SongLinePanel> onSectionClick) {
        JLabel sectionLabel = new JLabel("  " + name);
        sectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionLabel.setForeground(new Color(180, 180, 180));
        sectionLabel.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        // Wrap in panel for full width highlight on hover (future feature)
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(30, 32, 36));
        wrapper.add(sectionLabel, BorderLayout.WEST);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        // Make it clickable to scroll to the panel
        wrapper.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        wrapper.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onSectionClick != null) {
                    onSectionClick.accept(targetPanel);
                }
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                wrapper.setBackground(new Color(50, 52, 56));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                wrapper.setBackground(new Color(30, 32, 36));
            }
        });
        
        sectionsPanel.add(wrapper);
        sectionsPanel.add(Box.createVerticalStrut(2));
    }
}