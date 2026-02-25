package view.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.BiConsumer;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Represents the global guitar tuning control panel for the application.
 * Encapsulates the layout, validation, and event listening for the 6 guitar string inputs.
 */
public class TuningPanel extends JPanel {

    private final JTextField[] tuningFields;
    private final BiConsumer<Integer, String> onTuningChanged;

    /**
     * Constructs the TuningPanel.
     * * @param initialTunings An array of 6 strings representing the initial tuning (e.g., ["E", "A", "D", "G", "B", "e"]).
     * @param onTuningChanged Callback invoked when a tuning value is modified. Passes the string index and the new tuning string.
     */
    public TuningPanel(String[] initialTunings, BiConsumer<Integer, String> onTuningChanged) {
        this.onTuningChanged = onTuningChanged;
        this.tuningFields = new JTextField[6];
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setupUI(initialTunings);
    }

    private void setupUI(String[] initialTunings) {
        JLabel tuningLabel = new JLabel("Tuning:");
        tuningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(tuningLabel);

        JPanel fieldsContainer = new JPanel(new FlowLayout());
        Dimension containerDim = new Dimension(400, 45);
        fieldsContainer.setPreferredSize(containerDim);
        fieldsContainer.setMaximumSize(containerDim);

        for (int i = 5; i >= 0; i--) {
            JTextField tuningField = new JTextField(initialTunings[i], 2);
            Dimension fieldDim = new Dimension(35, 30);
            tuningField.setPreferredSize(fieldDim);
            tuningField.setHorizontalAlignment(JTextField.CENTER);
            
            ((AbstractDocument) tuningField.getDocument()).setDocumentFilter(new TuningLengthFilter(2));
            
            attachListener(tuningField, i);
            tuningFields[i] = tuningField;
            fieldsContainer.add(tuningField);
        }
        
        add(fieldsContainer);
    }

    private void attachListener(JTextField tuningField, int stringIndex) {
        tuningField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            
            private void update() {
                SwingUtilities.invokeLater(() -> {
                    if (onTuningChanged != null) {
                        onTuningChanged.accept(stringIndex, tuningField.getText());
                    }
                });
            }
        });
    }

    /**
     * Updates the text of a specific tuning field without triggering infinite update loops.
     * * @param stringIndex The zero-based index of the guitar string.
     * @param tuning The new tuning character(s).
     */
    public void setTuningSilently(int stringIndex, String tuning) {
        if (stringIndex >= 0 && stringIndex < tuningFields.length) {
            tuningFields[stringIndex].setText(tuning);
        }
    }

    /**
     * Retrieves the current tunings from the panel.
     * * @return An array of strings representing the current tunings.
     */
    public String[] getCurrentTunings() {
        String[] tunings = new String[6];
        for (int i = 0; i < 6; i++) {
            tunings[i] = tuningFields[i].getText();
        }
        return tunings;
    }

    // Inner class for filtering text length, localized to this component
    private static class TuningLengthFilter extends DocumentFilter {
        private final int max;
        TuningLengthFilter(int max) { this.max = max; }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (fb.getDocument().getLength() + string.length() <= this.max) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (fb.getDocument().getLength() + text.length() - length <= this.max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}