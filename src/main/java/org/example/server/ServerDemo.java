package org.example.server;
import org.example.rmi.ChatService;
import org.example.server.impl.ChatLogServiceImpl;
import org.example.server.impl.ChatServiceImpl;
import org.example.server.impl.UserServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerDemo {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(55545);
            registry.rebind("ChatService", new ChatServiceImpl());
            registry.rebind("UserService", new UserServiceImpl());
            registry.rebind("LogService", new ChatLogServiceImpl());
            registry.rebind("LogService", new ChatLogServiceImpl());
            System.out.println("Server started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}