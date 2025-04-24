package org.example.client.admin;

import org.example.domain.ChatGroup;
import org.example.domain.ChatLog;
import org.example.domain.ChatMessage;
import org.example.domain.User;
import org.example.rmi.ChatObserver;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.swing.SwingConstants;

public class AdminDashboardUI extends JFrame {
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;

    private final UserService userService;
    private final ChatService chatService;
    private final User currentAdminUser;

    private JPanel groupChatPanel;
    private JTextArea adminChatArea;
    private JTextField adminMsgField;
    private JButton adminSendButton;
    private JScrollPane adminGroupScroll;
    private JPanel adminGroupButtonPanel;
    private ChatGroup selectedAdminGroup;
    private ChatObserver adminObserver;
    private ChatObserver adminObserverStub;

    private JTable userTable;
    private JTable chatTable;

    public AdminDashboardUI(User adminUser, UserService userService, ChatService chatService) {
        this.currentAdminUser = adminUser;
        this.userService = userService;
        this.chatService = chatService;
        initializeUI();
        loadData();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (selectedAdminGroup != null) {
                        chatService.unsubscribe(currentAdminUser, adminObserverStub, null, selectedAdminGroup.getChatId());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeUI() {
        setTitle("Admin Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new ModernTabbedPane();
        tabbedPane.addTab("👥 User Management", createUserManagementPanel());
        tabbedPane.addTab("💬 Chat Management", createChatManagementPanel());
        tabbedPane.addTab("💬 Group Chats", createAdminChatPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY_COLOR);

        String username = currentAdminUser != null ? currentAdminUser.getUsername() : "Admin";
        JLabel welcome = new JLabel("Welcome, " + username + "👋");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcome.setForeground(new Color(100, 100, 100));

        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // User Table with custom styling
        userTable = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(63, 81, 181, 30));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        styleTable(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        styleScrollPane(scrollPane);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setOpaque(false);
        controlPanel.add(createIconButton("🔄 Refresh", "Refresh data", this::loadData));
        controlPanel.add(createIconButton("🗑️ Delete User", "Delete selected user", this::removeUser));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChatManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Chat Table with custom styling
        chatTable = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(63, 81, 181, 30));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        styleTable(chatTable);

        JScrollPane scrollPane = new JScrollPane(chatTable);
        styleScrollPane(scrollPane);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setOpaque(false);
        controlPanel.add(createIconButton("🔄 Refresh", "Refresh data", this::loadData));
        controlPanel.add(createIconButton("➕ New Chat", "Create new chat", this::createChat));
        controlPanel.add(createIconButton("👥 Add Members", "Add users to chat", this::createChatUser));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAdminChatPanel() {
        groupChatPanel = new JPanel(new BorderLayout());
        groupChatPanel.setOpaque(false);
        groupChatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Group list panel
        adminGroupButtonPanel = new JPanel();
        adminGroupButtonPanel.setLayout(new BoxLayout(adminGroupButtonPanel, BoxLayout.Y_AXIS));
        adminGroupScroll = new JScrollPane(adminGroupButtonPanel);
        styleScrollPane(adminGroupScroll);

        // Chat area
        adminChatArea = new JTextArea();
        adminChatArea.setEditable(false);
        adminChatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane chatScroll = new JScrollPane(adminChatArea);
        styleScrollPane(chatScroll);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        adminMsgField = new JTextField();
        adminSendButton = createIconButton("📤 Send", "Send message", this::sendAdminGroupMessage);

        // Enable Enter key for sending
        adminMsgField.addActionListener(e -> sendAdminGroupMessage());

        inputPanel.add(adminMsgField, BorderLayout.CENTER);
        inputPanel.add(adminSendButton, BorderLayout.EAST);

        // Add components
        groupChatPanel.add(adminGroupScroll, BorderLayout.WEST);
        groupChatPanel.add(chatScroll, BorderLayout.CENTER);
        groupChatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Initialize chat observer
        initializeAdminObserver();
        loadAdminGroups();

        return groupChatPanel;
    }

    private void initializeAdminObserver() {
        try {
            adminObserver = new ChatObserver() {
                @Override
                public void notifyNewMessage(String message, int chatId) throws RemoteException {
                    if (selectedAdminGroup != null && chatId == selectedAdminGroup.getChatId()) {
                        SwingUtilities.invokeLater(() -> {
                            adminChatArea.append(message + "\n");
                            // Auto-scroll to bottom
                            adminChatArea.setCaretPosition(adminChatArea.getDocument().getLength());
                        });
                    }
                }
            };
            adminObserverStub = (ChatObserver) UnicastRemoteObject.exportObject(adminObserver, 0);
        } catch (RemoteException e) {
            showError("Error initializing chat observer: " + e.getMessage());
        }
    }

    private void updateChatSubscription() {
        if (selectedAdminGroup != null) {
            try {
                // Unsubscribe from previous group
                chatService.unsubscribe(currentAdminUser, adminObserverStub, null, selectedAdminGroup.getChatId());

                // Subscribe to new group
                ChatLog log = new ChatLog();
                log.setStart_time(LocalDateTime.now());
                chatService.subscribe(currentAdminUser, adminObserverStub, log, selectedAdminGroup.getChatId());
            } catch (RemoteException e) {
                showError("Error updating subscription: " + e.getMessage());
            }
        }
    }

    // Modified loadGroupMessages method
    private void loadGroupMessages(int groupId) {
        try {
            adminChatArea.setText("");
            List<ChatMessage> messages = chatService.getAllChatMessages(groupId);
            for (ChatMessage msg : messages) {
                adminChatArea.append(formatMessage(msg) + "\n");
            }
            // Auto-scroll to bottom
            adminChatArea.setCaretPosition(adminChatArea.getDocument().getLength());
        } catch (RemoteException e) {
            showError("Failed to load messages: " + e.getMessage());
        }
    }

    // Updated formatMessage method
    private String formatMessage(ChatMessage msg) {
        try {
            return String.format("[%s] %s: %s",
                    msg.getStart_at().format(DateTimeFormatter.ofPattern("HH:mm")),
                    msg.getUser().getUsername(),
                    msg.getMessage());
        } catch (Exception e) {
            return "[Error formatting message] " + msg.getMessage();
        }
    }

    // Updated sendAdminGroupMessage method
    private void sendAdminGroupMessage() {
        if (selectedAdminGroup == null) {
            showError("Please select a group first!");
            return;
        }

        String message = adminMsgField.getText().trim();
        if (message.isEmpty()) return;

        try {
            // Send through service
            chatService.sendMessage(message, currentAdminUser, selectedAdminGroup.getChatId());
            adminMsgField.setText("");
        } catch (RemoteException e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }


    private void loadAdminGroups() {
        try {
            adminGroupButtonPanel.removeAll();
            List<ChatGroup> groups = chatService.getAllChats();

            for (ChatGroup group : groups) {
                JButton groupBtn = new JButton(group.getChatName());
                styleGroupButton(groupBtn);
                groupBtn.addActionListener(e -> {
                    selectedAdminGroup = group;
                    loadGroupMessages(group.getChatId());
                    highlightSelectedButton(groupBtn);
                });
                adminGroupButtonPanel.add(groupBtn);
            }

            adminGroupButtonPanel.revalidate();
            adminGroupButtonPanel.repaint();

            if (!groups.isEmpty()) {
                selectedAdminGroup = groups.get(0);
                loadGroupMessages(selectedAdminGroup.getChatId());
            }
        } catch (RemoteException e) {
            showError("Failed to load groups: " + e.getMessage());
        }
    }





    private void styleGroupButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(200, 40));
    }

    private void highlightSelectedButton(JButton selectedButton) {
        for (Component comp : adminGroupButtonPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setBackground(btn == selectedButton ?
                        new Color(63, 81, 181, 30) : Color.WHITE);
            }
        }
    }





    private String formatAdminMessage(String rawMessage) {
        return String.format("[%s] %s",
                new SimpleDateFormat("HH:mm").format(new Date()),
                rawMessage);
    }

    // Add this to your existing styleScrollPane method
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(new Color(240, 240, 240)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
    }

    // Add this helper method to style the message area
//    private void styleScrollPane(JScrollPane scrollPane) {
//        scrollPane.setBorder(new CompoundBorder(
//                new LineBorder(new Color(240, 240, 240)),
//                new EmptyBorder(10, 10, 10, 10)
//        ));
//        scrollPane.getViewport().setBackground(Color.WHITE);
//        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
//    }

    // Add custom scrollbar UI
    class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color SCROLL_BAR_COLOR = new Color(200, 200, 200);

        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = SCROLL_BAR_COLOR;
            this.trackColor = BACKGROUND_COLOR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 5));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial Black", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Left-align header text
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
    }

    private JButton createIconButton(String text, String tooltip, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        button.setBackground(CARD_COLOR);
        button.setToolTipText(tooltip);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(CARD_COLOR);
            }
        });

        return button;
    }

    class ModernTabbedPane extends JTabbedPane {
        public ModernTabbedPane() {
            setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                @Override
                protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}

                @Override
                protected void paintTabBackground(Graphics g, int tabPlacement,
                                                  int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                    if (isSelected) {
                        g.setColor(PRIMARY_COLOR);
                        g.fillRect(x, y, w, h);
                    }
                }

                @Override
                protected void paintText(Graphics g, int tabPlacement,
                                         Font font, FontMetrics metrics, int tabIndex,
                                         String title, Rectangle textRect, boolean isSelected) {
                    g.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
                    super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
                }

                @Override
                protected void paintTabBorder(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h, boolean isSelected) {}

                @Override
                protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                                   Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect,
                                                   boolean isSelected) {}
            });

            setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setBackground(new Color(150, 150, 150));
            setOpaque(true);
        }
    }

