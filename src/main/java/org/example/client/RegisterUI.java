package org.example.client;

import org.example.domain.User;
import org.example.rmi.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class RegisterUI extends JFrame {

    private JTextField usernameField;
    private JTextField nicknameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private UserService userService;

    public RegisterUI(UserService userService) {
        this.userService = userService;
        initUI();
    }

    private void initUI() {
        setTitle("Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(245, 245, 245)); // Soft gray background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Title
        JLabel titleLabel = new JLabel("Register", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(33, 37, 41));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 30, 10);
        mainPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;

        // Username
        addLabeledField("Username:", usernameField = new JTextField(18), labelFont, fieldFont, gbc, mainPanel, 1);

        // Nickname
        addLabeledField("Nickname:", nicknameField = new JTextField(18), labelFont, fieldFont, gbc, mainPanel, 2);

        // Email
        addLabeledField("Email:", emailField = new JTextField(18), labelFont, fieldFont, gbc, mainPanel, 3);

        // Password
        addLabeledField("Password:", passwordField = new JPasswordField(18), labelFont, fieldFont, gbc, mainPanel, 4);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerButton.setBackground(new Color(72, 133, 237));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(72, 133, 237)),
                BorderFactory.createEmptyBorder(12, 40, 12, 40)
        ));

        registerButton.addActionListener(this::handleRegister);

        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(52, 103, 217)); // Darker blue hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(72, 133, 237)); // Normal blue
            }
        });

        mainPanel.add(registerButton, gbc);

        add(mainPanel);
        setVisible(true);
    }

    private void addLabeledField(String labelText, JTextField field, Font labelFont, Font fieldFont,
                                 GridBagConstraints gbc, JPanel panel, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        field.setFont(fieldFont);
        field.setPreferredSize(new Dimension(250, 30));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(field, gbc);
    }

    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setNickname(nickname);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setRole("USER");

            boolean success = userService.registerUser(newUser);

            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "User already exists or registration failed.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }
}
