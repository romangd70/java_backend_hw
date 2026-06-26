package org.example;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void createConfigUsesExpectedConnectionSettings() {
        HikariConfig config = Main.createConfig(
            "test-pool",
            "jdbc:postgresql://localhost:15432/test_db",
            "test_user",
            "secret"
        );

        assertEquals("test-pool", config.getPoolName());
        assertEquals("jdbc:postgresql://localhost:15432/test_db", config.getJdbcUrl());
        assertEquals("test_user", config.getUsername());
        assertEquals("secret", config.getPassword());
        assertEquals(5, config.getMaximumPoolSize());
        assertEquals(1, config.getMinimumIdle());
        assertEquals("SELECT 1", config.getConnectionTestQuery());
    }

    @Test
    void sqlQueriesMatchMigratedSchema() {
        assertEquals("SELECT COUNT(*) FROM users", Main.COUNT_USERS_SQL);
        assertEquals("SELECT id, username, email FROM get_user_by_id(?)", Main.GET_USER_BY_ID_SQL);
        assertEquals("SELECT COUNT(*) FROM products", Main.COUNT_PRODUCTS_SQL);
        assertEquals(
            "SELECT id, name, price FROM products WHERE price >= ? ORDER BY price DESC",
            Main.SELECT_PRODUCTS_BY_MIN_PRICE_SQL
        );
    }

    @Test
    void db1MigrationsCreateUsersSchemaSeedDataAndLookupFunction() throws IOException {
        String initUsers = readMigration("db1", "V1__init_users.sql");
        String insertUsers = readMigration("db1", "V2__insert_users.sql");
        String lookupFunction = readMigration("db1", "V3__add_get_user_by_id.sql");

        assertTrue(initUsers.contains("CREATE TABLE users"));
        assertTrue(initUsers.contains("id BIGINT PRIMARY KEY"));
        assertTrue(initUsers.contains("email VARCHAR(100) NOT NULL UNIQUE"));
        assertTrue(initUsers.contains("CREATE INDEX idx_users_email ON users(email)"));

        assertTrue(insertUsers.contains("(1, 'john_doe', 'john@example.com')"));
        assertTrue(insertUsers.contains("(2, 'jane_smith', 'jane@example.com')"));
        assertTrue(insertUsers.contains("(3, 'ivan_petrov', 'ivan@example.com')"));

        assertTrue(lookupFunction.contains("CREATE OR REPLACE FUNCTION get_user_by_id(user_id BIGINT)"));
        assertTrue(lookupFunction.contains("RETURNS TABLE(id BIGINT, username VARCHAR, email VARCHAR"));
        assertTrue(lookupFunction.contains("WHERE u.id = user_id"));
    }

    @Test
    void db2MigrationsCreateProductsSchemaAndSeedData() throws IOException {
        String initProducts = readMigration("db2", "V1__init_products.sql");
        String insertProducts = readMigration("db2", "V2__insert_products.sql");

        assertTrue(initProducts.contains("CREATE TABLE products"));
        assertTrue(initProducts.contains("id BIGINT PRIMARY KEY"));
        assertTrue(initProducts.contains("price NUMERIC(12, 2) NOT NULL"));
        assertTrue(initProducts.contains("CREATE INDEX idx_products_price ON products(price)"));

        assertTrue(insertProducts.contains("(1, 'Keyboard', 85.50)"));
        assertTrue(insertProducts.contains("(2, 'Monitor', 245.00)"));
        assertTrue(insertProducts.contains("(3, 'Dock station', 155.75)"));

        assertTrue(new BigDecimal("100.00").compareTo(new BigDecimal("85.50")) > 0);
        assertTrue(new BigDecimal("245.00").compareTo(new BigDecimal("100.00")) >= 0);
    }

    private static String readMigration(String database, String fileName) throws IOException {
        return Files.readString(Path.of("migrations", database, fileName));
    }
}
