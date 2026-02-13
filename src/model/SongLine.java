package model;

import java.io.Serializable;

public class SongLine implements Serializable {
    private String chords;
    private String lyrics;
    private Tablature tablature;

    public SongLine(String chords, String lyrics, Tablature tablature) {
        this.chords = chords;
        this.lyrics = lyrics;
        this.tablature = tablature;
    }

    public SongLine() {
        this.chords = "";
        this.lyrics = "";
        this.tablature = new Tablature();
    }

    public String getChords() {
        return chords;
    }

    public void setChords(String chords) {
        this.chords = chords;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public Tablature getTablature() {
        return tablature;
    }

    public void setTablature(Tablature tablature) {
        this.tablature = tablature;
    }
}