package org.example.client.admin;

import org.example.domain.Chat;
import org.example.domain.User;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class AdminDashboardUI extends JFrame {
    private final UserService userService;
    private final ChatService chatService;
    private final String adminUsername;

    private JTable userTable;
    private JTable chatTable;
    private JTabbedPane tabbedPane;


    public AdminDashboardUI(String username, UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
        this.adminUsername = username;
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Chat Management", createChatManagementPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(33, 150, 243));

        JLabel welcome = new JLabel("Welcome, " + adminUsername);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcome.setForeground(new Color(120, 120, 120));

        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        userTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(userTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);

        JButton removeUserBtn = createActionButton("Remove User", "❌", this::removeUser);
        JButton refreshBtn = createActionButton("Refresh", "🔄", this::loadData);

        buttonPanel.add(removeUserBtn);
        buttonPanel.add(refreshBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChatManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        chatTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(chatTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);

        JButton createChatBtn = createActionButton("Create Chat", "➕", this::createChat);
      //  JButton subscribeBtn = createActionButton("Subscribe User", "🔗", () -> manageSubscription(true));
      //  JButton unsubscribeBtn = createActionButton("Unsubscribe User", "🔓", () -> manageSubscription(false));
        JButton refreshBtn = createActionButton("Refresh", "🔄", this::loadData);

        buttonPanel.add(createChatBtn);
      //  buttonPanel.add(subscribeBtn);
      //  buttonPanel.add(unsubscribeBtn);
        buttonPanel.add(refreshBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createActionButton(String text, String icon, Runnable action) {
        JButton btn = new JButton("<html><center>" + icon + "<br>" + text + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            action.run();
        });
        return btn;
    }

    private void createChat() {
        JTextField nameField = new JTextField();
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane scrollPane = new JScrollPane(descArea);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Chat Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(scrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New Chat", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Chat newChat = new Chat();
                newChat.setChatName(nameField.getText());
                newChat.setDescription(descArea.getText());
                chatService.createChat(newChat);
                JOptionPane.showMessageDialog(this, "Chat created successfully!");
                loadData();
            } catch (RemoteException e) {
                showError("Failed to create chat: " + e.getMessage());
            }
        }
    }

//    private void manageSubscription(boolean subscribe) {
//        try {
//            List<User> users = userService.getAllUsers();
//            List<Chat> chats = chatService.getAllChats();
//
//            if (users.isEmpty() || chats.isEmpty()) {
//                showError("No users or chats available");
//                return;
//            }
//
//            JComboBox<User> userCombo = new JComboBox<>(users.toArray(new User[0]));
//            JComboBox<Chat> chatCombo = new JComboBox<>(chats.toArray(new Chat[0]));
//
//            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
//            panel.add(new JLabel("Select User:"));
//            panel.add(userCombo);
//            panel.add(new JLabel("Select Chat:"));
//            panel.add(chatCombo);
//
//            int result = JOptionPane.showConfirmDialog(
//                    this, panel,
//                    subscribe ? "Subscribe User" : "Unsubscribe User",
//                    JOptionPane.OK_CANCEL_OPTION);
//
//            if (result == JOptionPane.OK_OPTION) {
//                User user = (User) userCombo.getSelectedItem();
//                Chat chat = (Chat) chatCombo.getSelectedItem();
//
//                if (subscribe) {
//                    chatService.subscribeUserToChat(user.getUser_id(), chat.getChatId());
//                } else {
//                    chatService.unsubscribeUserFromChat(user.getUser_id(), chat.getChatId());
//                }
//
//                JOptionPane.showMessageDialog(this, "Operation completed successfully!");
//                loadData();
//            }
//        } catch (RemoteException e) {
//            showError("Operation failed: " + e.getMessage());
//        }
//    }

    private void removeUser() {
        try {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a user first!");
                return;
            }

            // Retrieve as Integer first, then convert to Long
            Integer userIdInt = (Integer) userTable.getValueAt(selectedRow, 0);
            int userId = (int) userIdInt.longValue();  // Convert to Long

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this user?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                userService.deleteUser(userId);
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
                loadData();
            }
        } catch (RemoteException e) {
            showError("Failed to delete user: " + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            // Load users
            List<User> users = userService.getAllUsers();
            String[] userColumns = {"User ID", "Email", "Nickname", "Password", "Profile Picture", "Role", "Username"};
            DefaultTableModel userModel = new DefaultTableModel(userColumns, 0);
            for (User user : users) {
                userModel.addRow(new Object[]{
                        user.getUser_id(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getPassword(),
                        user.getProfile_picture(),
                        user.getRole(),
                        user.getUsername()
                });
            }
            userTable.setModel(userModel);

            // Load chats only if chatTable is initialized
            if (chatTable != null) {  // Add null check for safety
                List<Chat> chats = chatService.getAllChats();
                String[] chatColumns = {"Chat ID", "Chat Name", "Description"};
                DefaultTableModel chatModel = new DefaultTableModel(chatColumns, 0);
                for (Chat chat : chats) {
                    chatModel.addRow(new Object[]{
                            chat.getChatId(),
                            chat.getChatName(),
                            chat.getDescription()
                    });
                }
                chatTable.setModel(chatModel);
            }

        } catch (RemoteException e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 55545);
                UserService userService = (UserService) registry.lookup("UserService");
                ChatService chatService = (ChatService) registry.lookup("ChatService");
                new AdminDashboardUI("admin", userService, chatService);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
