package org.example.client.user;

import org.example.domain.ChatLog;
import org.example.domain.User;
import org.example.rmi.ChatLogService;
import org.example.rmi.ChatObserver;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class userDashBoard {
    private JPanel topPanel;
    private JPanel leftPane;
    private JPanel main;
    private JButton updateButton;
    private JButton chatButton;
    private JTabbedPane tabbedPane1;
    private JPanel updatePane;
    private JPanel chatPane;
    private JTextArea textArea1;
    private JTextField msgFeild;
    private JButton sendButton;
    private JLabel userName;
    private ChatService chatService;
    private UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;

    // Profile update components
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JTextField profilePictureField;
    private JButton saveProfileButton;
    private User currentUser;

    private void initializeProfileUpdateUI() {
        // Create components
        emailField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        nicknameField = new JTextField(20);
        profilePictureField = new JTextField(20);
        saveProfileButton = new JButton("Save Profile");

        // Pre-populate fields with current user data
        emailField.setText(currentUser.getEmail());
        usernameField.setText(currentUser.getUsername());
        passwordField.setText(currentUser.getPassword());
        nicknameField.setText(currentUser.getNickname());
        profilePictureField.setText(currentUser.getProfile_picture());

        // Set up layout
        updatePane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        updatePane.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        updatePane.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        updatePane.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        updatePane.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        updatePane.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        updatePane.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        updatePane.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        updatePane.add(nicknameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        updatePane.add(new JLabel("Profile Picture URL:"), gbc);

        gbc.gridx = 1;
        updatePane.add(profilePictureField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        updatePane.add(saveProfileButton, gbc);

        // Add action listener to save button
        saveProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Update user object with new values
                    currentUser.setEmail(emailField.getText());
                    currentUser.setUsername(usernameField.getText());
                    currentUser.setPassword(new String(passwordField.getPassword()));
                    currentUser.setNickname(nicknameField.getText());
                    currentUser.setProfile_picture(profilePictureField.getText());

                    // Call service to update user
                    userService.updateUser(currentUser);

                    // Update UI to reflect changes
                    userName.setText("Hello, " + currentUser.getUsername() + "\uD83D\uDE09");

                    // Show success message
                    JOptionPane.showMessageDialog(main, 
                        "Profile updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);

                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(main, 
                        "Error updating profile: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    public userDashBoard(ChatService chatService, UserService userService, ChatLogService logService, ChatLog chatLog) {
        this.chatService = chatService;
        this.userService = userService;
        this.logService = logService;
        this.chatLog = chatLog;

    }

    public void handle(User user) throws Exception {
        this.currentUser = user;

        JFrame frame = new JFrame("User Dashboard");
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        // Now UI components should be initialized
        userName.setText("Hello, "+user.getUsername() + "\uD83D\uDE09");

        // Initialize profile update UI
        initializeProfileUpdateUI();

        ChatObserver observer = new ChatObserver() {
            public void notifyNewMessage(String message) throws RemoteException {
                SwingUtilities.invokeLater(() -> textArea1.append(message + "\n"));
            }
        };

        ChatObserver stub = (ChatObserver) UnicastRemoteObject.exportObject(observer, 0);
        chatLog = logService.login(user.getUser_id());
        chatService.subscribe(user, stub, chatLog);

        // Make the frame visible after all setup is done
        frame.setVisible(true);


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedComponent(updatePane);
            }
        });
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane1.setSelectedComponent(chatPane);
            }
        });


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String msg = msgFeild.getText().trim();
                    if (!msg.isEmpty()) {
                        if (logService.isUserOnline(user.getUser_id())) {
                            chatService.sendMessage(msg, user);
                        } else {
                            System.out.println("User session has ended. Cannot send message.");
                        }

                        if (msg.equalsIgnoreCase("Bye")) {
                            chatLog = logService.logout(user.getUser_id());
                            chatService.unsubscribe(user, stub, chatLog);
                        }

                        msgFeild.setText("");
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    chatLog = logService.logout(user.getUser_id());
                    if (chatLog != null) {
                        chatService.unsubscribe(user, stub, chatLog);
                        System.out.println("User logged out and unsubscribed.");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
