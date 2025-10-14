package util;

import javax.swing.JOptionPane;

public class Message {

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    public static String getInput(String prompt) {
        return JOptionPane.showInputDialog(null, prompt);
    }
}
