package org.example.server.impl;

import org.example.rmi.ChatObserver;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ChatObserverImpl extends UnicastRemoteObject implements ChatObserver {
    private final JTextArea chatArea;
    public ChatObserverImpl(JTextArea chatArea) throws RemoteException {
        this.chatArea = chatArea;
    }

    @Override
    public void notifyNewMessage(String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }
}
