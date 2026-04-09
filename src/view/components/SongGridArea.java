package view.components;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import model.Song;
import model.SongLine;
import model.Tablature;
import view.listeners.PanelDragDropHandler;
import view.listeners.SongLineActionObserver;

/**
 * Encapsulates the scrollable grid of song lines, handling panel lifecycle,
 * drag-and-drop reordering, clipboard operations, and focus state management.
 */
public class SongGridArea extends JPanel {

    private final ArrayList<SongLinePanel> songLinePanels;
    private final JPanel songLinePanelContainer;
    private final JScrollPane scrollPane;
    private final PanelDragDropHandler dragDropHandler;
    
    private Consumer<Integer> removeLineCallback = (index) -> {};
    private SongLine clipboardLine = null;
    private SongLinePanel lastFocusedPanel = null;

    private final SongLineActionObserver panelActionObserver = new SongLineActionObserver() {
        @Override public void onRemove(SongLinePanel panel) { removeLinePanel(panel); }
        @Override public void onCopy(SongLinePanel panel) { copyLinePanel(panel); }
        @Override public void onPasteBelow(SongLinePanel panel) { pasteLineBelowPanel(panel); }
        @Override public void onDuplicate(SongLinePanel panel) { duplicateLinePanel(panel); }
        @Override public void onFocus(SongLinePanel panel) { setActivePanel(panel); }
    };

    public SongGridArea() {
        setLayout(new BorderLayout());
        songLinePanels = new ArrayList<>();
        
        songLinePanelContainer = new JPanel();
        songLinePanelContainer.setLayout(new BoxLayout(songLinePanelContainer, BoxLayout.Y_AXIS));
        songLinePanelContainer.setBorder(new EmptyBorder(15, 10, 45, 10));
        songLinePanelContainer.setBackground(new java.awt.Color(25, 27, 30));
        songLinePanelContainer.setFocusable(true);

        scrollPane = new JScrollPane(songLinePanelContainer);
        scrollPane.getViewport().setBackground(new java.awt.Color(25, 27, 30));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);    
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        add(scrollPane, BorderLayout.CENTER);

        dragDropHandler = new PanelDragDropHandler(
            songLinePanelContainer, 
            songLinePanels, 
            this::updateAllLineNumbers
        );

