package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;

import model.Song;
import model.SongLine;
import view.components.SongBuilderMenuBar;
import view.components.SongGridArea;
import view.components.SongHeaderPanel;
import view.components.SongLinePanel;
import view.components.TuningPanel;

/**
 * The main graphical user interface for the SongBuilder application.
 *
 * Operates strictly as a View component within the MVC architecture.
 * Handles high-level layout orchestration and routing data to the Controller.
 */
public class SongBuilderGUI {
    
    private JFrame frame;
    private SongHeaderPanel headerPanel;
    private TuningPanel tuningPanel;
    private SongGridArea gridArea;
    
    private Runnable newSongAction = () -> {};
    private Runnable saveSongAction = () -> {};
    private Runnable saveSongAsAction = () -> {};
    private Runnable loadSongAction = () -> {};

    public SongBuilderGUI() {
        setupUI();
    }

    public void setNewSongAction(Runnable action) { 
        this.newSongAction = action;
        updateMenuBar(); 
    }
    
    public void setSaveSongAction(Runnable action) { 
        this.saveSongAction = action;
        updateMenuBar();
    }
    
    public void setSaveSongAsAction(Runnable action) { 
        this.saveSongAsAction = action;
        updateMenuBar(); 
    }
    
    public void setLoadSongAction(Runnable action) { 
        this.loadSongAction = action;
        updateMenuBar();
    }
    
    public void setRemoveLineCallback(Consumer<Integer> callback) { 
        gridArea.setRemoveLineCallback(callback);
    }

    private void setupUI() {
        frame = new JFrame("SongBuilder");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setPreferredSize(new Dimension(920, 800));

        headerPanel = new SongHeaderPanel(
            () -> gridArea.addLineAction(), 
            () -> saveSongAction.run(), 
            () -> loadSongAction.run()
        );
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.add(headerPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] defaultTunings = {"e", "B", "G", "D", "A", "E"};
        tuningPanel = new TuningPanel(defaultTunings, (index, tuning) -> gridArea.propagateTuningChange(index, tuning));
        frame.add(tuningPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));

        gridArea = new SongGridArea();
        frame.add(gridArea, BorderLayout.CENTER);

        updateMenuBar();
        setupGlobalFocusClearing();
        gridArea.addLineAction();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);
    }

    private void updateMenuBar() {
        SongBuilderMenuBar menuBar = new SongBuilderMenuBar(
            headerPanel.getTextFieldActionMap(),
            newSongAction,
            saveSongAction,
            saveSongAsAction,
            loadSongAction,
            () -> gridArea.addLineAction(),
            () -> gridArea.performActionOnTarget("COPY"),
            () -> gridArea.performActionOnTarget("PASTE"),
            () -> gridArea.performActionOnTarget("DUPLICATE"),
            () -> gridArea.performActionOnTarget("CLEAR"),
            () -> gridArea.performActionOnTarget("REMOVE")
        );
        frame.setJMenuBar(menuBar);
    }

    public void refreshUIFromModel(Song song) {
        headerPanel.setSongName(song.getName());
        
        if (!song.getSongLines().isEmpty()) {
            SongLine firstLine = song.getSongLines().get(0);
            for (int i = 0; i < 6; i++) {
                String tuning = firstLine.getTablature().getGuitarStringTuning(i).trim();
                tuningPanel.setTuningSilently(i, tuning);
            }
        }
        
        gridArea.loadSong(song);
    }

    public void resetGUI() {
        headerPanel.setSongName("");
        String[] defaultTunings = {"e", "B", "G", "D", "A", "E"};
        for (int i = 0; i < 6; i++) {
            tuningPanel.setTuningSilently(i, defaultTunings[i]);
        }
        gridArea.clearGrid();
    }

    private void setupGlobalFocusClearing() {
        MouseAdapter clearFocusAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                gridArea.setActivePanel(null);
            }
        };
        frame.addMouseListener(clearFocusAdapter);
        headerPanel.addMouseListener(clearFocusAdapter);
        tuningPanel.addMouseListener(clearFocusAdapter);
    }

    public JFrame getFrame() { return frame; }
    public String getSongName() { return headerPanel.getSongName(); }
    public List<SongLinePanel> getSongLinePanels() { return gridArea.getPanels(); }
    public String[] getTuningFieldsData() { return tuningPanel.getCurrentTunings(); }
}