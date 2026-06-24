package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        queryWithHibernateSessionFactory();
        queryWithJpaEntityManager();
        queryWithNativeSql();
        queryWithCriteriaApi();
    }

    public static void queryWithHibernateSessionFactory() {
        try (SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            List<User> users = session
                .createQuery("select u from User u order by u.id", User.class)
                .getResultList();

            System.out.println("Hibernate SessionFactory: all users");
            users.forEach(Main::printUser);
        }
    }

    public static void queryWithJpaEntityManager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        EntityManager entityManager = emf.createEntityManager();

        try {
            List<User> users = entityManager
                .createQuery(
                    "select u from User u where u.createdAt >= :createdAt order by u.createdAt desc",
                    User.class
                )
                .setParameter("createdAt", LocalDateTime.of(2024, 1, 1, 0, 0))
                .getResultList();

            System.out.println("JPA EntityManager JPQL: users created since 2024-01-01");
            users.forEach(Main::printUser);
        } finally {
            entityManager.close();
            emf.close();
        }
    }

    public static void queryWithNativeSql() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        EntityManager entityManager = emf.createEntityManager();

        try {
            List<User> users = entityManager
                .createNativeQuery("select * from users where email ilike :email order by id", User.class)
                .setParameter("email", "%example.com%")
                .getResultList();

            System.out.println("JPA EntityManager native SQL: users with example.com email");
            users.forEach(Main::printUser);
        } finally {
            entityManager.close();
            emf.close();
        }
    }

    public static void queryWithCriteriaApi() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        EntityManager entityManager = emf.createEntityManager();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            query
                .select(root)
                .where(cb.equal(root.get("username"), "john_doe"))
                .orderBy(cb.asc(root.get("id")));

            List<User> users = entityManager.createQuery(query).getResultList();

            System.out.println("JPA Criteria API: user by username");
            users.forEach(Main::printUser);
        } finally {
            entityManager.close();
            emf.close();
        }
    }

    private static void printUser(User user) {
        System.out.println(
            "ID: " + user.getId()
                + ", Username: " + user.getUsername()
                + ", Email: " + user.getEmail()
                + ", Created at: " + user.getCreatedAt()
        );
    }
}