        setupFocusClearing();
    }

    public void setRemoveLineCallback(Consumer<Integer> callback) {
        this.removeLineCallback = callback;
    }

    public void addLineAction() {
        SongLinePanel newPanel = new SongLinePanel();
        newPanel.setActionObserver(panelActionObserver);

        if (!songLinePanels.isEmpty()) {
            songLinePanelContainer.add(Box.createVerticalStrut(20));
        }

        newPanel.getDragHandle().addMouseListener(dragDropHandler);
        newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);
        
        songLinePanels.add(newPanel);
        songLinePanelContainer.add(newPanel);
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        revalidate();
        repaint();
    }

    private void removeLinePanel(SongLinePanel panelToRemove) {
        if (this.lastFocusedPanel == panelToRemove) {
            setActivePanel(null);
        }
        
        int index = songLinePanels.indexOf(panelToRemove);
        if (index != -1) {
            removeLineCallback.accept(index);
            songLinePanels.remove(index);
            rebuildContainer();
        }
    }

    public void loadSong(Song song) {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();

        for (int i = 0; i < song.getSongLines().size(); i++) {
            SongLine songLine = song.getSongLines().get(i);
            SongLinePanel newPanel = new SongLinePanel();
            
            newPanel.getChordsField().setText(songLine.getChords());
            newPanel.getLyricsField().setText(songLine.getLyrics());
            newPanel.getTablatureArea().setText(songLine.getTablature().toString());
            
            if (songLine.getSectionLabel() != null) {
                newPanel.getSectionLabelField().setText(songLine.getSectionLabel());
            }
            
            newPanel.updateSongLine();
            newPanel.setActionObserver(panelActionObserver);

            if (i > 0) {
                songLinePanelContainer.add(Box.createVerticalStrut(20));
            }

            newPanel.getDragHandle().addMouseListener(dragDropHandler);
            newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);
            
            songLinePanelContainer.add(newPanel);
            songLinePanels.add(newPanel);
        }
        revalidate();
        repaint();
    }

    public void clearGrid() {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        addLineAction();
    }

    public void propagateTuningChange(int stringIndex, String newTuning) {
        for (SongLinePanel panel : songLinePanels) {
            panel.updateTuningVisually(stringIndex, newTuning);
        }
    }

    private void updateAllLineNumbers() {
        for (int i = 0; i < songLinePanels.size(); i++) {
        }
    }

    private void rebuildContainer() {
        songLinePanelContainer.removeAll();
        for (int i = 0; i < songLinePanels.size(); i++) {
            if (i > 0) {
                songLinePanelContainer.add(Box.createVerticalStrut(20));
            }
            songLinePanelContainer.add(songLinePanels.get(i));
        }
        revalidate();
        repaint();
    }

    private void copyLinePanel(SongLinePanel panel) {
        panel.updateSongLine();
        SongLine source = panel.getSongLine();
        this.clipboardLine = new SongLine(
            source.getChords(), 
            source.getLyrics(), 
            Tablature.parseTablature(source.getTablature().toString())
        );
        this.clipboardLine.setSectionLabel(source.getSectionLabel());
    }

    private void pasteLineBelowPanel(SongLinePanel targetPanel) {
        if (this.clipboardLine == null) return;
        
        int targetIndex = songLinePanels.indexOf(targetPanel);
        if (targetIndex == -1) return;

        SongLinePanel newPanel = new SongLinePanel();
        newPanel.getChordsField().setText(this.clipboardLine.getChords());
        newPanel.getLyricsField().setText(this.clipboardLine.getLyrics());
        newPanel.getTablatureArea().setText(this.clipboardLine.getTablature().toString());
        newPanel.getSectionLabelField().setText(this.clipboardLine.getSectionLabel() != null ? this.clipboardLine.getSectionLabel() : "");
        newPanel.updateSongLine();

        newPanel.setActionObserver(panelActionObserver);
        newPanel.getDragHandle().addMouseListener(dragDropHandler);
        newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);

        songLinePanels.add(targetIndex + 1, newPanel);
        rebuildContainer();
    }

    private void duplicateLinePanel(SongLinePanel targetPanel) {
        copyLinePanel(targetPanel);
        pasteLineBelowPanel(targetPanel);
    }

    public void setActivePanel(SongLinePanel panel) {
        if (this.lastFocusedPanel != null && this.lastFocusedPanel != panel) {
            this.lastFocusedPanel.setForceHighlight(false);
        }
        this.lastFocusedPanel = panel;
        if (this.lastFocusedPanel != null) {
            this.lastFocusedPanel.setForceHighlight(true);
        }
    }

    public SongLinePanel getTargetPanel() {
        if (lastFocusedPanel != null && songLinePanels.contains(lastFocusedPanel)) {
            return lastFocusedPanel;
        }
        if (!songLinePanels.isEmpty()) {
            return songLinePanels.get(songLinePanels.size() - 1);
        }
        return null;
    }

    private void setupFocusClearing() {
        MouseAdapter clearFocusAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                songLinePanelContainer.requestFocusInWindow();
                setActivePanel(null);
            }
        };
        songLinePanelContainer.addMouseListener(clearFocusAdapter);
        scrollPane.getViewport().addMouseListener(clearFocusAdapter);
    }

    public void performActionOnTarget(String action) {
        SongLinePanel target = getTargetPanel();
        if (target == null) return;

        switch (action) {
            case "COPY" -> copyLinePanel(target);
            case "PASTE" -> pasteLineBelowPanel(target);
            case "DUPLICATE" -> duplicateLinePanel(target);
            case "CLEAR" -> target.clearContents();
            case "REMOVE" -> removeLinePanel(target);
        }
    }

    public List<SongLinePanel> getPanels() {
        return songLinePanels;
    }
}