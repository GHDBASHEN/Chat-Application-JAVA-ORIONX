package org.example.client;

import org.example.domain.User;
import org.example.rmi.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterUI extends JFrame {

    private JTextField usernameField;
    private JTextField nicknameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton registerButton;
    private UserService userService;

    public RegisterUI(UserService userService) {
        this.userService = userService;
        initUI();
    }

    private void initUI() {
        setTitle("User Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        // Nickname
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        nicknameField = new JTextField(15);
        panel.add(nicknameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(15);
        panel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        roleBox = new JComboBox<>(new String[]{"user", "admin"});
        panel.add(roleBox, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Register");
        panel.add(registerButton, gbc);

        registerButton.addActionListener(this::handleRegister);

        add(panel);
        setVisible(true);
    }

    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setNickname(nickname);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole(role);

            boolean success = userService.registerUser(newUser);

            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "User already exists or registration failed.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during registration: " + ex.getMessage());
        }
    }
}
