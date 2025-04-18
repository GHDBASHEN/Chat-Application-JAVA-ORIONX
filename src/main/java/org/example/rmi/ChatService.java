// ChatService.java (new interface)
package org.example.rmi;
import org.example.domain.Chat;
import org.example.domain.ChatLog;
import org.example.domain.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    void createChat(Chat chat) throws RemoteException;
    List<Chat> getAllChats() throws RemoteException;
//    void subscribeUserToChat(int userId, int chatId) throws RemoteException;
//    void unsubscribeUserFromChat(int userId, int chatId) throws RemoteException;
//    void sendMessageToChat(int chatId, String message) throws RemoteException;
    void sendMessage(String message, User sender) throws RemoteException;
    void subscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException;
    void unsubscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException;
}