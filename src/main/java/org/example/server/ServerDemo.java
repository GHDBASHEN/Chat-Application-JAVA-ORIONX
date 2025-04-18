package org.example.server;
import org.example.rmi.ChatLogService;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;
import org.example.server.impl.ChatLogServiceImpl;
import org.example.server.impl.ChatServiceImpl;
import org.example.server.impl.UserServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerDemo {
    public static void main(String[] args) {
        try {
            ChatService chatService = new ChatServiceImpl();
            UserService userService = new UserServiceImpl();
            ChatLogService logService = new ChatLogServiceImpl();

            Registry registry = LocateRegistry.createRegistry(55545);
            registry.rebind("ChatService", chatService);
            registry.rebind("UserService", userService);
            registry.rebind("LogService", logService);

            System.out.println("RMI services are up and running on port 55545");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}