package com.ytbot;

import javax.swing.*;

public class Error {
    public static void showError(String infoMessage) {
        JOptionPane.showMessageDialog(null, infoMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
