# Chat Application - JAVA ORIONX

A real-time chat application built with Java, featuring user/admin roles, group management, and persistent chat history.

 <!-- Add your screenshot path -->

## ✨ Features

### 👤 User Features
- **Registration System**  
  Create accounts with: Email, Username, Password, Nickname, Profile Picture
- **Profile Management**  
  Update account details and profile picture
- **Group Interaction**  
  - Subscribe/Unsubscribe from chat groups
  - Receive real-time notifications when groups become active

### 👑 Admin Features
- **Group Management**  
  - Create/Delete chat groups
  - Add/Remove users from groups
  - View all group memberships
- **User Moderation**  
  - Delete user accounts
  - Monitor user activity logs

### 💬 Chat Functionality
- **Real-Time Notifications**  
  - Visual alerts when groups activate
  - Join/Leave messages with timestamps
- **Message History**  
  - Automatic .txt file creation per group
  - Database logging with Hibernate
  ```txt
  [2023-12-01 14:30] [Admin] System: Group "Tech Talk" started
  [2023-12-01 14:31] Alice: Hello everyone!
