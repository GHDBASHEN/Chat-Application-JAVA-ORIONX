package org.example.server.impl;

import org.example.domain.User;
import org.example.rmi.UserService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class UserServiceImpl extends UnicastRemoteObject implements UserService {
    Session session = null;
    public UserServiceImpl() throws RemoteException {
        super();
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(User.class);
        configuration.configure("hibernate.cfg.xml");
        SessionFactory factory = configuration.buildSessionFactory();
        session = factory.openSession();
    }

    @Override
    public List<User> getAllUsers() throws RemoteException {
        try {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching users", e);
        }
    }

    @Override
    public void deleteUser(Long userId) throws RemoteException {

    }

    @Override
    public User getUser(int id) throws RemoteException {
        return null;
    }

    @Override
    public User checkEmailAndPassword(String email, String password) throws RemoteException {
        // check user exists using email
        User user = (User) session.createQuery("FROM User WHERE email = :email AND password = :password")
                .setParameter("email", email)
                .setParameter("password", password)
                .uniqueResult();
        return user;
    }


}
