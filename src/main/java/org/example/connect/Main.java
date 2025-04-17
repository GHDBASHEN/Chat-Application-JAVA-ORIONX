package org.example.connect;

import org.example.domain.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) {
        User user = new User();
        user.setUser_id(2);
        user.setEmail("2@example.com");
        user.setUsername("Namal");
        user.setPassword("password2");
        user.setNickname("nami");
        user.setProfile_picture("profile_picture");
        user.setRole("user");

        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(User.class);
        configuration.configure("hibernate.cfg.xml");
        SessionFactory factory = configuration.buildSessionFactory();
        Session session = factory.openSession();

        // add raw (transaction need when saving new data not for view)
        // Transaction transaction = session.beginTransaction();
        // session.persist(user);
        // transaction.commit();

        // get raw ( for get not used transaction )
        // User user1 = session.get(User.class, 2); // lazy fetching
        // User user1 = session.byId(User.class).getReference(2); // eager fetching
        // System.out.println(user1);

        // update raw
        // Transaction transaction = session.beginTransaction();
        // session.merge(user);
        // transaction.commit();

        // delete raw
         User user1 = session.get(User.class, 3);
         Transaction transaction = session.beginTransaction();
         session.remove(user1);
         transaction.commit();

        session.close();
        factory.close();
    }
}