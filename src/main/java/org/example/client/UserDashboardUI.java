package org.example.client;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;

import javax.swing.*;
import java.awt.*;

public class UserDashboardUI extends JFrame {
    public UserDashboardUI(String username, UserService userService, ChatService chatService) {
        setTitle("User Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("Welcome " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton updateProfileBtn = new JButton("Update Profile");
        JButton subscribeBtn = new JButton("Subscribe to Chat");
        JButton unsubscribeBtn = new JButton("Unsubscribe from Chat");

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.add(welcomeLabel);
        panel.add(updateProfileBtn);
        panel.add(subscribeBtn);
        panel.add(unsubscribeBtn);

        add(panel);
        setVisible(true);
    }
}
