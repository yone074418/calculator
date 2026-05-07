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

    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color DISPLAY_BG = new Color(18, 18, 18);
    private static final Color DISPLAY_TEXT = new Color(220, 220, 220);
    private static final Color BTN_NUM = new Color(58, 58, 60);
    private static final Color BTN_OP = new Color(255, 149, 0);
    private static final Color BTN_SPECIAL = new Color(80, 80, 82);
    private static final Color BTN_TEXT = new Color(255, 255, 255);
    private static final Color BTN_TEXT_OP = Color.WHITE;

    public CalculatorUI() {
        setLayout(new GridBagLayout());
        setBackground(BG_COLOR);

        display = new JTextArea(3, 20);
        display.setEditable(false);
        display.setBackground(DISPLAY_BG);
        display.setForeground(DISPLAY_TEXT);
        display.setFont(new Font("Menlo", Font.PLAIN, 26));
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        display.setLineWrap(true);
        display.setWrapStyleWord(true);

        GridBagConstraints dc = new GridBagConstraints();
        dc.gridx = 0;
        dc.gridy = 0;
        dc.gridwidth = 4;
        dc.weightx = 1.0;
        dc.weighty = 0.35;
        dc.fill = GridBagConstraints.BOTH;
        dc.insets = new Insets(0, 0, 2, 0);
        add(display, dc);

        addButtons();
    }

    private void addButtons() {
        // Row 1: parens, percent, clear
        addBtn("(", 0, 1, 1, 1, BTN_SPECIAL, () -> append("("));
        addBtn(")", 1, 1, 1, 1, BTN_SPECIAL, () -> append(")"));
        addBtn("%", 2, 1, 1, 1, BTN_SPECIAL, () -> append("%"));
        addBtn("C", 3, 1, 1, 1, BTN_SPECIAL, this::clear);

        // Row 2: 7 8 9 /
        addBtn("7", 0, 2, 1, 1, BTN_NUM, () -> append("7"));
        addBtn("8", 1, 2, 1, 1, BTN_NUM, () -> append("8"));
        addBtn("9", 2, 2, 1, 1, BTN_NUM, () -> append("9"));
        addBtn("/", 3, 2, 1, 1, BTN_OP, () -> append("/"));

        // Row 3: 4 5 6 *
        addBtn("4", 0, 3, 1, 1, BTN_NUM, () -> append("4"));
        addBtn("5", 1, 3, 1, 1, BTN_NUM, () -> append("5"));
        addBtn("6", 2, 3, 1, 1, BTN_NUM, () -> append("6"));
        addBtn("*", 3, 3, 1, 1, BTN_OP, () -> append("*"));

        // Row 4: 1 2 3 -
        addBtn("1", 0, 4, 1, 1, BTN_NUM, () -> append("1"));
        addBtn("2", 1, 4, 1, 1, BTN_NUM, () -> append("2"));
        addBtn("3", 2, 4, 1, 1, BTN_NUM, () -> append("3"));
        addBtn("-", 3, 4, 1, 1, BTN_OP, () -> append("-"));

        // Row 5: 0(span2)  .  ±
        addBtn("0", 0, 5, 2, 1, BTN_NUM, () -> append("0"));
        addBtn(".", 2, 5, 1, 1, BTN_NUM, () -> append("."));
        addBtn("±", 3, 5, 1, 1, BTN_SPECIAL, this::toggleSign);

        // Row 6: ⌫(span2)  =(span2)
        addBtn("⌫", 0, 6, 2, 1, BTN_SPECIAL, this::backspace);
        addBtn("=", 2, 6, 2, 1, BTN_OP, this::evaluate);
    }

    private void addBtn(String label, int x, int y, int w, int h,
                        Color bg, Command cmd) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setForeground(bg == BTN_OP ? BTN_TEXT_OP : BTN_TEXT);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BG_COLOR, 1));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        // Command pattern: encapsulate action in Command object
        btn.addActionListener(e -> cmd.execute());

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = x;
        bc.gridy = y;
        bc.gridwidth = w;
        bc.gridheight = h;
        bc.weightx = 1.0;
        bc.weighty = 1.0;
        bc.fill = GridBagConstraints.BOTH;
        bc.insets = new Insets(2, 2, 2, 2);
        add(btn, bc);
    }

    // ---- Command implementations ----

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

    private void toggleSign() {
        if (resultShown) {
            clear();
        }
        if (expression.length() == 0) {
            expression.append('-');
            updateDisplay();
            return;
        }

        int i = expression.length() - 1;

        while (i >= 0 && (Character.isDigit(expression.charAt(i))
                || expression.charAt(i) == '.')) {
            i--;
        }

        if (i >= 0 && expression.charAt(i) == '-') {
            boolean isUnary = i == 0
                    || isOpOrParen(expression.charAt(i - 1));
            if (isUnary) {
                expression.deleteCharAt(i);
                updateDisplay();
                return;
            }
        }

        expression.insert(i + 1, '-');
        updateDisplay();
    }

    private void evaluate() {
        if (expression.length() == 0) {
            return;
        }
        try {
            ExpressionParser parser = new ExpressionParser();
            double result = parser.evaluate(expression.toString());
            String line = expression + "\n= " + formatResult(result);
            display.setText(line);
            expression.setLength(0);
            expression.append(formatResult(result));
            resultShown = true;
        } catch (ArithmeticException e) {
            display.setText(expression + "\nError: Division by zero");
            resultShown = true;
        } catch (Exception e) {
            display.setText(expression + "\nError");
            resultShown = true;
        }
    }

    private void updateDisplay() {
        String text = expression.toString();
        if (text.isEmpty()) {
            display.setText("");
        } else {
            display.setText(text);
        }
    }

    private static boolean isOpOrParen(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(';
    }

    private static String formatResult(double result) {
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return "Error";
        }
        if (result == Math.floor(result) && !Double.isInfinite(result)) {
            return String.format("%.0f", result);
        }
        String s = String.format("%.10f", result)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
        return s;
    }
}
