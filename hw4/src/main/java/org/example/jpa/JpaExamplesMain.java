package org.example.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@Configuration
@EnableTransactionManagement
public class JpaExamplesMain {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(JpaExamplesMain.class);

        EntityManager entityManager = context.getBean(EntityManager.class);
        PlatformTransactionManager transactionManager = context.getBean(PlatformTransactionManager.class);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        clearUsers(entityManager, transactionTemplate);

        List<User> users = List.of(
            new User("John Doe" + new Random().nextInt(), "Test"),
            new User("Jane Doe" + new Random().nextInt(), "Test"),
            new User("Jane Doe" + new Random().nextInt(), "Test")
        );

//        insertUser(entityManager, transactionTemplate, users.get(0));
//        insertMultipleUsers(entityManager, transactionTemplate, users);
//        insertWithTransaction(entityManager, transactionTemplate, users);
//        insertWithTransactionRollback(entityManager, transactionTemplate, users);
//        testReadNotCommited(entityManager, transactionManager, users);
//        notDirtyRead(entityManager, transactionManager);
//        dirtyRead(entityManager, transactionManager);
//        repeatableRead(entityManager, transactionManager);
//        notRepeatableRead(entityManager, transactionManager);//         phantomRead(entityManager, transactionManager);
//        notPhantomRead(entityManager, transactionManager);
//        anomalyExample(entityManager, transactionManager);
//        notAnomalyExample(entityManager, transactionManager);

