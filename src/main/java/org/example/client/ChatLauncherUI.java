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
    private ChatService chatService;
    private static UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;
    private static JButton registerButton;


    public ChatLauncherUI() {
        initUI();
        setupServices();
    }

    private void initUI() {
        setTitle("Multi-User Chat Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main container with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(30, 136, 229);
                Color color2 = new Color(0, 172, 193);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Login form panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Chat Application");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        emailField = new JTextField(20);

        passwordField = new JPasswordField(20);


        loginButton = new JButton("Login");
        styleButton(loginButton, new Color(183, 59, 198));

        //fro register
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerButton.setBackground(new Color(183, 59, 198));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));



        registerButton.addActionListener(e -> {
            new RegisterUI(userService, chatService);
        });





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
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        //for register
        gbc.gridy++;
        loginPanel.add(registerButton, gbc);

        mainPanel.add(loginPanel, BorderLayout.CENTER);
        add(mainPanel);

        loginButton.addActionListener(this::handleLogin);
        setVisible(true);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                    new AdminDashboardUI(adminUser, userService, chatService);
                }
                 else {
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

        SwingUtilities.invokeLater(() -> new ChatLauncherUI());


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
}