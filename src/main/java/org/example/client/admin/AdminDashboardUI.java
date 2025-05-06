package org.example.client.admin;

import org.example.domain.ChatGroup;
import org.example.domain.ChatLog;
import org.example.domain.ChatMessage;
import org.example.domain.User;
import org.example.rmi.ChatLogService;
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
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingConstants;

public class AdminDashboardUI extends JFrame {
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;

    private final UserService userService;
    private final ChatService chatService;
    private final User currentAdminUser;
    private final ChatLogService logService;
    private ChatLog currentChatLog;
    private int currentGroupId = -1;


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

    public AdminDashboardUI(User adminUser, UserService userService, ChatService chatService, ChatLogService logService) {
        this.currentAdminUser = adminUser;
        this.userService = userService;
        this.chatService = chatService;
        this.logService = logService;
        initializeUI();
        loadData();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (currentChatLog != null && adminObserverStub != null) {
                        chatService.unsubscribeFromChat(currentAdminUser, adminObserverStub, currentChatLog, currentGroupId);
                        logService.logout(currentAdminUser.getUser_id());
                    }
                } catch (RemoteException e) {
                    System.err.println("Cleanup error: " + e.getMessage());
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
        tabbedPane.addTab("👤 Profile", createProfilePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);

    }

    // Welcome label for header
    private JLabel welcomeLabel;

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY_COLOR);

        String username = currentAdminUser != null ? currentAdminUser.getUsername() : "Admin";
        welcomeLabel = new JLabel("Welcome, " + username + "👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(new Color(100, 100, 100));

        panel.add(title, BorderLayout.WEST);
        panel.add(welcomeLabel, BorderLayout.EAST);

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
        //controlPanel.add(createIconButton("🔄 Refresh", "Refresh data", this::loadData));
        controlPanel.add(createIconButton("➕ New Chat Group", "Create new Chat Group", this::createChat));
        controlPanel.add(createIconButton("👥 Delete Chat Group", "Delete Existing Chat Group", this::deleteChat));
        controlPanel.add(createIconButton("👥 Add Group Members", "Add users to Chat Group", this::createChatUser));
        controlPanel.add(createIconButton("👥 View / Remove Group Members", "View and Remove users from Chat Group", this::manageGroupUsers));

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
       // initializeAdminChatComponents();
        return groupChatPanel;
    }

    private void initializeAdminObserver() {
        try {
            adminObserver = new ChatObserver() {
                @Override
                public void notifyNewMessage(String message, int chatId) throws RemoteException {
                    if (currentGroupId == chatId) {
                        SwingUtilities.invokeLater(() -> {
                            adminChatArea.append(message + "\n");
                            adminChatArea.setCaretPosition(adminChatArea.getDocument().getLength());
                        });
                    }
                }
            };

            // Export observer immediately
            adminObserverStub = (ChatObserver) UnicastRemoteObject.exportObject(adminObserver, 0);
            System.out.println("Observer initialized: " + (adminObserverStub != null));

        } catch (RemoteException e) {
            showError("Observer initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeAdminChatComponents() {
        try {
            // Initialize observer
            adminObserver = new ChatObserver() {
                @Override
                public void notifyNewMessage(String message, int chatId) throws RemoteException {
                    if (currentGroupId == chatId) {
                        SwingUtilities.invokeLater(() -> {
                            adminChatArea.append(message + "\n");
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

    private void handleGroupSelection(int groupId) {
        try {
            // Add null checks before unsubscribe
            if (currentGroupId != -1) {
                if (adminObserverStub != null && currentChatLog != null) {
                    chatService.unsubscribeFromChat(currentAdminUser, adminObserverStub, currentChatLog, currentGroupId);
                    logService.logout(currentAdminUser.getUser_id());
                } else {
                    System.err.println("Skipping unsubscribe - observer or chatlog null");
                }
            }

            // Initialize new session
            currentChatLog = logService.login(currentAdminUser.getUser_id());
            if (currentChatLog == null) {
                showError("Failed to create chat session");
                return;
            }

            chatService.subscribe(currentAdminUser, adminObserverStub, currentChatLog, groupId);
            currentGroupId = groupId;
            loadGroupMessages(groupId);

        } catch (RemoteException e) {
            showError("Connection error: " + e.getMessage());
        }
    }

    // Update loadAdminGroups method
    private void loadAdminGroups() {
        try {
            adminGroupButtonPanel.removeAll();
            List<ChatGroup> groups = chatService.getAllChats();

            for (ChatGroup group : groups) {
                JButton groupBtn = new JButton(group.getChatName());
                styleGroupButton(groupBtn);
                groupBtn.addActionListener(e -> {
                    // Ensure observer exists before handling selection
                    if (adminObserverStub == null) {
                        showError("Chat observer not initialized");
                        return;
                    }
                    selectedAdminGroup = group;
                    handleGroupSelection(group.getChatId());
                    highlightSelectedButton(groupBtn);
                });
                adminGroupButtonPanel.add(groupBtn);
            }

            if (!groups.isEmpty()) {
                selectedAdminGroup = groups.get(0);
                handleGroupSelection(selectedAdminGroup.getChatId());
            }
        } catch (RemoteException e) {
            showError("Failed to load groups: " + e.getMessage());
        }
    }

    // Update sendAdminGroupMessage method
    private void sendAdminGroupMessage() {
        // Validate all required components
        if (selectedAdminGroup == null) {
            showError("Please select a group first!");
            return;
        }

        if (currentAdminUser == null) {
            showError("Admin session expired");
            return;
        }

        if (logService == null || chatService == null) {
            showError("Chat services unavailable");
            return;
        }

        String message = adminMsgField.getText().trim();
        if (message.isEmpty()) return;

        try {
            // Verify online status and group ID
            if (!logService.isUserOnline(currentAdminUser.getUser_id())) {
                showError("You appear offline. Cannot send messages.");
                return;
            }

            if (currentGroupId == -1) {
                showError("No active chat group");
                return;
            }

            // Send through service
            chatService.sendAdminMessage(
                    "[ADMIN] " + message, // Add admin prefix
                    currentAdminUser,
                    currentGroupId
            );
            adminMsgField.setText("");

        } catch (RemoteException e) {
            showError("Failed to send message: " + e.getCause().getMessage());
        }
    }

//    private void initializeAdminObserver() {
//        try {
//            adminObserver = new ChatObserver() {
//                @Override
//                public void notifyNewMessage(String message, int chatId) throws RemoteException {
//                    if (selectedAdminGroup != null && chatId == selectedAdminGroup.getChatId()) {
//                        SwingUtilities.invokeLater(() -> {
//                            adminChatArea.append(message + "\n");
//                            // Auto-scroll to bottom
//                            adminChatArea.setCaretPosition(adminChatArea.getDocument().getLength());
//                        });
//                    }
//                }
//            };
//            adminObserverStub = (ChatObserver) UnicastRemoteObject.exportObject(adminObserver, 0);
//        } catch (RemoteException e) {
//            showError("Error initializing chat observer: " + e.getMessage());
//        }
//    }




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
            String username = msg.getUser().getUsername();
            String rawMessage = msg.getMessage();

            // Remove redundant username prefixes
            String cleanedMessage = rawMessage.replaceAll("\\d{1,2}:\\d{2} [AP]M$", "").trim();
            return String.format("[%s]  %s",
                    msg.getStart_at().format(DateTimeFormatter.ofPattern("HH:mm")),
                    cleanedMessage);
        } catch (Exception e) {
            return "[Error formatting message] " + msg.getMessage();
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



    // Add this to your existing styleScrollPane method
    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(new Color(240, 240, 240)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
    }



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

    private void deleteChat() {
        try {
            int selectedRow = chatTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a chat first!");
                return;
            }

            int chatId = (Integer) chatTable.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this chat group?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                chatService.deleteChat(chatId);
                JOptionPane.showMessageDialog(this, "Chat deleted successfully!");
                loadData();
            }
        } catch (RemoteException e) {
            showError("Failed to delete chat: " + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
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

    private void manageGroupUsers() {
        try {
            int selectedRow = chatTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a chat group first!");
                return;
            }

            int chatId = (Integer) chatTable.getValueAt(selectedRow, 0);
            List<User> groupUsers = chatService.getUsersInChat(chatId);

            JPanel userListPanel = new JPanel();
            userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));

            for (User user : groupUsers) {
                JPanel userPanel = new JPanel(new BorderLayout());
                userPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

                JLabel userLabel = new JLabel(user.getUsername() + " (" + user.getEmail() + ")");
                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(e -> removeUserFromChat(user.getUser_id(), chatId));

                userPanel.add(userLabel, BorderLayout.CENTER);
                userPanel.add(removeButton, BorderLayout.EAST);
                userListPanel.add(userPanel);
            }

            JScrollPane scrollPane = new JScrollPane(userListPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Manage Users in " + chatTable.getValueAt(selectedRow, 1),
                    JOptionPane.PLAIN_MESSAGE);

        } catch (RemoteException e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private void removeUserFromChat(int userId, int chatId) {
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to remove this user?",
                    "Confirm Removal",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                chatService.removeUserFromChat(userId, chatId);
                manageGroupUsers(); // Refresh the list
            }
        } catch (RemoteException e) {
            showError("Failed to remove user: " + e.getMessage());
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

    // Profile-related fields
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nicknameField;
    private JTextField profilePictureField;
    private JButton saveProfileButton;
    private JLabel profilePicLabel;

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create profile picture panel at the top
        JPanel picturePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        picturePanel.setOpaque(false);
        profilePicLabel = new JLabel();
        updateProfilePicture();
        picturePanel.add(profilePicLabel);

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create components
        emailField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        nicknameField = new JTextField(20);
        profilePictureField = new JTextField(20);
        profilePictureField.setEditable(false); // Make it read-only
        JButton chooseFileButton = createIconButton("📁 Choose File", "Select profile picture", this::chooseProfilePicture);
        saveProfileButton = createIconButton("💾 Save Profile", "Save profile changes", this::saveProfile);

        // Pre-populate fields with current user data
        emailField.setText(currentAdminUser.getEmail());
        usernameField.setText(currentAdminUser.getUsername());
        passwordField.setText(currentAdminUser.getPassword());
        confirmPasswordField.setText(currentAdminUser.getPassword());
        nicknameField.setText(currentAdminUser.getNickname());
        profilePictureField.setText(currentAdminUser.getProfilePicture());

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        formPanel.add(nicknameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Profile Picture:"), gbc);

        // Create a panel for the profile picture field and choose file button
        JPanel profilePicturePanel = new JPanel(new BorderLayout(5, 0));
        profilePicturePanel.add(profilePictureField, BorderLayout.CENTER);
        profilePicturePanel.add(chooseFileButton, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(profilePicturePanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(saveProfileButton, gbc);

        // Add components to main panel
        profilePanel.add(picturePanel, BorderLayout.NORTH);
        profilePanel.add(formPanel, BorderLayout.CENTER);

        return profilePanel;
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");

        // Set file filter to only show image files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                        name.endsWith(".png") || name.endsWith(".gif") ||
                        name.endsWith(".bmp");
            }
            public String getDescription() {
                return "Image files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            profilePictureField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void saveProfile() {
        try {
            // Get password values
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match!");
                return;
            }

            // Update user object with new values
            currentAdminUser.setEmail(emailField.getText());
            currentAdminUser.setUsername(usernameField.getText());
            currentAdminUser.setPassword(password);
            currentAdminUser.setNickname(nicknameField.getText());

            // Handle profile picture
            String selectedFilePath = profilePictureField.getText();
            if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                try {
                    // Create directory for profile pictures if it doesn't exist
                    java.io.File profilePicsDir = new java.io.File("profile_pictures");
                    if (!profilePicsDir.exists()) {
                        profilePicsDir.mkdir();
                    }

                    // Get the original file
                    java.io.File sourceFile = new java.io.File(selectedFilePath);
                    String fileName = currentAdminUser.getUser_id() + "_" + sourceFile.getName();
                    java.io.File destFile = new java.io.File(profilePicsDir, fileName);

                    // Copy the file
                    java.nio.file.Files.copy(
                            sourceFile.toPath(),
                            destFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );

                    // Update the path in the user object to the copied file
                    String newPath = destFile.getAbsolutePath();
                    currentAdminUser.setProfilePicture(newPath);
                    // Update the UI to show the new path
                    profilePictureField.setText(newPath);
                } catch (java.io.IOException ioEx) {
                    System.err.println("Error copying profile picture: " + ioEx.getMessage());
                    // If file copy fails, still save the original path
                    currentAdminUser.setProfilePicture(selectedFilePath);
                }
            } else {
                currentAdminUser.setProfilePicture(selectedFilePath);
            }

            // Call service to update user
            userService.updateUser(currentAdminUser);

            // Update UI to reflect changes
            welcomeLabel.setText("Welcome, " + currentAdminUser.getUsername() + "👋");

            // Update profile picture
            updateProfilePicture();

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (RemoteException ex) {
            showError("Error updating profile: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Method to update the profile picture in the UI
    private void updateProfilePicture() {
        String profilePicPath = currentAdminUser.getProfilePicture();
        if (profilePicPath != null && !profilePicPath.isEmpty()) {
            // Create a circular profile picture with 100x100 dimensions
            ImageIcon profileIcon = createCircularProfilePicture(profilePicPath, 100, 100);
            profilePicLabel.setIcon(profileIcon);
        } else {
            // Set a default profile picture
            profilePicLabel.setIcon(createDefaultProfilePicture(100, 100));
        }
    }

    // Method to create a circular profile picture
    private ImageIcon createCircularProfilePicture(String imagePath, int width, int height) {
        try {
            // Load the image
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage == null) {
                // Return a default image or placeholder if the image couldn't be loaded
                return createDefaultProfilePicture(width, height);
            }

            // Create a new buffered image with transparency
            BufferedImage circularImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Get the graphics context
            Graphics2D g2 = circularImage.createGraphics();

            // Set rendering hints for better quality
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Create a circular clip
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, width, height);
            g2.setClip(circle);

            // Scale the original image to fit the circle
            g2.drawImage(originalImage, 0, 0, width, height, null);

            // Add a circular border
            g2.setClip(null);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.draw(circle);

            g2.dispose();

            return new ImageIcon(circularImage);
        } catch (IOException e) {
            System.err.println("Error loading profile picture: " + e.getMessage());
            return createDefaultProfilePicture(width, height);
        }
    }

    // Method to create a default profile picture
    private ImageIcon createDefaultProfilePicture(int width, int height) {
        BufferedImage defaultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = defaultImage.createGraphics();

        // Set rendering hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a circle with a gradient fill
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, width, height);
        g2.setClip(circle);

        // Create a gradient paint
        GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, width, height, new Color(100, 100, 255));
        g2.setPaint(gradient);
        g2.fill(circle);

        // Add a border
        g2.setClip(null);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.draw(circle);

        // Add a user icon or initials
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, width / 3));
        FontMetrics fm = g2.getFontMetrics();
        String text = "A"; // "A" for Admin
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2.drawString(text, (width - textWidth) / 2, (height + textHeight / 2) / 2);

        g2.dispose();

        return new ImageIcon(defaultImage);
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
                ChatLogService logService = (ChatLogService) registry.lookup("ChatLogService");

                // Get admin user properly
                User adminUser = userService.getUserByUsername("admin");
                if (adminUser == null) {
                    JOptionPane.showMessageDialog(null, "Admin user not found!");
                    return;
                }
                new AdminDashboardUI(adminUser, userService, chatService, logService);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}
