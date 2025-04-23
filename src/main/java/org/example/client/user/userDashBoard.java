package org.example.client.user;

import org.example.domain.ChatLog;
import org.example.domain.User;
import org.example.rmi.ChatLogService;
import org.example.rmi.ChatObserver;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;

import javax.swing.*;
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
    private JScrollPane groupList;
    private ChatService chatService;
    private UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;
    private JPanel groupButtonPanel; // panel inside groupList

    public userDashBoard(ChatService chatService, UserService userService, ChatLogService logService, ChatLog chatLog) {
        this.chatService = chatService;
        this.userService = userService;
        this.logService = logService;
        this.chatLog = chatLog;

        // display all groups START
        this.groupButtonPanel = new JPanel();
        groupButtonPanel.setLayout(new BoxLayout(groupButtonPanel, BoxLayout.Y_AXIS));
        groupList.setViewportView(groupButtonPanel);

        // Example buttons
        for (int i = 1; i <= 5; i++) {
            JButton groupButton = new JButton("Group " + i);
            groupButtonPanel.add(groupButton);
        }

        groupButtonPanel.revalidate();
        groupButtonPanel.repaint();
        // display all groups END
    }

    public void handle(User user) throws Exception {

        userName.setText("Hello, "+user.getUsername() + "\uD83D\uDE09");
        ChatObserver observer = new ChatObserver() {
            public void notifyNewMessage(String message) throws RemoteException {
                SwingUtilities.invokeLater(() -> textArea1.append(message + "\n"));
            }
        };

        // add observer to the list [add new user]
        ChatObserver stub = (ChatObserver) UnicastRemoteObject.exportObject(observer, 0);
        chatLog = logService.login(user.getUser_id());
        chatService.subscribe(user, stub, chatLog);


        JFrame frame = new JFrame("User Dashboard");
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
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
