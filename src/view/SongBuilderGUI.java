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
import javax.swing.JPanel;

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
    private view.components.SongNavigatorSidebar leftSidebar;
    private view.components.SongMetadataSidebar rightSidebar;
    
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
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(920, 800));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        headerPanel = new SongHeaderPanel(
            () -> gridArea.addLineAction(), 
            () -> saveSongAction.run(), 
            () -> loadSongAction.run()
        );
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        topPanel.add(headerPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String[] defaultTunings = {"e", "B", "G", "D", "A", "E"};
        tuningPanel = new TuningPanel(defaultTunings, (index, tuning) -> gridArea.propagateTuningChange(index, tuning));
        topPanel.add(tuningPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        frame.add(topPanel, BorderLayout.NORTH);

        gridArea = new SongGridArea();
        leftSidebar = new view.components.SongNavigatorSidebar();
        rightSidebar = new view.components.SongMetadataSidebar();
        
        gridArea.setSectionsChangedCallback(() -> {
            leftSidebar.updateSections(gridArea.getPanels(), panel -> {
                gridArea.scrollToPanel(panel);
            });
        });
        
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(leftSidebar, BorderLayout.WEST);
        mainContentPanel.add(gridArea, BorderLayout.CENTER);
        mainContentPanel.add(rightSidebar, BorderLayout.EAST);
        
        frame.add(mainContentPanel, BorderLayout.CENTER);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                boolean showSidebars = frame.getWidth() >= 1200;
                if (leftSidebar.isVisible() != showSidebars) {
                    leftSidebar.setVisible(showSidebars);
                    rightSidebar.setVisible(showSidebars);
                    frame.revalidate();
                }
            }
        });
        
        leftSidebar.setVisible(false);
        rightSidebar.setVisible(false);

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
        setCapo(song.getCapo());
        setTempo(song.getTempo());
        setTimeSignature(song.getTimeSignature());
        setScratchpadNotes(song.getScratchpadNotes());
        
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
        setCapo(0);
        setTempo(120);
        setTimeSignature("4/4");
        setScratchpadNotes("");
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
    
    public int getCapo() { return rightSidebar.getCapo(); }
    public void setCapo(int capo) { rightSidebar.setCapo(capo); }
    public int getTempo() { return rightSidebar.getTempo(); }
    public void setTempo(int tempo) { rightSidebar.setTempo(tempo); }
    public String getTimeSignature() { return rightSidebar.getTimeSignature(); }
    public void setTimeSignature(String ts) { rightSidebar.setTimeSignature(ts); }
    public String getScratchpadNotes() { return rightSidebar.getScratchpadNotes(); }
    public void setScratchpadNotes(String notes) { rightSidebar.setScratchpadNotes(notes); }
}