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
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
    private JLabel profilePicLabel; // Added for profile picture
    private ChatService chatService;
    private UserService userService;
    private ChatLogService logService;
    private ChatLog chatLog;

    // Profile update components
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nicknameField;
    private JTextField profilePictureField;
    private JButton saveProfileButton;
    private User currentUser;

    private void initializeProfileUpdateUI() {
        // Create components
        emailField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        nicknameField = new JTextField(20);
        profilePictureField = new JTextField(20);
        profilePictureField.setEditable(false); // Make it read-only
        JButton chooseFileButton = new JButton("Choose File");
        saveProfileButton = new JButton("Save Profile");

        // Pre-populate fields with current user data
        emailField.setText(currentUser.getEmail());
        usernameField.setText(currentUser.getUsername());
        passwordField.setText(currentUser.getPassword());
        nicknameField.setText(currentUser.getNickname());
        profilePictureField.setText(currentUser.getProfile_picture());

        // Add action listener to the choose file button
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                int result = fileChooser.showOpenDialog(main);
                if (result == JFileChooser.APPROVE_OPTION) {
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    profilePictureField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

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
        updatePane.add(new JLabel("Confirm Password:"), gbc);
        // Set the echo character for confirmPasswordField to match passwordField
        confirmPasswordField.setEchoChar('•'); // Standard bullet character


        // Create a layered pane for the confirm password field with eye icon inside
        JLayeredPane layeredPane = new JLayeredPane();

        // Get the preferred size of the passwordField to match sizes
        Dimension passwordSize = passwordField.getPreferredSize();
        int fieldWidth = passwordSize.width;
        int fieldHeight = passwordSize.height;
        layeredPane.setPreferredSize(passwordSize);

        // Add the password field to the layered pane with matching size
        confirmPasswordField.setBounds(0, 0, fieldWidth, fieldHeight);
        layeredPane.add(confirmPasswordField, JLayeredPane.DEFAULT_LAYER);

        // Create eye icon button for password visibility with standard icon
        JButton viewPasswordButton = new JButton("-"); // Closed eye icon (password hidden)
        viewPasswordButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewPasswordButton.setFocusPainted(false);
        viewPasswordButton.setBorderPainted(false);
        viewPasswordButton.setContentAreaFilled(false);
        viewPasswordButton.setToolTipText("Show/Hide Password");

        // Position the button inside the password field at the right corner
        int buttonWidth = 20;
        int buttonHeight = 20;
        int rightPadding = 5;
        viewPasswordButton.setBounds(fieldWidth - buttonWidth - rightPadding, 
                                    (fieldHeight - buttonHeight) / 2, 
                                    buttonWidth, buttonHeight);

        viewPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle password visibility
                if (confirmPasswordField.getEchoChar() == 0) {
                    // Currently showing, switch to hiding
                    confirmPasswordField.setEchoChar('•'); // Standard bullet character
                    viewPasswordButton.setText("-"); // Closed eye icon (password hidden)
                } else {
                    // Currently hiding, switch to showing
                    confirmPasswordField.setEchoChar((char) 0);
                    viewPasswordButton.setText("O"); // Open eye (representing "visible")
                }
            }
        });

        layeredPane.add(viewPasswordButton, JLayeredPane.PALETTE_LAYER);

        gbc.gridx = 1;
        updatePane.add(layeredPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        updatePane.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        updatePane.add(nicknameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        updatePane.add(new JLabel("Profile Picture:"), gbc);

        // Create a panel for the profile picture field and choose file button
        JPanel profilePicturePanel = new JPanel(new BorderLayout(5, 0));
        profilePicturePanel.add(profilePictureField, BorderLayout.CENTER);
        profilePicturePanel.add(chooseFileButton, BorderLayout.EAST);

        gbc.gridx = 1;
        updatePane.add(profilePicturePanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        updatePane.add(saveProfileButton, gbc);

        // Add action listener to save button
        saveProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get password values
                    String password = new String(passwordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());

                    // Check if passwords match
                    if (!password.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(main, 
                            "Passwords do not match!", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Update user object with new values
                    currentUser.setEmail(emailField.getText());
                    currentUser.setUsername(usernameField.getText());
                    currentUser.setPassword(password);
                    currentUser.setNickname(nicknameField.getText());

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
                            String fileName = currentUser.getUser_id() + "_" + sourceFile.getName();
                            java.io.File destFile = new java.io.File(profilePicsDir, fileName);

                            // Copy the file
                            java.nio.file.Files.copy(
                                sourceFile.toPath(),
                                destFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );

                            // Update the path in the user object to the copied file
                            String newPath = destFile.getAbsolutePath();
                            currentUser.setProfile_picture(newPath);
                            // Update the UI to show the new path
                            profilePictureField.setText(newPath);
                        } catch (java.io.IOException ioEx) {
                            System.err.println("Error copying profile picture: " + ioEx.getMessage());
                            // If file copy fails, still save the original path
                            currentUser.setProfile_picture(selectedFilePath);
                        }
                    } else {
                        currentUser.setProfile_picture(selectedFilePath);
                    }

                    // Call service to update user
                    userService.updateUser(currentUser);

                    // Update UI to reflect changes
                    userName.setText("Hello, " + currentUser.getUsername() + "\uD83D\uDE09");

                    // Update profile picture
                    updateProfilePicture();

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
        GradientPaint gradient = new GradientPaint(0, 0, new Color(100, 100, 255), width, height, new Color(200, 200, 255));
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
        String text = "?";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2.drawString(text, (width - textWidth) / 2, (height + textHeight / 2) / 2);

        g2.dispose();

        return new ImageIcon(defaultImage);
    }

    // Method to update the profile picture in the UI
    private void updateProfilePicture() {
        String profilePicPath = currentUser.getProfile_picture();
        if (profilePicPath != null && !profilePicPath.isEmpty()) {
            // Create a circular profile picture with 40x40 dimensions
            ImageIcon profileIcon = createCircularProfilePicture(profilePicPath, 40, 40);
            profilePicLabel.setIcon(profileIcon);
        } else {
            // Set a default profile picture
            profilePicLabel.setIcon(createDefaultProfilePicture(40, 40));
        }
    }

    public void handle(User user) throws Exception {
        this.currentUser = user;

        JFrame frame = new JFrame("User Dashboard");
        frame.setContentPane(main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        // Now UI components should be initialized
        userName.setText("Hello, "+user.getUsername() + "\uD83D\uDE09");

        // Create and add profile picture label if it doesn't exist
        if (profilePicLabel == null) {
            profilePicLabel = new JLabel();
            // Find the panel that contains the userName label
            Container parent = userName.getParent();
            if (parent instanceof JPanel) {
                JPanel panel = (JPanel) parent;
                // Change layout to FlowLayout to place components side by side
                panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
                // Remove the userName label and add both components in the desired order
                panel.remove(userName);
                panel.add(profilePicLabel);
                panel.add(userName);
                panel.revalidate();
                panel.repaint();
            }
        }

        // Update the profile picture
        updateProfilePicture();

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
