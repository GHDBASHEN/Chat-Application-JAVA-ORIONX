package org.example.client;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.example.domain.ChatLog;
import org.example.domain.User;
import org.example.rmi.*;
import org.example.server.impl.ChatLogServiceImpl;

public class ChatLauncherUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private ChatService chatService;
    private UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;

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
        styleTextField(emailField, "Email");
        passwordField = new JPasswordField(20);
        styleTextField(passwordField, "Password");

        loginButton = new JButton("Login");
        styleButton(loginButton, new Color(76, 175, 80));

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

        mainPanel.add(loginPanel, BorderLayout.CENTER);
        add(mainPanel);

        loginButton.addActionListener(this::handleLogin);
        setVisible(true);
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setPreferredSize(new Dimension(250, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        ((JTextComponent) field).setText(placeholder);
        ((JTextComponent) field).setForeground(Color.GRAY);
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
                openChatWindow(user);
                emailField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openChatWindow(User user) throws Exception {
        JFrame chatFrame = new JFrame("Chat - " + user.getEmail());
        chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chatFrame.setSize(600, 500);
        chatFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel headerLabel = new JLabel("Chat Room - " + user.getEmail());
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(120, 120, 120));
        headerPanel.add(timeLabel, BorderLayout.EAST);

        // Chat area
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextField inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JButton sendButton = new JButton("Send");
        styleButton(sendButton, new Color(33, 150, 243));
        sendButton.setPreferredSize(new Dimension(100, 40));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Chat functionality
        ChatObserver observer = new ChatObserver() {
            public void notifyNewMessage(String message) throws RemoteException {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append(message + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            }
        };

        ChatObserver stub = (ChatObserver) UnicastRemoteObject.exportObject(observer, 0);
        chatLog = logService.login(user.getUser_id());
        chatService.subscribe(user, stub, chatLog);

        sendButton.addActionListener(ev -> handleMessageSend(inputField, user, stub));

        inputField.addActionListener(ev -> handleMessageSend(inputField, user, stub));

        chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    chatLog = logService.logout(user.getUser_id());
                    if (chatLog != null) {
                        chatService.unsubscribe(user, stub, chatLog);
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        chatFrame.add(mainPanel);
        chatFrame.setVisible(true);
    }

    private void handleMessageSend(JTextField inputField, User user, ChatObserver stub) {
        try {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                if (logService.isUserOnline(user.getUser_id())) {
                    chatService.sendMessage(msg, user);
                }
                if (msg.equalsIgnoreCase("Bye")) {
                    chatLog = logService.logout(user.getUser_id());
                    chatService.unsubscribe(user, stub, chatLog);
                }
                inputField.setText("");
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
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
                List<ChatLog> chatLogs = new ChatLogServiceImpl().getChatLogsWithNullEndTime();
                if (!chatLogs.isEmpty()) {
                    for (ChatLog chatLog : chatLogs) {
                        chatLog.setEnd_time(LocalDateTime.now());
                        new ChatLogServiceImpl().updateChatLog(chatLog);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
    }
}