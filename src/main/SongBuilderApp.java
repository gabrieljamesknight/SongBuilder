package main;

import view.SongBuilderGUI;

public class SongBuilderApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new SongBuilderGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}