        propagationRequired(entityManager, transactionManager);
//        propagationSupport(entityManager, transactionManager);
 //       propagationRequiredNew(entityManager, transactionManager);

//        countUsers(entityManager);
        printUsers(entityManager);
        clearUsers(entityManager, transactionTemplate);
    }

    static void clearUsers(EntityManager entityManager, TransactionTemplate transactionTemplate) {
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });
    }

    static void printUsers(EntityManager entityManager) {
        entityManager.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
            .getResultList()
            .forEach(System.out::println);
    }

    static void countUsers(EntityManager entityManager) {
        System.out.println(entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult());
    }

    static void concatUsersName(EntityManager entityManager) {
        System.out.println(entityManager.createNativeQuery("SELECT string_agg(name, ', ') FROM users").getSingleResult());
    }

    static void insertUser(EntityManager entityManager, TransactionTemplate transactionTemplate, User user) {
        transactionTemplate.execute(status -> {
            entityManager.persist(user);
            return null;
        });
    }

    static void insertMultipleUsers(EntityManager entityManager,
                                    TransactionTemplate transactionTemplate,
                                    List<User> users) {
        transactionTemplate.execute(status -> {
            users.forEach(entityManager::persist);
            return null;
        });
    }

    static void insertWithTransaction(EntityManager entityManager,
                                      TransactionTemplate transactionTemplate,
                                      List<User> users) {
        transactionTemplate.execute(status -> {
            users.forEach(user -> {
                entityManager.persist(user);
                entityManager.flush();
                printUsers(entityManager);
                System.out.println("-----------");
            });
            return null;
        });
    }

    static void insertWithTransactionRollback(EntityManager entityManager,
                                              TransactionTemplate transactionTemplate,
                                              List<User> users) {
        transactionTemplate.execute(status -> {
            users.forEach(user -> {
                entityManager.persist(user);
                entityManager.flush();
                printUsers(entityManager);
                System.out.println("-----------");
                status.setRollbackOnly();
            });
            return null;
        });
    }

    static void testReadNotCommited(EntityManager entityManager,
                                    PlatformTransactionManager transactionManager,
                                    List<User> users) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        Thread threadInsert = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                users.forEach(user -> {
                    entityManager.persist(user);
                    entityManager.flush();
                    sleep(1_000);
                });
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(entityManager);
                sleep(100);
            }
        });

        join(threadInsert);
    }

    static void notDirtyRead(EntityManager entityManager,
                             PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        insertUser(entityManager, template, new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                User luci = findUserByName(entityManager, "Luci");
                luci.setAbout(luci.getAbout() + " Test");
                entityManager.flush();
                sleep(3_000);
                status.setRollbackOnly();
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                sleep(1_000);
                printUsers(entityManager);
                return null;
            });
        });

        join(thread1, thread2);
    }

    static void dirtyRead(EntityManager entityManager,
                          PlatformTransactionManager transactionManager) {
        TransactionTemplate template = transactionTemplate(transactionManager, TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        insertUser(entityManager, new TransactionTemplate(transactionManager), new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                User luci = findUserByName(entityManager, "Luci");
                luci.setAbout(luci.getAbout() + " Test");
                entityManager.flush();
                sleep(3_000);
                status.setRollbackOnly();
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            printUsers(entityManager);
        });

        join(thread1, thread2);

        // PostgreSQL treats READ_UNCOMMITTED as READ_COMMITTED, so dirty read is not visible.
    }

    static void repeatableRead(EntityManager entityManager,
                               PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        insertUser(entityManager, template, new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            template.execute(status -> {
                System.out.println("Start thread 1");
                User luci = findUserByName(entityManager, "Luci");
                luci.setAbout(luci.getAbout() + " Test");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 2");
                printUsers(entityManager);
                entityManager.clear();
                sleep(3_000);
                printUsers(entityManager);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        join(thread1, thread2);
    }

    static void notRepeatableRead(EntityManager entityManager,
                                  PlatformTransactionManager transactionManager) {
        TransactionTemplate template = transactionTemplate(transactionManager, TransactionDefinition.ISOLATION_REPEATABLE_READ);
        insertUser(entityManager, new TransactionTemplate(transactionManager), new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            template.execute(status -> {
                System.out.println("Start thread 1");
                User luci = findUserByName(entityManager, "Luci");
                luci.setAbout(luci.getAbout() + " Test");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 2");
                printUsers(entityManager);
                entityManager.clear();
                sleep(3_000);
                printUsers(entityManager);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        join(thread1, thread2);
    }

    static void phantomRead(EntityManager entityManager,
                            PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        insertUser(entityManager, template, new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            template.execute(status -> {
                System.out.println("Start thread 1");
                entityManager.persist(new User("Mark", "Test"));
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 2");
                concatUsersName(entityManager);
                sleep(3_000);
                concatUsersName(entityManager);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        join(thread1, thread2);
    }

    static void notPhantomRead(EntityManager entityManager,
                               PlatformTransactionManager transactionManager) {
        TransactionTemplate template = transactionTemplate(transactionManager, TransactionDefinition.ISOLATION_REPEATABLE_READ);
        insertUser(entityManager, new TransactionTemplate(transactionManager), new User("Luci", "Test"));

        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            template.execute(status -> {
                System.out.println("Start thread 1");
                entityManager.persist(new User("Mark", "Test"));
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 2");
                concatUsersName(entityManager);
                sleep(3_000);
                concatUsersName(entityManager);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        join(thread1, thread2);
    }

    static void anomalyExample(EntityManager entityManager,
                               PlatformTransactionManager transactionManager) {
        prepareCalculator(entityManager, new TransactionTemplate(transactionManager));
        TransactionTemplate template = transactionTemplate(transactionManager, TransactionDefinition.ISOLATION_REPEATABLE_READ);

        Thread thread1 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 1");
                sumCalculator(entityManager, 1);
                sleep(2_000);
                entityManager.persist(new Calculator(2, 30));
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            template.execute(status -> {
                System.out.println("Start thread 2");
                sumCalculator(entityManager, 2);
                sleep(2_000);
                entityManager.persist(new Calculator(1, 300));
                return null;
            });
            System.out.println("Finish thread 2");
        });

        join(thread1, thread2);
        printCalculator(entityManager);
    }

    static void notAnomalyExample(EntityManager entityManager,
                                  PlatformTransactionManager transactionManager) {
        prepareCalculator(entityManager, new TransactionTemplate(transactionManager));
        TransactionTemplate template = transactionTemplate(transactionManager, TransactionDefinition.ISOLATION_SERIALIZABLE);

        Thread thread1 = Thread.startVirtualThread(() -> {
            try {
                sleep(1_000);
                template.execute(status -> {
                    System.out.println("Start thread 1");
                    sumCalculator(entityManager, 1);
                    sleep(2_000);
                    entityManager.persist(new Calculator(2, 30));
                    return null;
                });
                System.out.println("Finish thread 1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            try {
                sleep(1_000);
                template.execute(status -> {
                    System.out.println("Start thread 2");
                    sumCalculator(entityManager, 2);
                    sleep(2_000);
                    entityManager.persist(new Calculator(1, 300));
                    return null;
                });
                System.out.println("Finish thread 2");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        join(thread1, thread2);
        printCalculator(entityManager);
    }

    static void propagationRequired(EntityManager entityManager,
                                    PlatformTransactionManager transactionManager) {
        TransactionTemplate required = propagationTemplate(transactionManager, TransactionDefinition.PROPAGATION_REQUIRED);
        Thread threadInsert = Thread.startVirtualThread(() -> {
            required.execute(status -> {
                entityManager.persist(new User("Luci", "Test"));
                entityManager.flush();
                sleep(2_000);
                required.execute(status2 -> {
                    entityManager.persist(new User("Mark", "Test"));
                    entityManager.flush();
                    return null;
                });
                sleep(2_000);
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(entityManager);
                sleep(100);
            }
        });

        join(threadInsert);
    }

    static void propagationSupport(EntityManager entityManager,
                                   PlatformTransactionManager transactionManager) {
        Thread threadInsert = Thread.startVirtualThread(() -> {
            insertUser(entityManager, new TransactionTemplate(transactionManager), new User("Luci", "Test"));
            sleep(2_000);
            insertUser(entityManager, new TransactionTemplate(transactionManager), new User("Mark", "Test"));
            sleep(2_000);
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(entityManager);
                sleep(100);
            }
        });

        join(threadInsert);
    }

    static void propagationRequiredNew(EntityManager entityManager,
                                       PlatformTransactionManager transactionManager) {
        TransactionTemplate requiresNew = propagationTemplate(transactionManager, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Thread threadInsert = Thread.startVirtualThread(() -> {
            requiresNew.execute(status -> {
                entityManager.persist(new User("Luci", "Test"));
                entityManager.flush();
                sleep(2_000);
                requiresNew.execute(status2 -> {
                    entityManager.persist(new User("Mark", "Test"));
                    entityManager.flush();
                    return null;
                });
                sleep(2_000);
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(entityManager);
                sleep(100);
            }
        });

        join(threadInsert);
    }

    static User findUserByName(EntityManager entityManager, String name) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class)
            .setParameter("name", name)
            .setMaxResults(1)
            .getSingleResult();
    }

    static void prepareCalculator(EntityManager entityManager, TransactionTemplate transactionTemplate) {
        transactionTemplate.execute(status -> {
            entityManager.createQuery("DELETE FROM Calculator").executeUpdate();
            entityManager.persist(new Calculator(1, 10));
            entityManager.persist(new Calculator(1, 20));
            entityManager.persist(new Calculator(2, 100));
            entityManager.persist(new Calculator(2, 200));
            return null;
        });
    }

    static Long sumCalculator(EntityManager entityManager, int classNumber) {
        Long sum = entityManager.createQuery(
                "SELECT SUM(c.value) FROM Calculator c WHERE c.classNumber = :classNumber",
                Long.class
            )
            .setParameter("classNumber", classNumber)
            .getSingleResult();
        System.out.println(sum);
        return sum;
    }

    static void printCalculator(EntityManager entityManager) {
        entityManager.createQuery("SELECT c FROM Calculator c ORDER BY c.id", Calculator.class)
            .getResultList()
            .forEach(row -> System.out.println(row.getClassNumber() + " " + row.getValue()));
    }

    static TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager, int isolationLevel) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(isolationLevel);
        return template;
    }

    static TransactionTemplate propagationTemplate(PlatformTransactionManager transactionManager, int propagationBehavior) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(propagationBehavior);
        return template;
    }

    static void join(Thread... threads) {
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:15434/mydatabase");
        dataSource.setUsername("admin");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("org.example.jpa");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        Properties props = new Properties();
        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        em.setJpaProperties(props);

        return em;
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
