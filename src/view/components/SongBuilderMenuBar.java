package view.components;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.DefaultEditorKit;

/**
 * Constructs the main menu bar for the application and routes user actions 
 * to the provided action handlers.
 */
public class SongBuilderMenuBar extends JMenuBar {

    private final Runnable onNewSong;
    private final Runnable onSaveSong;
    private final Runnable onSaveSongAs;
    private final Runnable onLoadSong;
    private final Runnable onAddLine;
    private final Runnable onCopyLine;
    private final Runnable onPasteLine;
    private final Runnable onDuplicateLine;
    private final Runnable onClearLine;
    private final Runnable onDeleteLine;

    /**
     * Constructs the menu bar with injected action dependencies.
     * * @param actionMap The action map from the main text field for copy/paste support.
     * @param onNewSong Callback for creating a new song.
     * @param onSaveSong Callback for saving the current song.
     * @param onSaveSongAs Callback for saving as a new file.
     * @param onLoadSong Callback for loading a song.
     * @param onAddLine Callback for adding a new song line.
     */
    public SongBuilderMenuBar(ActionMap actionMap, Runnable onNewSong, Runnable onSaveSong, 
                              Runnable onSaveSongAs, Runnable onLoadSong, Runnable onAddLine,
                              Runnable onCopyLine, Runnable onPasteLine, Runnable onDuplicateLine,
                              Runnable onClearLine, Runnable onDeleteLine) {
        this.onNewSong = onNewSong;
        this.onSaveSong = onSaveSong;
        this.onSaveSongAs = onSaveSongAs;
        this.onLoadSong = onLoadSong;
        this.onAddLine = onAddLine;
        
        this.onCopyLine = onCopyLine;
        this.onPasteLine = onPasteLine;
        this.onDuplicateLine = onDuplicateLine;
        this.onClearLine = onClearLine;
        this.onDeleteLine = onDeleteLine;

        buildFileMenu();
        buildEditMenu(actionMap);
    }

    private void buildFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newSongMenuItem = new JMenuItem("New File");
        newSongMenuItem.addActionListener(e -> onNewSong.run());
        
        JMenuItem saveSongMenuItem = new JMenuItem("Save");
        saveSongMenuItem.addActionListener(e -> onSaveSong.run());
        
        JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
        saveAsMenuItem.addActionListener(e -> onSaveSongAs.run());
        
        JMenuItem loadSongMenuItem = new JMenuItem("Load...");
        loadSongMenuItem.addActionListener(e -> onLoadSong.run());
        
        JMenuItem exitSongMenuItem = new JMenuItem("Exit");
        exitSongMenuItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newSongMenuItem);
        fileMenu.add(saveSongMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(loadSongMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitSongMenuItem);
        
        this.add(fileMenu);
    }

    private void buildEditMenu(ActionMap actionMap) {
        JMenu editMenu = new JMenu("Edit");
        
        JMenuItem addLineMenuItem = new JMenuItem("Add Line...");
        addLineMenuItem.addActionListener(e -> onAddLine.run());
        
        JMenuItem cutMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.cutAction));
        cutMenuItem.setText("Cut");
        
        JMenuItem copyMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.copyAction));
        copyMenuItem.setText("Copy");
        
        JMenuItem pasteMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.pasteAction));
        pasteMenuItem.setText("Paste");
        
        editMenu.add(addLineMenuItem);
        editMenu.addSeparator();
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        
        JMenuItem copyLineItem = new JMenuItem("Copy Line");
        copyLineItem.addActionListener(e -> onCopyLine.run());
        
        JMenuItem pasteLineItem = new JMenuItem("Paste Line Below");
        pasteLineItem.addActionListener(e -> onPasteLine.run());
        
        JMenuItem duplicateLineItem = new JMenuItem("Duplicate Line");
        duplicateLineItem.addActionListener(e -> onDuplicateLine.run());
        
        JMenuItem clearLineItem = new JMenuItem("Clear Line Contents");
        clearLineItem.addActionListener(e -> onClearLine.run());
        
        JMenuItem deleteLineItem = new JMenuItem("Delete Line");
        deleteLineItem.addActionListener(e -> onDeleteLine.run());

        editMenu.add(copyLineItem);
        editMenu.add(pasteLineItem);
        editMenu.add(duplicateLineItem);
        editMenu.add(clearLineItem);
        editMenu.add(deleteLineItem);
        
        this.add(editMenu);
    }
}