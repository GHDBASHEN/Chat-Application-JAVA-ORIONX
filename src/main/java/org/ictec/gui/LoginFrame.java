package org.ictec.gui;
import org.ictec.entities.User;
import org.ictec.server.ChatService;
import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private ChatService chatService; // RMI Service reference

    public LoginFrame() {
        initializeRMI(); // Connect to RMI server
        setupUI();
    }

    private void initializeRMI() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            chatService = (ChatService) registry.lookup("ChatService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage());
            System.exit(1);
        }
    }

    private void setupUI() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 5, 5));

        // Components
        emailField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Add components
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        // Action Listeners
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> openRegistration());

        setVisible(true);
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and password cannot be empty!");
            return;
        }

        try {
            User user = chatService.login(email, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                dispose(); // Close login window
                new ChatClientGUI(user, chatService); // Open chat window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    private void openRegistration() {
        // Implement registration window here
        JOptionPane.showMessageDialog(this, "Redirect to registration...");
    }
}
