package view;

import controller.SongManager;
import model.Song;
import model.SongLine;
import model.Tablature;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SongBuilderGUI {
    private SongManager songManager;
    private JFrame frame;
    private JTextField songNameField;
    private JButton addLineButton, saveSongButton, loadSongButton;
    private ArrayList<SongLinePanel> songLinePanels;
    private JTextField[] tuningFields;


    public SongBuilderGUI() {
        songManager = new SongManager();
        songLinePanels = new ArrayList<>();
        songNameField = new JTextField(20);
        tuningFields = new JTextField[6];
        Tablature tablature = new Tablature();
        for (int i = 0; i < 6; i++) {
            String tuning = Character.toString(tablature.getGuitarStringTuning(i).charAt(0));
            tuningFields[i] = new JTextField(tuning, 2);
        }
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("SongBuilder");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setPreferredSize(new Dimension(800, 600));

        JMenuBar menuBar = new JMenuBar();

        ActionMap actionMap = songNameField.getActionMap();
        Action cutAction = actionMap.get(DefaultEditorKit.cutAction);
        Action copyAction = actionMap.get(DefaultEditorKit.copyAction);
        Action pasteAction = actionMap.get(DefaultEditorKit.pasteAction);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem newSongMenuItem = new JMenuItem("New File");
        fileMenu.add(newSongMenuItem);
        JMenuItem saveSongMenuItem = new JMenuItem("Save");
        fileMenu.add(saveSongMenuItem);
        JMenuItem loadSongMenuItem = new JMenuItem("Load...");
        fileMenu.add(loadSongMenuItem);
        JMenuItem exitSongMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitSongMenuItem);

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenuItem addLineMenuItem = new JMenuItem("Add Line...");
        editMenu.add(addLineMenuItem);
        JMenuItem removeLineMenuItem = new JMenuItem("Remove Line...");
        editMenu.add(removeLineMenuItem);
        JMenuItem cutMenuItem = new JMenuItem(cutAction);
        cutMenuItem.setText("Cut");
        JMenuItem copyMenuItem = new JMenuItem(copyAction);
        copyMenuItem.setText("Copy");
        JMenuItem pasteMenuItem = new JMenuItem(pasteAction);
        pasteMenuItem.setText("Paste");
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        frame.setJMenuBar(menuBar);

        Font font = new Font("Arial", Font.PLAIN, 16);

        addLineButton = new JButton("Add Line");
        addLineButton.setFont(font);
        saveSongButton = new JButton("Save Song");
        saveSongButton.setFont(font);
        loadSongButton = new JButton("Load Song");
        loadSongButton.setFont(font);

        Dimension SongNameFieldDim = new Dimension(400, 20);
        songNameField.setPreferredSize(SongNameFieldDim);
        songNameField.setMaximumSize(SongNameFieldDim);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(addLineButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveSongButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(loadSongButton);

        

    


        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel songNameLabel = new JLabel("Song Name:");
        songNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(songNameLabel);
        frame.add(songNameField);
        songNameField.setHorizontalAlignment(JTextField.CENTER);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.add(buttonPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel tuningLabel = new JLabel("Tuning:");
        tuningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(tuningLabel); 
        JPanel tuningFieldsPanel = new JPanel(new FlowLayout());
        frame.add(tuningFieldsPanel);
        Dimension tuningFieldPanelDim = new Dimension(250, 30);
        tuningFieldsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 15, 0),
            tuningFieldsPanel.getBorder()
        ));
        tuningFieldsPanel.setBorder(BorderFactory.createMatteBorder(2,2 , 2, 2, Color.BLACK));
        tuningFieldsPanel.setPreferredSize(tuningFieldPanelDim);
        tuningFieldsPanel.setMaximumSize(tuningFieldPanelDim);
        frame.add(Box.createRigidArea(new Dimension(0, 20)));

        for (int i = tuningFields.length - 1; i >= 0; i--) {
            JTextField tuningField = tuningFields[i];
            Dimension tuningFieldsDim = new Dimension(50, 20);
            tuningField.setPreferredSize(tuningFieldsDim);
            tuningField.setMaximumSize(tuningFieldsDim);
            tuningFieldsPanel.add(tuningField);
        }

        for (int i = tuningFields.length - 1; i >= 0; i--) {
            JTextField tuningField = tuningFields[i];
            ((AbstractDocument)tuningField.getDocument()).setDocumentFilter(new LengthFilter(2));     
        }      
        
        
        JPanel songLinePanelContainer = new JPanel();
        songLinePanelContainer.setLayout(new BoxLayout(songLinePanelContainer, BoxLayout.Y_AXIS));
        songLinePanelContainer.setBorder(new EmptyBorder(5, 5, 45, 5));

        JScrollPane scrollPane = new JScrollPane(songLinePanelContainer);     
        frame.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        SongLinePanel initialPanel = new SongLinePanel();
        songLinePanels.add(initialPanel);
        songLinePanelContainer.add(initialPanel);


        newSongMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                songLinePanels.clear();
                songLinePanelContainer.removeAll();

                songNameField.setText("");
                SongLinePanel initialPanel = new SongLinePanel();
                songLinePanels.add(initialPanel);
                songLinePanelContainer.add(initialPanel);

                frame.repaint();
                frame.pack();
            }
        });
        

        removeLineMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(frame, "Enter the number of the line to remove:");
                if (input != null && !input.isEmpty()) {
                    try {
                        int index = Integer.parseInt(input);
                        songManager.removeSongLine(index-1);
                        SongLinePanel panelToRemove = songLinePanels.remove(index-1);
                        songLinePanelContainer.remove(panelToRemove);
                        frame.revalidate();
                        frame.repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid index. Please enter a number.");
                    }
                }
            }
        });

        exitSongMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        ActionListener addLine = e -> {
            SongLinePanel newPanel = new SongLinePanel();
            songLinePanels.add(newPanel);
            frame.add(newPanel);
            songLinePanelContainer.add(Box.createVerticalStrut(40));
            songLinePanelContainer.add(newPanel);
            frame.pack();
        };
        
        ActionListener loadSong = e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    songManager.loadSongFromFile(selectedFile.getName());
                    Song song = songManager.getCurrentSong();
                    songNameField.setText(song.getName());
                    SongLine firstSongLine = song.getSongLines().get(0);
            
                    StringBuilder allTunings = new StringBuilder();
                    for (int i = 0; i < 6; i++) {
                        String tuning = firstSongLine.getTablature().getGuitarStringTuning(i);
                        allTunings.append(tuning);
                    }
                    for (int i = 0; i < 6; i++) {
                        String tuning = firstSongLine.getTablature().getGuitarStringTuning(i).trim();
                        tuningFields[i].setText(tuning);
                    }
    
                    for (JPanel panel : songLinePanels) {
                        frame.remove(panel);
                    }
                    songLinePanels.clear();
                    songLinePanelContainer.removeAll();
    
                    for (SongLine songLine : song.getSongLines()) {
                        SongLinePanel newPanel = new SongLinePanel();
                        newPanel.getChordsField().setText(songLine.getChords());
                        newPanel.getLyricsField().setText(songLine.getLyrics());
                        newPanel.getTablatureArea().setText(songLine.getTablature().toString());
                        frame.add(scrollPane, BorderLayout.CENTER);
                        songLinePanelContainer.add(newPanel);
                        songLinePanelContainer.add(Box.createVerticalStrut(50));
                        songLinePanels.add(newPanel);
                    }
                    frame.pack();
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };

        ActionListener saveSong = e -> {
            String[] tunings = new String[6];
            for (int i = 0; i < 6; i++) {
                tunings[i] = tuningFields[i].getText();
            }
    
            songManager.getCurrentSong().getSongLines().clear();
    
            for (SongLinePanel panel : songLinePanels) {
                SongLinePanel songLinePanel = (SongLinePanel) panel;
                String chords = songLinePanel.getChords();
                String lyrics = songLinePanel.getLyrics();
                Tablature tablature = songLinePanel.getTablature();

    
                songManager.getCurrentSong().addSongLine(new SongLine(chords, lyrics, tablature));
            }
    
    
            for (SongLine songLine : songManager.getCurrentSong().getSongLines()) {
                Tablature tablature = songLine.getTablature();
                for (int i = 0; i < 6; i++) {
                    String tuning = tunings[i];
                    if (tuning.length() < 2) {
                        tuning = String.format("%-2s", tuning);
                    }
                    tablature.setGuitarStringTuning(i, tuning);
                }
            }
    
            for (int i = 0; i < songLinePanels.size(); i++) {
                SongLinePanel panel = songLinePanels.get(i);
                String tab = songManager.getCurrentSong().getSongLines().get(i).getTablature().toString();
                panel.getTablatureArea().setText(tab);
            }
            
            String filename = songNameField.getText() + ".ser";
            songManager.getCurrentSong().setName(songNameField.getText());
            try {
                songManager.saveSongToFile(filename);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };

        
        loadSongButton.addActionListener(loadSong);
        loadSongMenuItem.addActionListener(loadSong);
        saveSongButton.addActionListener(saveSong);
        saveSongMenuItem.addActionListener(saveSong);
        addLineButton.addActionListener(addLine);
        addLineMenuItem.addActionListener(addLine);




        

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

}


class LengthFilter extends DocumentFilter {
    private int max;

    LengthFilter(int max) {
        this.max = max;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + string.length() <= this.max) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + text.length() - length <= this.max) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}