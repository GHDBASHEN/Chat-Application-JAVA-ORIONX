package org.example.domain;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class PreDefinedSql {

    private final SessionFactory sessionFactory;

    public PreDefinedSql(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addDefaultGroupRow() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            Long count = (Long) session.createQuery("SELECT COUNT(c) FROM ChatGroup c").uniqueResult();
            if (count == 0) {
                ChatGroup newch = new ChatGroup();
                newch.setChatName("Public Chat");
                newch.setDescription("chat with everyone");
                newch.setAdmin(null);

                session.persist(newch);
            }

            session.getTransaction().commit();
        }
    }

}
