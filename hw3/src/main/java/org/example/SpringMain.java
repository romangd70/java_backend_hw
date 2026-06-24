package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class SpringMain {
    public static void main(String[] args) {
        SpringApplication.run(SpringMain.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserService userService) {
        return args -> {
            System.out.println("Spring Data JPA: all users");
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                printUser(user);
            }

            System.out.println("Spring Data JPA: user by username");
            userService.getUserByUsername("john_doe").ifPresent(SpringMain::printUser);

            System.out.println("Spring Data JPA: users with example.com email");
            userService.searchByEmail("example.com").forEach(SpringMain::printUser);

            System.out.println("Spring Data JPA: users created since 2024-01-01");
            userService.getUsersCreatedSince(LocalDateTime.of(2024, 1, 1, 0, 0))
                .forEach(SpringMain::printUser);
        };
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
