// ChatService.java (new interface)
package org.example.rmi;
import org.example.domain.ChatLog;
import org.example.domain.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatService extends Remote {
    void sendMessage(String message, User sender) throws RemoteException;
    void subscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException;
    void unsubscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException;
}