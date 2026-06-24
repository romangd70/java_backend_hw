package org.example.jpa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
public class JpaMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.jpa");

        UserRepository userRepository = context.getBean(UserRepository.class);

        userRepository.deleteAll(); // Очистить таблицу перед вставкой

        User user1 = new User();
        user1.setName("John");
        user1.setAbout("Developer");

        User user2 = new User();
        user2.setName("Johny");
        user2.setAbout("Designer");

        User user3 = new User();
        user3.setName("Alice");
        user3.setAbout("John");

        User user4 = new User();
        user4.setName("Bob");
        user4.setAbout("Manager");

        User user5 = new User();
        user5.setName("Johnny");
        user5.setAbout("Intern");

        // Сохраняем пользователей
        user1 = userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        userRepository.save(user5);

        // 1. Query method — findByName
        System.out.println("1. findByName(\"John\")");
        List<User> usersByName = userRepository.findByName("John");
        usersByName.forEach(System.out::println);

        // 2. Query method — findByNameStartingWithAndIdLessThan
        System.out.println("\n2. findByNameStartingWithAndIdLessThan(\"J\", 100)");
        List<User> usersByPrefixAndId = userRepository.findByNameStartingWithAndIdLessThan("J", 100L);
        usersByPrefixAndId.forEach(System.out::println);

        // 3. JPQL — searchByNameUsingJPQL
        System.out.println("\n3. searchByNameUsingJPQL(\"John\")");
        List<User> usersByJPQL = userRepository.searchByNameUsingJPQL("John");
        usersByJPQL.forEach(System.out::println);

        // 4. Native SQL — searchByNameUsingNativeQuery
        System.out.println("\n4. searchByNameUsingNativeQuery(\"John\")");
        List<User> usersByNativeQuery = userRepository.searchByNameUsingNativeQuery("John");
        usersByNativeQuery.forEach(System.out::println);

        // 5. JPQL с optional критериями — searchByOptionalCriteria
        System.out.println("\n5. searchByOptionalCriteria(\"John\", null)");
        List<User> usersByOptionalCriteria = userRepository.searchByOptionalCriteria("John", null);
        usersByOptionalCriteria.forEach(System.out::println);

        // 6. Projection — findUserNameById
        System.out.println("\n6. findUserNameById(1L)");
        UserRepository.UserNameOnly userNameOnly = userRepository.findUserNameById(user1.getId());
        System.out.println(userNameOnly.getName());

        // 7. Paging and Sorting — findByName pageable
        System.out.println("\n7. findByName(\"John\", Pageable)");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<User> userPage = userRepository.findByName("John", pageable);
        userPage.getContent().forEach(System.out::println);

        // 8. Named Query — findByNameNamed
        System.out.println("\n8. findByNameNamed(\"John\")");
        List<User> usersByNamedQuery = userRepository.findByNameNamed("John");
        usersByNamedQuery.forEach(System.out::println);

        // 9. Specification API — nameContains
        System.out.println("\n9. Specification — nameContains(\"John\")");
        List<User> usersByNameSpec = userRepository.findAll(UserRepository.UserSpecifications.nameContains("John"));
        usersByNameSpec.forEach(System.out::println);

        // 10. Specification API — nameOrAboutContains
        System.out.println("\n10. Specification — nameOrAboutContains(\"John\")");
        List<User> usersByOrSpec = userRepository.findAll(UserRepository.UserSpecifications.nameOrAboutContains("John"));
        usersByOrSpec.forEach(System.out::println);
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
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
