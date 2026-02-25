package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Song;
import model.SongLine;
import view.SongBuilderGUI;
import view.components.SongLinePanel;

/**
 * Orchestrates interactions between the SongBuilderGUI (View) and the SongManager (Model).
 * Handles file I/O dialogs, user actions, and synchronizes the UI state with the underlying data model.
 */
public class SongController {

    private final SongBuilderGUI gui;
    private final SongManager songManager;

    /**
     * Constructs the controller and binds UI actions.
     *
     * @param gui         The main graphical user interface view.
     * @param songManager The manager handling the core song data and file operations.
     */
    public SongController(SongBuilderGUI gui, SongManager songManager) {
        this.gui = gui;
        this.songManager = songManager;
        bindActions();
    }

    /**
     * Binds internal methods to the GUI's action listeners.
     */
    private void bindActions() {
        gui.setNewSongAction(this::handleNewSong);
        gui.setSaveSongAction(this::handleSaveSong);
        gui.setSaveSongAsAction(this::handleSaveSongAs);
        gui.setLoadSongAction(this::handleLoadSong);
        gui.setRemoveLineCallback(this::handleRemoveLine);
    }

    private void handleNewSong() {
        songManager.setCurrentSong(new Song("Untitled"));
        songManager.setCurrentFile(null);
        gui.resetGUI();
    }

    private void handleLoadSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        int returnValue = fileChooser.showOpenDialog(gui.getFrame());

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                songManager.loadSong(selectedFile);
                Song song = songManager.getCurrentSong();
                gui.refreshUIFromModel(song);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui.getFrame(), "Error loading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void handleSaveSong() {
        if (songManager.getCurrentFile() == null) {
            handleSaveSongAs();
        } else {
            updateModelFromUI();
            try {
                songManager.saveSong(songManager.getCurrentFile());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui.getFrame(), "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void handleSaveSongAs() {
        updateModelFromUI();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Song As...");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setSelectedFile(new File(songManager.getCurrentSong().getName() + ".json"));

        int returnValue = fileChooser.showSaveDialog(gui.getFrame());
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                songManager.saveSong(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(gui.getFrame(), "Song saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gui.getFrame(), "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void handleRemoveLine(int index) {
        songManager.removeSongLine(index);
    }

    /**
     * Parses the current UI state and updates the underlying Song model.
     */
    private void updateModelFromUI() {
        String[] tunings = gui.getTuningFieldsData();
        songManager.getCurrentSong().getSongLines().clear();

        List<SongLinePanel> panels = gui.getSongLinePanels();
        for (SongLinePanel panel : panels) {
            panel.updateSongLine();
            SongLine line = panel.getSongLine();

            for (int i = 0; i < 6; i++) {
                String t = tunings[i];
                if (t.length() < 2) t = String.format("%-2s", t);
                line.getTablature().setGuitarStringTuning(i, t);
            }
            songManager.getCurrentSong().addSongLine(line);
        }

        String name = gui.getSongName();
        if (name.trim().isEmpty()) name = "Untitled";
        songManager.getCurrentSong().setName(name);
    }
}