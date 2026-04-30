package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Song implements Serializable {
    private String name;
    private ArrayList<SongLine> songLines;
    
    private int capo = 0;
    private int tempo = 120;
    private String timeSignature = "4/4";
    private String scratchpadNotes = "";

    public Song(String name) {
        this.name = name;
        this.songLines = new ArrayList<>();
    }

    public int getCapo() { return capo; }
    public void setCapo(int capo) { this.capo = capo; }
    
    public int getTempo() { return tempo; }
    public void setTempo(int tempo) { this.tempo = tempo; }
    
    public String getTimeSignature() { return timeSignature; }
    public void setTimeSignature(String timeSignature) { this.timeSignature = timeSignature; }
    
    public String getScratchpadNotes() { return scratchpadNotes; }
    public void setScratchpadNotes(String scratchpadNotes) { this.scratchpadNotes = scratchpadNotes; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SongLine> getSongLines() {
        return songLines;
    }

    public void setSongLines(ArrayList<SongLine> songLines) {
        this.songLines = songLines;
    }

    public void addSongLine(SongLine songLine) {
        this.songLines.add(songLine);
    }

    public void removeLine(int index) {
        songLines.remove(index);
    }

}



