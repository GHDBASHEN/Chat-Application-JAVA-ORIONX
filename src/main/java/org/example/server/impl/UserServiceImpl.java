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
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RemoteException("Error updating user", e);
        }
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

    //for register
    @Override
    public boolean registerUser(User user) throws RemoteException {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            throw new RemoteException("Error registering user", e);
        }

    }
}
