package org.example.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@SpringBootApplication
public class LockMain {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.jpa");

        EntityManager entityManager = context.getBean(EntityManager.class);
        TransactionTemplate transactionTemplate = context.getBean(TransactionTemplate.class);

        pessimisticLock(entityManager, transactionTemplate);
    }

    private static void optimisticLock(EntityManager entityManager, TransactionTemplate transactionTemplate) {
        Address address = new Address("address1");
        transactionTemplate.execute(status -> {
            entityManager.persist(address);
            return null;
        });
        Thread thread1 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                Address address1 = entityManager.find(Address.class, address.getId(), LockModeType.OPTIMISTIC);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                address1.setAddress("address2");
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                Address address1 = entityManager.find(Address.class, address.getId(), LockModeType.OPTIMISTIC);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                address1.setAddress("address3");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        });
        try {
            thread1.join();
            thread2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(entityManager.find(Address.class, address.getId()));
    }

    private static void pessimisticLock(EntityManager entityManager, TransactionTemplate transactionTemplate) {
        Address address = new Address("address1");
        transactionTemplate.execute(status -> {
            entityManager.persist(address);
            return null;
        });
        Thread thread1 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                Address address1 = entityManager.find(Address.class, address.getId(), LockModeType.PESSIMISTIC_WRITE);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                address1.setAddress("address2");
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Address address1 = entityManager.find(Address.class, address.getId(), LockModeType.PESSIMISTIC_WRITE);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                address1.setAddress("address3");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        });
        try {
            thread1.join();
            thread2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(entityManager.find(Address.class, address.getId()));
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://127.0.0.1:15432/mydatabase");
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
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        em.setJpaProperties(props);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
