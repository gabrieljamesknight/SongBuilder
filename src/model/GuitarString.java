package model;

import java.io.Serializable;

public class GuitarString implements Serializable {
    private String tuning;
    private StringBuilder content;

    public GuitarString(String tuning, String content) {
        this.tuning = tuning;
        this.content = new StringBuilder(content);
    }
    public String getTuning() {
        return tuning;
    }

    public void setTuning(String tuning) {
        this.tuning = tuning;
    }

    public StringBuilder getContent() {
        return content;
    }

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return tuning + "|" + content;
    }
}
