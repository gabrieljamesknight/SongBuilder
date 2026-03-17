package view.components;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Encapsulates the right-click context menu for a SongLinePanel.
 * Resolves callbacks at runtime to ensure they are not null if set after initialization.
 */
public class SongLineContextMenu extends JPopupMenu {

    public SongLineContextMenu(SongLinePanel targetPanel, Runnable onClearCallback) {

        JMenuItem copyItem = new JMenuItem("Copy Line");
        copyItem.addActionListener(e -> {
            if (targetPanel.getActionObserver() != null) {
                targetPanel.getActionObserver().onCopy(targetPanel);
            }
        });

        JMenuItem pasteBelowItem = new JMenuItem("Paste Line Below");
        pasteBelowItem.addActionListener(e -> {
            if (targetPanel.getActionObserver() != null) {
                targetPanel.getActionObserver().onPasteBelow(targetPanel);
            }
        });

        JMenuItem duplicateItem = new JMenuItem("Duplicate Line");
        duplicateItem.addActionListener(e -> {
            if (targetPanel.getActionObserver() != null) {
                targetPanel.getActionObserver().onDuplicate(targetPanel);
            }
        });

        JMenuItem clearItem = new JMenuItem("Clear Contents");
        clearItem.addActionListener(e -> {
            if (onClearCallback != null) onClearCallback.run();
        });

        JMenuItem deleteItem = new JMenuItem("Delete Line");
        deleteItem.addActionListener(e -> {
            if (targetPanel.getActionObserver() != null) {
                targetPanel.getActionObserver().onRemove(targetPanel);
            }
        });

        this.add(copyItem);
        this.add(pasteBelowItem);
        this.add(duplicateItem);
        this.addSeparator();
        this.add(clearItem);
        this.addSeparator();
        this.add(deleteItem);
    }

    /**
     * Attaches this context menu to the specified components via a MouseAdapter.
     *
     * @param components The UI components that should trigger this context menu on right-click.
     */
    public void attachTo(java.awt.Component... components) {
        java.awt.event.MouseAdapter popupListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { showPopup(e); }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { showPopup(e); }

            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        for (java.awt.Component c : components) {
            c.addMouseListener(popupListener);
        }
    }
}