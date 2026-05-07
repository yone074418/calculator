package calculator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Calculator {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Calculator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new CalculatorUI());
            frame.pack();
            frame.setSize(340, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
