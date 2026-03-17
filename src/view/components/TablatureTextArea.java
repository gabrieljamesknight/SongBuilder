package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import model.Tablature;

/**
 * A specialized text area for rendering and interacting with guitar tablature.
 * Handles its own custom anti-aliased background painting and focus ring borders.
 */
public class TablatureTextArea extends JTextArea {

    private final Border unfocusedBorder;
    private final Border focusedBorder;

    public TablatureTextArea() {
        super();
        setOpaque(false);
        setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        Color borderColor = UIManager.getColor("Component.borderColor");
        Color focusColor = UIManager.getColor("Component.focusColor");

        this.unfocusedBorder = createUnfocusedBorder(borderColor);
        this.focusedBorder = createFocusedBorder(focusColor);

        setBorder(unfocusedBorder);
        setupFocusListeners();
        setupKeyBindings();

        setText(new Tablature().toString());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2d.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void processKeyEvent(KeyEvent ke) {
        if ((ke.getKeyCode() == KeyEvent.VK_C && ke.isControlDown()) || 
            (ke.getKeyCode() == KeyEvent.VK_V && ke.isControlDown())) {
            ke.consume();
        } else {
            super.processKeyEvent(ke);
        }
    }

    private void setupFocusListeners() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(focusedBorder);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(unfocusedBorder);
            }
        });
    }

    private void setupKeyBindings() {
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), DefaultEditorKit.forwardAction);
        getInputMap().put(KeyStroke.getKeyStroke(' '), DefaultEditorKit.forwardAction);
    }

    private Border createUnfocusedBorder(Color borderColor) {
        return new EmptyBorder(5, 8, 5, 8) {
            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(borderColor != null ? borderColor : Color.GRAY);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
                g2d.dispose();
            }
        };
    }

    private Border createFocusedBorder(Color focusColor) {
        return new EmptyBorder(5, 8, 5, 8) {
            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(focusColor != null ? focusColor : new Color(62, 134, 224));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 12, 12);
                g2d.dispose();
            }
        };
    }
}