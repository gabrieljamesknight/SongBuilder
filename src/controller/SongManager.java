package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import javax.swing.JOptionPane;
import model.Song;
import model.SongLine;
import model.Tablature;

/**
 * Manages the lifecycle and persistence of Song objects.
 * Refactored to use JSON serialization for improved data portability and robustness.
 * * @author SongBuilder Helper
 * @version 2.0
 */
public class SongManager {
    
    /** The current song being edited in the application. */
    private Song currentSong;
    
    /** The default directory path for saving/loading files. */
    private final String directoryPath = System.getProperty("user.dir");
    
    /** Gson instance configured for pretty printing JSON output. */
    private final Gson gson;

    private File currentFile; // Tracks the currently loaded/saved file for better user experience

    /**
     * Constructor initializes a new song and configures the JSON mapper.
     */
    public SongManager() {
        this.currentSong = new Song("New Song");
        // Pretty printing makes the save file human-readable/debuggable
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
 
    /**
     * Removes a line from the current song model.
     * * @param index The zero-based index of the line to remove.
     */
    public void removeSongLine(int index) {
        if (index >= 0 && index < currentSong.getSongLines().size()) {
            currentSong.removeLine(index);
        } else {
            System.err.println("Attempted to remove invalid song line index: " + index);
        }
    }

    /**
     * Persists the current Song object to a JSON file.
     * * @param filename The name of the file to save (e.g., "MySong.json").
     * @throws IOException If file writing fails.
     */
    public void saveSong(File file) throws IOException {
        String filename = file.getName();
        if (!filename.toLowerCase().endsWith(".json")) {
            file = new File(file.getParentFile(), filename + ".json");
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(currentSong, writer);
        }
        
        this.currentFile = file;
    }
    
    /**
     * Loads a Song object from a JSON file.
     * * @param filename The name of the file to load.
     * @throws IOException If file reading fails.
     */
    public void loadSongFromFile(String filename) throws IOException {
        File file = new File(directoryPath, filename);
        
        try (Reader reader = new FileReader(file)) {
            // Deserializes the JSON back into the Song object graph
            Song loadedSong = gson.fromJson(reader, Song.class);
            
            if (loadedSong != null) {
                this.currentSong = loadedSong;
            } else {
                throw new IOException("File contained invalid song data.");
            }
        }
    }

    /**
     * Retrieves the current song instance.
     * * @return The active Song object.
     */
    public Song getCurrentSong() {
        return currentSong;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Retrieves the tablature from the first line of the song.
     * Useful for determining global tuning.
     * * @return The Tablature object of the first SongLine.
     */
    public Tablature getCurrentTablature() {
        if (currentSong.getSongLines().isEmpty()) {
            return null; 
        }
        return currentSong.getSongLines().get(0).getTablature();
    }
    
    /**
     * Resets the content of all tablature lines in the current song.
     */
    public void resetTablature() {
        for (SongLine songLine : currentSong.getSongLines()) {
            if (songLine.getTablature() != null) {
                songLine.getTablature().getTablatureStrings().clear();
            }
        }
    }

    /**
     * Updates the current song instance.
     * * @param currentSong The new Song object to set as active.
     */
    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }
}