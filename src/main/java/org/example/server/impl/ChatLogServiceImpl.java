package org.example.server.impl;

import org.example.domain.ChatLog;
import org.example.domain.User;
import org.example.rmi.ChatLogService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ChatLogServiceImpl extends UnicastRemoteObject implements ChatLogService {
    private final SessionFactory sessionFactory;

    public ChatLogServiceImpl() throws RemoteException {
        super();

        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(ChatLog.class); // Make sure to include ChatLog
        configuration.configure("hibernate.cfg.xml");
        this.sessionFactory = configuration.buildSessionFactory();
    }

    @Override
    public ChatLog login(int user_id) throws RemoteException {
        ChatLog chatLog = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            chatLog = new ChatLog();
            chatLog.setUser_id(user_id);
            chatLog.setStart_time(LocalDateTime.now());

            session.persist(chatLog); //save chat log
            transaction.commit();
            return chatLog;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error while logging in", e);
        }
    }

    @Override
    public ChatLog logout(int user_id) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            // Retrieve the active chat log for the user
            ChatLog chatLog = session.createQuery(
                            "FROM ChatLog WHERE user_id = :user_id AND end_time IS NULL ORDER BY start_time DESC",
                            ChatLog.class
                    )
                    .setParameter("user_id", user_id)
                    .setMaxResults(1)
                    .uniqueResult();

            if (chatLog != null) {
                chatLog.setEnd_time(LocalDateTime.now());
                session.update(chatLog); // Persist changes
                transaction.commit();
                System.out.println("User with ID " + user_id + " has been logged out.");
                return chatLog; // Return updated ChatLog
            } else {
                System.out.println("No active chat found for user with ID " + user_id);
                transaction.rollback();
                return null; // No active chat log found
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error while logging out", e);
        }
    }



    @Override
    public Boolean isUserOnline(int user_id) throws RemoteException {
        System.out.println(user_id);
        System.out.println("Checking if user is online...");
        try (Session session = sessionFactory.openSession()) {
            ChatLog chatLog = session.createQuery(
                            "FROM ChatLog WHERE user_id = :user_id AND end_time IS NULL ORDER BY start_time DESC",
                            ChatLog.class
                    )
                    .setParameter("user_id", user_id)
                    .setMaxResults(1)
                    .uniqueResult();


            System.out.println("retur: " + (chatLog != null));
            return chatLog != null;
        }
    }

    public List<ChatLog> getChatLogsWithNullEndTime() {
        try (Session session = sessionFactory.openSession()) {
            // HQL query to fetch all ChatLog entities where end_time is NULL
            List<ChatLog> chatLogs = session.createQuery(
                            "FROM ChatLog c WHERE c.end_time IS NULL",
                            ChatLog.class
                    )
                    .list(); // Retrieve all matching results as a list

            System.out.println("Retrieved chat logs: " + chatLogs);
            return chatLogs;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // Return an empty list if an exception occurs
        }
    }

    public ChatLog updateChatLog(ChatLog chatLog) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            chatLog.setEnd_time(LocalDateTime.now());

            session.merge(chatLog); // update chat log
            transaction.commit();
            return chatLog;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error while logging in", e);
        }
    }


    @Override
    public ChatLog login(Long userId, Long chatId) throws RemoteException {
        return null;
    }

    @Override
    public ChatLog logout(Long userId, Long chatId) throws RemoteException {
        return null;
    }

    @Override
    public boolean isUserOnline(Long userId, Long chatId) throws RemoteException {
        return false;
    }
}
