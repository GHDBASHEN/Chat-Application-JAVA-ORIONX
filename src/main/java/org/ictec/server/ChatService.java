package org.ictec.server;
import org.ictec.entities.Chat;
import org.ictec.entities.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    // User Actions
    User registerUser(User user) throws RemoteException;
    User login(String email, String password) throws RemoteException;
    void updateUser(User user) throws RemoteException;
    void subscribeToChat(int userId, int chatId) throws RemoteException;
    void unsubscribeFromChat(int userId, int chatId) throws RemoteException;

    // Admin Actions
    Chat createChat(Chat chat) throws RemoteException;
    void subscribeUserToChat(int userId, int chatId) throws RemoteException;
    void unsubscribeUserFromChat(int userId, int chatId) throws RemoteException;
    void removeUser(int userId) throws RemoteException;

    // Chat Actions
    void sendMessage(String message, User user) throws RemoteException;
    void joinChat(User user) throws RemoteException;
    void leaveChat(User user) throws RemoteException;
    List<String> getActiveChats() throws RemoteException;
}
