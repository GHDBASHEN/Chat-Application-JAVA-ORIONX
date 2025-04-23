package org.example.rmi;

import org.example.domain.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// SubscriptionService.java
public interface SubscriptionService extends Remote {
    boolean isSubscribed(int userId, int chatId) throws RemoteException;
    List<User> getSubscribedUsers(int chatId) throws RemoteException;
}