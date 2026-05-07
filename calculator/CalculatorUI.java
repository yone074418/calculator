package calculator;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@FunctionalInterface
interface Command {
    void execute();
}

public class CalculatorUI extends JPanel {

    private final JTextArea display;
    private final StringBuilder expression = new StringBuilder();
    private boolean resultShown = false;

    // ---- Modern Dark Theme ----
    private static final Color BG_PANEL    = new Color(28, 28, 30);
    private static final Color BG_DISPLAY  = new Color(0, 0, 0);
    private static final Color FG_DISPLAY  = new Color(255, 255, 255);
    private static final Color SEP_LINE    = new Color(56, 56, 58);

    private static final Color NUM_BG      = new Color(51, 51, 54);
    private static final Color NUM_HOVER   = new Color(66, 66, 69);

    private static final Color OP_BG       = new Color(255, 159, 10);
    private static final Color OP_HOVER    = new Color(255, 179, 64);

    private static final Color SPC_BG      = new Color(58, 58, 60);
    private static final Color SPC_HOVER   = new Color(72, 72, 74);

    private static final Color CLEAR_BG    = new Color(180, 60, 50);
    private static final Color CLEAR_HOVER = new Color(200, 80, 70);

    private static final Font DISPLAY_FONT = new Font("Segoe UI", Font.PLAIN, 28);
    private static final Font BTN_FONT     = new Font("Segoe UI", Font.BOLD, 20);

    public CalculatorUI() {
        setLayout(new GridBagLayout());
        setBackground(BG_PANEL);

        // ---- Display ----
        display = new JTextArea(2, 14);
        display.setEditable(false);
        display.setBackground(BG_DISPLAY);
        display.setForeground(FG_DISPLAY);
        display.setFont(DISPLAY_FONT);
        display.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SEP_LINE),
            BorderFactory.createEmptyBorder(18, 14, 18, 14)
        ));
        display.setLineWrap(true);
        display.setWrapStyleWord(true);

        GridBagConstraints dc = new GridBagConstraints();
        dc.gridx = 0; dc.gridy = 0;
        dc.gridwidth = 4;
        dc.weightx = 1.0; dc.weighty = 0.3;
        dc.fill = GridBagConstraints.BOTH;
        add(display, dc);

        addButtons();
    }

    private void addButtons() {
        // Row 1: (  )  %  C
        addBtn("(", 0, 1, 1, SPC_BG, SPC_HOVER, () -> append("("));
        addBtn(")", 1, 1, 1, SPC_BG, SPC_HOVER, () -> append(")"));
        addBtn("%", 2, 1, 1, SPC_BG, SPC_HOVER, () -> append("%"));
        addBtn("C", 3, 1, 1, CLEAR_BG, CLEAR_HOVER, this::clear);

        // Row 2: 7  8  9  /
        addBtn("7", 0, 2, 1, NUM_BG, NUM_HOVER, () -> append("7"));
        addBtn("8", 1, 2, 1, NUM_BG, NUM_HOVER, () -> append("8"));
        addBtn("9", 2, 2, 1, NUM_BG, NUM_HOVER, () -> append("9"));
        addBtn("/", 3, 2, 1, OP_BG, OP_HOVER, () -> append("/"));

        // Row 3: 4  5  6  *
        addBtn("4", 0, 3, 1, NUM_BG, NUM_HOVER, () -> append("4"));
        addBtn("5", 1, 3, 1, NUM_BG, NUM_HOVER, () -> append("5"));
        addBtn("6", 2, 3, 1, NUM_BG, NUM_HOVER, () -> append("6"));
        addBtn("*", 3, 3, 1, OP_BG, OP_HOVER, () -> append("*"));

        // Row 4: 1  2  3  -
        addBtn("1", 0, 4, 1, NUM_BG, NUM_HOVER, () -> append("1"));
        addBtn("2", 1, 4, 1, NUM_BG, NUM_HOVER, () -> append("2"));
        addBtn("3", 2, 4, 1, NUM_BG, NUM_HOVER, () -> append("3"));
        addBtn("-", 3, 4, 1, OP_BG, OP_HOVER, () -> append("-"));

        // Row 5: 0(span2)  .  +
        addBtn("0", 0, 5, 2, NUM_BG, NUM_HOVER, () -> append("0"));
        addBtn(".", 2, 5, 1, NUM_BG, NUM_HOVER, () -> append("."));
        addBtn("+", 3, 5, 1, OP_BG, OP_HOVER, () -> append("+"));

        // Row 6: ⌫(span2)  =(span2)
        addBtn("⌫", 0, 6, 2, SPC_BG, SPC_HOVER, this::backspace);
        addBtn("=", 2, 6, 2, OP_BG, OP_HOVER, this::evaluate);
    }

    private void addBtn(String label, int x, int y, int w,
                        Color bg, Color hoverBg, Command cmd) {
        JButton btn = new JButton(label);
        btn.setFont(BTN_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        btn.addActionListener(e -> cmd.execute());

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = x; bc.gridy = y;
        bc.gridwidth = w; bc.gridheight = 1;
        bc.weightx = 1.0; bc.weighty = 1.0;
        bc.fill = GridBagConstraints.BOTH;
        bc.insets = new Insets(1, 1, 1, 1);
        add(btn, bc);
    }

    // =========================================================
    //  Command implementations
    // =========================================================

    private void append(String s) {
        if (resultShown) {
            expression.setLength(0);
            resultShown = false;
        }
        expression.append(s);
        updateDisplay();
    }

    private void clear() {
        expression.setLength(0);
        resultShown = false;
        updateDisplay();
    }

    private void backspace() {
        if (resultShown) {
            clear();
            return;
        }
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
        }
        updateDisplay();
    }

    private void evaluate() {
        if (expression.length() == 0) return;
        try {
            ExpressionParser parser = new ExpressionParser();
            double result = parser.evaluate(expression.toString());
            String line = expression + "\n= " + formatResult(result);
            display.setText(line);
            expression.setLength(0);
            expression.append(formatResult(result));
            resultShown = true;
        } catch (ArithmeticException e) {
            display.setText(expression + "\nError: division by zero");
            resultShown = true;
        } catch (Exception e) {
            display.setText(expression + "\nError");
            resultShown = true;
        }
    }

    private void updateDisplay() {
        display.setText(expression.toString());
    }

    private static String formatResult(double result) {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return "Error";
        }
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.format("%.0f", result);
        }
        return String.format("%.10f", result)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }
}
