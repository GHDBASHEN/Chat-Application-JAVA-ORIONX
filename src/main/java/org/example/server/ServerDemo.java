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
import java.rmi.server.UnicastRemoteObject;

public class ServerDemo {
    private static Registry registry;
    private static SessionFactory sessionFactory;
    private static ChatServiceImpl chatService;
    private static UserServiceImpl userService;
    private static ChatLogServiceImpl logService;

    public static void startServer() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(ChatUser.class);
            configuration.addAnnotatedClass(ChatGroup.class);
            configuration.addAnnotatedClass(ChatMessage.class);
            configuration.addAnnotatedClass(ChatLog.class);
            sessionFactory = configuration.buildSessionFactory();

            chatService = new ChatServiceImpl(sessionFactory);
            userService = new UserServiceImpl(sessionFactory);
            logService = new ChatLogServiceImpl(sessionFactory);

            registry = LocateRegistry.createRegistry(55545);
            registry.rebind("ChatService", chatService);
            registry.rebind("UserService", userService);
            registry.rebind("LogService", logService);

            new PreDefinedSql(sessionFactory).addDefaultGroupRow();
            System.out.println("RMI services are up and running on port 55545");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        try {
            if (registry != null) {
                registry.unbind("ChatService");
                registry.unbind("UserService");
                registry.unbind("LogService");
                UnicastRemoteObject.unexportObject(registry, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (chatService != null) {
                UnicastRemoteObject.unexportObject(chatService, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (userService != null) {
                UnicastRemoteObject.unexportObject(userService, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (logService != null) {
                UnicastRemoteObject.unexportObject(logService, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}