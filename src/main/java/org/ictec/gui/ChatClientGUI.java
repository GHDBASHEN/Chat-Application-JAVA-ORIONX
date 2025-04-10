package org.ictec.gui;
import org.ictec.client.ChatObserver;
import org.ictec.entities.User;
import org.ictec.server.ChatService;

import javax.swing.*;
import java.awt.*;

public class ChatClientGUI extends JFrame implements ChatObserver {
    private JTextArea chatArea;
    private JTextField messageField;

    public ChatClientGUI(User user, ChatService chatService) {
        setTitle("Chat Room");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText();
        // Call RMI sendMessage() here
        messageField.setText("");
    }

    @Override
    public void update(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }
}