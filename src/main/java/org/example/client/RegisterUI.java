package org.example.client;

import org.example.domain.User;
import org.example.rmi.ChatService;
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
    private ChatService chatService;

    public RegisterUI(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
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
        mainPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 26);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel titleLabel = new JLabel("Register user", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(33, 37, 41));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 30, 10);
        mainPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;

        addLabeledField("Username:", usernameField = new RoundedTextField(18), labelFont, fieldFont, gbc, mainPanel, 1);
        addLabeledField("Nickname:", nicknameField = new RoundedTextField(18), labelFont, fieldFont, gbc, mainPanel, 2);
        addLabeledField("Email:", emailField = new RoundedTextField(18), labelFont, fieldFont, gbc, mainPanel, 3);
        addLabeledField("Password:", passwordField = new RoundedPasswordField(18), labelFont, fieldFont, gbc, mainPanel, 4);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        // Register Button
        registerButton = new RoundedButton("Register");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(200, 40));
        registerButton.setMaximumSize(new Dimension(480, 40));
        registerButton.addActionListener(this::handleRegister);

        // Cancel Button
        RoundedButton cancelButton = new RoundedButton("Cancel");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setPreferredSize(new Dimension(300, 40));
        cancelButton.setMaximumSize(new Dimension(480, 40));
        cancelButton.setNormalColor(new Color(120, 120, 120));
        cancelButton.setRolloverColor(new Color(90, 90, 90));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, gbc);

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
        field.setPreferredSize(new Dimension(250, 40));
        field.setBackground(Color.WHITE);
        panel.add(field, gbc);
    }

    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        try {
            User newUser = createUser(username, nickname, email, password);
            User registered = userService.registerUser(newUser);

            if (registered != null && registered.getUser_id() > 0) {
                handleSuccessfulRegistration(registered);
            } else {
                showError("Registration failed.");
            }
        } catch (Exception ex) {
            handleRegistrationError(ex);
        }
    }

    private User createUser(String username, String nickname, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        return user;
    }

    private void handleSuccessfulRegistration(User registered) throws Exception {
        chatService.addUserToGroup(registered.getUser_id(), 1);
        JOptionPane.showMessageDialog(this,
                "Registration successful! Your ID is " + registered.getUser_id());
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void handleRegistrationError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error during registration: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    // Custom UI Components
    class RoundedTextField extends JTextField {
        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.dispose();
        }
    }

    class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField(int columns) {
            super(columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        private Color normalColor = new Color(72, 133, 237);
        private Color rolloverColor = new Color(52, 103, 217);

        public RoundedButton(String text) {
            super(text);
            setupButton();
        }

        private void setupButton() {
            setFocusPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void setNormalColor(Color color) {
            this.normalColor = color;
        }

        public void setRolloverColor(Color color) {
            this.rolloverColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? rolloverColor : normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(normalColor);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            g2.dispose();
        }
    }
}