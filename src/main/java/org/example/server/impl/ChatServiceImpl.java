package org.example.server.impl;
import org.example.domain.*;
import org.example.rmi.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private List<ChatObserver> observers = new ArrayList<>();

    public ChatServiceImpl() throws RemoteException {}

    @Override
    public void sendMessage(String message, User sender) throws RemoteException {
        // Save to DB (add your Hibernate code here)

        notifyAllObservers(sender.getNickname() + ": " + message);
    }

    @Override
    public void subscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException {
        observers.add(observer);

        // Get the formatted time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = chatLog.getStart_time().format(formatter);
        notifyAllObservers(user.getNickname() + " joined: "+ formattedTime); // Send message to all observers
    }

    @Override
    public void unsubscribe(User user, ChatObserver observer, ChatLog chatLog) throws RemoteException {
        observers.remove(observer);

        // Get the formatted time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = chatLog.getEnd_time().format(formatter);
        notifyAllObservers(user.getNickname() + " left: " + formattedTime);
    }

    private void notifyAllObservers(String message) {
        observers.forEach(obs -> {
            try { obs.notifyNewMessage(message); }
            catch (RemoteException e) { e.printStackTrace(); }
        });

    }
}