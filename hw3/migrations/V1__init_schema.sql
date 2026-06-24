-- Создание таблицы
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Добавление индекса
CREATE INDEX idx_users_email ON users(email);

-- Вставка данных
INSERT INTO users (id, username, email) VALUES
(1, 'john_doe', 'john@example.com'),
(2, 'jane_smith', 'jane@example.com');

-- Обновление данных
UPDATE users
SET email = 'john_new@example.com'
WHERE id = 1;

-- Удаление данных
DELETE FROM users
WHERE id = 2;