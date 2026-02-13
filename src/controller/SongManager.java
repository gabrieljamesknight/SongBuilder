package controller;
import java.io.*;

import javax.swing.JOptionPane;

import model.Song;
import model.SongLine;
import model.Tablature;

public class SongManager {
    private Song currentSong;
    private String directoryPath = System.getProperty("user.dir");


    public SongManager() {
        currentSong = new Song("New Song");
    }
 
    public void removeSongLine(int index) {
        currentSong.removeLine(index);
    }

    public void saveSongToFile(String filename) throws IOException {
        File file = new File(directoryPath + "/" + filename);
        if (file.exists()) {
            int response = JOptionPane.showConfirmDialog(null, 
                "Do you want to overwrite existing file?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(currentSong);
        out.close();
    }
    
    public void loadSongFromFile(String filename) throws IOException, ClassNotFoundException {
    	resetTablature();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(directoryPath + "/" + filename));
        currentSong = (Song) in.readObject();
        in.close();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public Tablature getCurrentTablature(){
        return currentSong.getSongLines().get(0).getTablature();
    }
    
    public void resetTablature() {
        for (SongLine songLine : currentSong.getSongLines()) {
            songLine.getTablature().getTablatureStrings().clear();
        }
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }
    
    
}
