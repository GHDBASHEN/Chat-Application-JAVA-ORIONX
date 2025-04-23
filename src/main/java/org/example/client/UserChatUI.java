package org.example.client;

import org.example.domain.ChatGroup;
import org.example.domain.User;
import org.example.rmi.ChatService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class UserChatUI extends JFrame {
    private final User currentUser;
    private final ChatService chatService;
    private final DefaultListModel<ChatGroup> chatListModel = new DefaultListModel<>();
    private JList<ChatGroup> chatList; // Properly declared class member

    public UserChatUI(User user, ChatService chatService) {
        this.currentUser = user;
        this.chatService = chatService;
        initializeUI();
        loadChats();
    }

    private void initializeUI() {
        setTitle("Chat Dashboard - " + currentUser.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Chat List Panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        chatList = new JList<>(chatListModel); // Proper initialization
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ChatGroup) {
                    setText(((ChatGroup) value).getChatName());
                }
                return this;
            }
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadChats());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton subscribeBtn = new JButton("Subscribe");
        subscribeBtn.addActionListener(e -> subscribeToChat());
        JButton unsubscribeBtn = new JButton("Unsubscribe");
        unsubscribeBtn.addActionListener(e -> unsubscribeFromChat());

        buttonPanel.add(subscribeBtn);
        buttonPanel.add(unsubscribeBtn);

        leftPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.add(refreshBtn, BorderLayout.NORTH);

        // Message Panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        // Add message components here...

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        add(splitPane);
    }

    private void loadChats() {
        try {
            chatListModel.clear();
            chatService.getAllChats().forEach(chatListModel::addElement);
        } catch (RemoteException e) {
            showError("Failed to load chats: " + e.getMessage());
        }
    }

    private void subscribeToChat() {
        ChatGroup selected = chatList.getSelectedValue();
        if (selected == null) {
            showError("Please select a chat first!");
            return;
        }

        try {
            chatService.subscribeToChat(currentUser.getUser_id(), selected.getChatId());
            JOptionPane.showMessageDialog(this, "Subscribed to chat!");
            loadChats();
        } catch (RemoteException e) {
            showError("Subscription failed: " + e.getMessage());
        }
    }

    private void unsubscribeFromChat() {
        ChatGroup selected = chatList.getSelectedValue();
        if (selected == null) {
            showError("Please select a chat first!");
            return;
        }

        try {
            chatService.unsubscribeFromChat(currentUser.getUser_id(), selected.getChatId());
            JOptionPane.showMessageDialog(this, "Unsubscribed from chat!");
            loadChats();
        } catch (RemoteException e) {
            showError("Unsubscription failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}