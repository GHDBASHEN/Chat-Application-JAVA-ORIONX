package org.example.server.impl;
import org.example.domain.*;
import org.example.rmi.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private List<ChatObserver> observers = new ArrayList<>();
    private final SessionFactory sessionFactory;
    public ChatServiceImpl(SessionFactory sessionFactory) throws RemoteException {
        this.sessionFactory = sessionFactory;
    }
    @Override
    public User getUserByUsername(String username) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RemoteException("Error finding user", e);
        }
    }

    // ChatServiceImpl.java
    @Override
    public void createChat(ChatGroup chat) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(chat);
            tx.commit();
            System.out.println("Chat created: " + chat.getChatName()); // Log success
        } catch (Exception e) {
            System.err.println("Error creating chat: " + e.getMessage()); // Log error
            throw new RemoteException("Error creating chat", e);
        }
    }

    @Override
    public void subscribeToChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public void unsubscribeFromChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public List<Message> getChatMessages(int chatId) throws RemoteException {
        return List.of();
    }

    @Override
    public void sendMessage(Message message) throws RemoteException {

    }

    @Override
    public List<ChatGroup> getAllChats() throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "SELECT c FROM ChatGroup c LEFT JOIN FETCH c.admin",
                    ChatGroup.class
            ).list();
        } catch (Exception e) {
            throw new RemoteException("Error fetching chats", e);
        }
    }

    @Override
    public void sendMessage(String message, User sender, int chatId) throws RemoteException {
        // Save to DB (add your Hibernate code here)

        notifyAllObservers(sender.getNickname() + ": " + message, chatId);
    }

    @Override
    public void subscribe(User user, ChatObserver observer, ChatLog chatLog, int chatId) throws RemoteException {
        observers.add(observer);

        // Get the formatted time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = chatLog.getStart_time().format(formatter);
        notifyAllObservers(user.getNickname() + " joined: "+ formattedTime, chatId); // Send message to all observers
    }

    @Override
    public void unsubscribe(User user, ChatObserver observer, ChatLog chatLog, int chatId) throws RemoteException {
        observers.remove(observer);

        // Get the formatted time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = chatLog.getEnd_time().format(formatter);
        notifyAllObservers(user.getNickname() + " left: " + formattedTime, chatId);
    }

    @Override
    public List<User> getAllUsers() throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            throw new RemoteException("Error fetching users", e);
        }
    }

    @Override
    public void addUserToGroup(int userId, int groupId) throws RemoteException {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);
            ChatGroup chatGroup = session.get(ChatGroup.class, groupId);

            if (user == null || chatGroup == null) {
                throw new RemoteException("User or Chat Group not found");
            }

            ChatUserId id = new ChatUserId(userId, groupId);

            // Avoid duplicate entry
            ChatUser existing = session.get(ChatUser.class, id);
            if (existing != null) {
                throw new RemoteException("User is already in the group");
            }

            ChatUser chatUser = new ChatUser();
            chatUser.setId(id);
            chatUser.setUser(user);
            chatUser.setChatGroup(chatGroup);

            session.persist(chatUser);
            tx.commit();

            System.out.println("User added to group successfully");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RemoteException("Error adding user to group", e);
        }
    }


    private void notifyAllObservers(String message, int chatId) {
        observers.forEach(obs -> {
            try { obs.notifyNewMessage(message, chatId); }
            catch (RemoteException e) { e.printStackTrace(); }
        });

    }
}