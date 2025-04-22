package org.example.server.impl;

import org.example.domain.User;
import org.example.rmi.UserService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UserServiceImpl extends UnicastRemoteObject implements UserService {
    private final SessionFactory sessionFactory;  // Proper SessionFactory

    public UserServiceImpl(SessionFactory sessionFactory) throws RemoteException {
        super();
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void updateUser(User user) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RemoteException("Error updating user", e);
        }
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
    public void deleteUser(int userId) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                session.delete(user);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RemoteException("Error deleting user", e);
        }
    }

    @Override
    public User getUser(int id) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            throw new RemoteException("Error fetching user", e);
        }
    }

    @Override
    public User checkEmailAndPassword(String email, String password) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE email = :email AND password = :password", User.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RemoteException("Authentication error", e);
        }
    }
}
