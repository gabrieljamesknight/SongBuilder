package view.components;

import java.util.function.Consumer;

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
            Consumer<SongLinePanel> callback = targetPanel.getOnCopyCallback();
            if (callback != null) callback.accept(targetPanel);
        });

        JMenuItem pasteBelowItem = new JMenuItem("Paste Line Below");
        pasteBelowItem.addActionListener(e -> {
            Consumer<SongLinePanel> callback = targetPanel.getOnPasteBelowCallback();
            if (callback != null) callback.accept(targetPanel);
        });

        JMenuItem duplicateItem = new JMenuItem("Duplicate Line");
        duplicateItem.addActionListener(e -> {
            Consumer<SongLinePanel> callback = targetPanel.getOnDuplicateCallback();
            if (callback != null) callback.accept(targetPanel);
        });

        JMenuItem clearItem = new JMenuItem("Clear Contents");
        clearItem.addActionListener(e -> {
            if (onClearCallback != null) onClearCallback.run();
        });

        JMenuItem deleteItem = new JMenuItem("Delete Line");
        deleteItem.addActionListener(e -> {
            Consumer<SongLinePanel> callback = targetPanel.getOnRemoveCallback();
            if (callback != null) callback.accept(targetPanel);
        });

        this.add(copyItem);
        this.add(pasteBelowItem);
        this.add(duplicateItem);
        this.addSeparator();
        this.add(clearItem);
        this.addSeparator();
        this.add(deleteItem);
    }
}