//    private JButton createActionButton(String text, String icon, Runnable action) {
//        JButton btn = new JButton("<html><center>" + icon + "<br>" + text + "</center></html>");
//        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        btn.setBackground(Color.WHITE);
//        btn.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(220, 220, 220)),
//                BorderFactory.createEmptyBorder(10, 15, 10, 15)
//        ));
//        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        btn.addActionListener(e -> action.run());
//        return btn;
//    }

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
                ChatGroup newChatGroup = new ChatGroup();
                newChatGroup.setChatName(nameField.getText());
                newChatGroup.setDescription(descArea.getText());
                newChatGroup.setAdmin(currentAdminUser);
                chatService.createChat(newChatGroup);
                JOptionPane.showMessageDialog(this, "Chat created successfully!");
                loadData();
            } catch (RemoteException e) {
                showError("Failed to create chat: " + e.getMessage());
            }
        }
    }

    private void createChatUser() {
        try {
            // Fetch users and chat groups
            List<User> users = chatService.getAllUsers();
            List<ChatGroup> chatGroups = chatService.getAllChats();

            // Prepare combo boxes
            JComboBox<User> userComboBox = new JComboBox<>(users.toArray(new User[0]));
            JComboBox<ChatGroup> groupComboBox = new JComboBox<>(chatGroups.toArray(new ChatGroup[0]));

            // Render names instead of object toString
            userComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                return new JLabel(value != null ? value.getUsername() + " (ID: " + value.getUser_id() + ")" : "");
            });

            groupComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                return new JLabel(value != null ? value.getChatName() + " (ID: " + value.getChatId() + ")" : "");
            });

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("Select User:"));
            panel.add(userComboBox);
            panel.add(new JLabel("Select Chat Group:"));
            panel.add(groupComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Assign User to Group", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                User selectedUser = (User) userComboBox.getSelectedItem();
                ChatGroup selectedGroup = (ChatGroup) groupComboBox.getSelectedItem();

                if (selectedUser != null && selectedGroup != null) {
                    chatService.addUserToGroup(selectedUser.getUser_id(), selectedGroup.getChatId());
                    JOptionPane.showMessageDialog(this, "User added to group successfully!");
                    loadData();
                } else {
                    showError("You must select both user and group.");
                }
            }
        } catch (RemoteException e) {
            showError("Failed to load data: " + e.getMessage());
        }
    }


    private void removeUser() {
        try {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a user first!");
                return;
            }

            int userId = (Integer) userTable.getValueAt(selectedRow, 0);

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
            String[] userColumns = {"User ID", "Email", "Nickname", "Role", "Username"};
            DefaultTableModel userModel = new DefaultTableModel(userColumns, 0);
            for (User user : users) {
                userModel.addRow(new Object[]{
                        user.getUser_id(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole(),
                        user.getUsername()
                });
            }
            userTable.setModel(userModel);

            // Load chats
            List<ChatGroup> chatGroups = chatService.getAllChats();
            String[] chatColumns = {"Chat ID", "Chat Name", "Description", "Admin"};
            DefaultTableModel chatModel = new DefaultTableModel(chatColumns, 0);
            for (ChatGroup chatGroup : chatGroups) {
                chatModel.addRow(new Object[]{
                        chatGroup.getChatId(),
                        chatGroup.getChatName(),
                        chatGroup.getDescription(),
                        chatGroup.getAdmin() != null ? chatGroup.getAdmin().getUsername() : "N/A"
                });
            }
            chatTable.setModel(chatModel);
            chatTable.repaint();

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

                // Get admin user properly
                User adminUser = userService.getUserByUsername("admin");
                if (adminUser == null) {
                    JOptionPane.showMessageDialog(null, "Admin user not found!");
                    return;
                }
                new AdminDashboardUI(adminUser, userService, chatService);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}