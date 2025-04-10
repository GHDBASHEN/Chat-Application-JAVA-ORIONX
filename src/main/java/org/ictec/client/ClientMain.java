package org.ictec.client;

import org.ictec.gui.LoginFrame;
import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame(); // Launch the login GUI
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to initialize client: " + e.getMessage());
            }
        });
    }
}