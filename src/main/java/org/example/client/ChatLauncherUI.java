package org.example.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.List;

import org.example.client.admin.AdminDashboardUI;
import org.example.client.user.userDashBoard;
import org.example.domain.ChatLog;
import org.example.domain.User;
import org.example.rmi.*;
import org.example.server.impl.ChatLogServiceImpl;
import org.hibernate.SessionFactory;

public class ChatLauncherUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private static JButton registerButton;
    private ChatService chatService;
    private static UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;

    public ChatLauncherUI() {
        initUI();
        setupServices();
    }

    private void initUI() {
        setTitle("Multi-User Chat Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(169, 188, 188);
                Color color2 = new Color(181, 211, 211);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Chat Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        emailField = new RoundedTextField(20);
        passwordField = new RoundedPasswordField(20);

        loginButton = new RoundedButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));

        registerButton = new RoundedButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));

        loginButton.addActionListener(this::handleLogin);
        registerButton.addActionListener(e -> new RegisterUI(userService, chatService));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        loginPanel.add(createLabel("Email:"), gbc);
        gbc.gridx++;
        loginPanel.add(emailField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        loginPanel.add(createLabel("Password:"), gbc);
        gbc.gridx++;
        loginPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        gbc.gridy++;
        loginPanel.add(registerButton, gbc);

        mainPanel.add(loginPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void setupServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 55545);
            chatService = (ChatService) registry.lookup("ChatService");
            userService = (UserService) registry.lookup("UserService");
            logService = (ChatLogService) registry.lookup("LogService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Service initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = userService.checkEmailAndPassword(email, password);
            if (user != null) {
                if (user.getRole().equalsIgnoreCase("admin")) {
                    User adminUser = userService.getUserByUsername(user.getUsername());
                    new AdminDashboardUI(adminUser, userService, chatService, logService);
                } else {
                    new userDashBoard(chatService, userService, logService, chatLog, user).handle();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(ChatLauncherUI::new);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                SessionFactory sessionFactory = null;
                List<ChatLog> chatLogs = new ChatLogServiceImpl(sessionFactory).getChatLogsWithNullEndTime();
                if (!chatLogs.isEmpty()) {
                    for (ChatLog chatLog : chatLogs) {
                        chatLog.setEnd_time(LocalDateTime.now());
                        new ChatLogServiceImpl(sessionFactory).updateChatLog(chatLog);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
    }

    // Rounded TextField
    static class RoundedTextField extends JTextField {
        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    // Rounded PasswordField
    static class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField(int columns) {
            super(columns);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    // Rounded Button
    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(getModel().isRollover() ? new Color(52, 103, 217) : new Color(72, 133, 237));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(72, 133, 237));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
        }
    }
}
