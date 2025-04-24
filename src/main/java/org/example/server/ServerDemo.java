package org.example.server;
import org.example.domain.*;
import org.example.rmi.ChatLogService;
import org.example.rmi.ChatService;
import org.example.rmi.UserService;
import org.example.server.impl.ChatLogServiceImpl;
import org.example.server.impl.ChatServiceImpl;
import org.example.server.impl.UserServiceImpl;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerDemo {
    public static void main(String[] args) {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(ChatUser.class);
            configuration.addAnnotatedClass(ChatGroup.class);
            configuration.addAnnotatedClass(ChatMessage.class);
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            ChatService chatService = new ChatServiceImpl(sessionFactory);
            UserService userService = new UserServiceImpl(sessionFactory);
            ChatLogService logService = new ChatLogServiceImpl();

            Registry registry = LocateRegistry.createRegistry(55545);
            registry.rebind("ChatService", chatService);
            registry.rebind("UserService", userService);
            registry.rebind("LogService", logService);

            new PreDefinedSql(sessionFactory).addDefaultGroupRow();


            System.out.println("RMI services are up and running on port 55545");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}