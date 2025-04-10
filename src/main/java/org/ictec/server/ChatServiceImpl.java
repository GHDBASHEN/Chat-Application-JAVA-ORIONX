package org.ictec.server;
import org.ictec.entities.*;
import org.ictec.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private ChatRoom chatRoom = new ChatRoom();

    public ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public User registerUser(User user) throws RemoteException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        }
    }

    @Override
    public User login(String email, String password) throws RemoteException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE email = :email AND password = :password", User.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .uniqueResult();
        }
    }

    @Override
    public void updateUser(User user) throws RemoteException {

    }

    @Override
    public void subscribeToChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public void unsubscribeFromChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public Chat createChat(Chat chat) throws RemoteException {
        return null;
    }

    @Override
    public void subscribeUserToChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public void unsubscribeUserFromChat(int userId, int chatId) throws RemoteException {

    }

    @Override
    public void removeUser(int userId) throws RemoteException {

    }

    @Override
    public void sendMessage(String message, User user) throws RemoteException {
        String formattedMsg = user.getNickName() + ": " + message + " [" + LocalDateTime.now() + "]";
        chatRoom.notifyObservers(formattedMsg);
    }

    @Override
    public void joinChat(User user) throws RemoteException {

    }

    @Override
    public void leaveChat(User user) throws RemoteException {

    }

    @Override
    public List<String> getActiveChats() throws RemoteException {
        return List.of();
    }

    // Implement other methods similarly
}
