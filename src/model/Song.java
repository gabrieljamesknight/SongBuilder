package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Song implements Serializable {
    private String name;
    private ArrayList<SongLine> songLines;

    public Song(String name) {
        this.name = name;
        this.songLines = new ArrayList<>();
    }

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



