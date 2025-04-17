package org.example.client;

import org.example.domain.User;
import org.example.rmi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientDemo {

    private ChatService chatService;
    private UserService userService;
    private ChatLogService logService;

    public ClientDemo() throws RemoteException {
        try {
            setupRMI();
            loginWindow();
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupRMI() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 55555);
        chatService = (ChatService) registry.lookup("ChatService");
        userService = (UserService) registry.lookup("UserService");
        logService = (ChatLogService) registry.lookup("LogService");
    }

    public void chatWindow(User user) throws RemoteException {
        JFrame frame = new JFrame("Chat App - " + user.getEmail());
        JTextArea chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);

        JTextField inputField = new JTextField(20);
        JButton sendButton = new JButton("Send");

        // 🧠 Observer inner class to receive messages
        ChatObserver observer = new ChatObserver() {
            @Override
            public void notifyNewMessage(String message) throws RemoteException {
                SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
            }
        };

        // Exporting the observer
        ChatObserver observerStub = (ChatObserver) UnicastRemoteObject.exportObject(observer, 0);
        chatService.subscribe(user, observerStub, logService.login(user.getUser_id()));
        logService.login(user.getUser_id());

        sendButton.addActionListener(e -> {
            try {
                chatService.sendMessage(inputField.getText(), user);
                inputField.setText("");
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    public void loginWindow() {
        JFrame frame = new JFrame("Login Form");
        frame.setLayout(new FlowLayout());

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");

        loginButton.addActionListener((ActionEvent e) -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                User user = userService.checkEmailAndPassword(email, password);
                if (user != null) {
                    chatWindow(user);
                    frame.dispose(); // close login window
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed!");
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        frame.add(emailLabel);
        frame.add(emailField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(loginButton);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Launch multiple instances for testing
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientDemo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        SwingUtilities.invokeLater(() -> {
            try {
                new ClientDemo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Add more if you want more windows
    }
}
