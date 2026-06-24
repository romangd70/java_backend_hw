package org.example.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Метод сохранения пользователя в рамках одной транзакции
     */
    @Transactional
    public void registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        // Здесь можно добавить бизнес-логику: валидация, проверки и т.д.
        userRepository.save(user);
    }

    /**
     * Пример транзакции с несколькими операциями
     */
    @Transactional
    public void registerMultipleUsers(List<User> users) {
        for (User user : users) {
            userRepository.save(user);
        }
    }
}