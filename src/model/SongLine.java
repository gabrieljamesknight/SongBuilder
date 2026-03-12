package model;

import java.io.Serializable;

public class SongLine implements Serializable {
    private String chords;
    private String lyrics;
    private Tablature tablature;
    private String sectionLabel;

    public SongLine(String chords, String lyrics, Tablature tablature) {
        this.chords = chords;
        this.lyrics = lyrics;
        this.tablature = tablature;
        this.sectionLabel = sectionLabel;
    }

    public SongLine() {
        this.chords = "";
        this.lyrics = "";
        this.tablature = new Tablature();
        this.sectionLabel = "";
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

    public String getSectionLabel() {
        return sectionLabel;
    }

    public void setSectionLabel(String sectionLabel) {
        this.sectionLabel = sectionLabel;
    }
}