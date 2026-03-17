package view.listeners;

import view.components.SongLinePanel;

/**
 * Defines a contract for listening to user-initiated actions 
 * on a specific SongLinePanel.
 */
public interface SongLineActionObserver {
    void onRemove(SongLinePanel panel);
    void onCopy(SongLinePanel panel);
    void onPasteBelow(SongLinePanel panel);
    void onDuplicate(SongLinePanel panel);
    void onFocus(SongLinePanel panel);
}