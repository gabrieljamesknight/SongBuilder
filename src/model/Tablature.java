package model;
import java.io.Serializable;
import java.util.ArrayList;

public class Tablature implements Serializable {
    private ArrayList<GuitarString> guitarStrings;

    public Tablature() {
        guitarStrings = new ArrayList<>();
        // Increased from 77 to 95 to fill the new 780px wide panels
        int dashCount = 88;
        
        guitarStrings.add(new GuitarString("e ", "|" + "-".repeat(dashCount) + "|"));
        guitarStrings.add(new GuitarString("B ", "|" + "-".repeat(dashCount) + "|"));
        guitarStrings.add(new GuitarString("G ", "|" + "-".repeat(dashCount) + "|"));
        guitarStrings.add(new GuitarString("D ", "|" + "-".repeat(dashCount) + "|"));
        guitarStrings.add(new GuitarString("A ", "|" + "-".repeat(dashCount) + "|"));
        guitarStrings.add(new GuitarString("E ", "|" + "-".repeat(dashCount) + "|"));
    }

    public ArrayList<GuitarString> getTablatureStrings() {
        return guitarStrings;
    }

    public void setTablatureStrings(ArrayList<GuitarString> guitarStrings) {
        this.guitarStrings = guitarStrings;
    }

    public void addGuitarString(String tuning, String content) {
        guitarStrings.add(new GuitarString(tuning, content));
    }

    public GuitarString getGuitarString(int index) {
        return guitarStrings.get(index);
    }

    public void setGuitarStringContent(int index, StringBuilder content) {
        guitarStrings.get(index).setContent(content);
    }

    public StringBuilder getGuitarStringContent(int index) {
        return guitarStrings.get(index).getContent();
    }

    public void setGuitarStringTuning(int index, String tuning) {
        guitarStrings.get(index).setTuning(tuning);
    }
    
    public String getGuitarStringTuning(int index) {
        return guitarStrings.get(index).getTuning();
    }
    
    public static Tablature parseTablature(String tablatureText) {
        String[] lines = tablatureText.split("\n");
        Tablature tablature = new Tablature();
        tablature.getTablatureStrings().clear();
        for (String line : lines) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                String tuning = parts[0].trim();
                String content = parts[1];
                tablature.addGuitarString(tuning, content);
            }
        }
        return tablature;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < guitarStrings.size(); i++) {
            sb.append(guitarStrings.get(i).toString());
            if (i < guitarStrings.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}