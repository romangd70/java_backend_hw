package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    static final String COUNT_USERS_SQL = "SELECT COUNT(*) FROM users";
    static final String GET_USER_BY_ID_SQL = "SELECT id, username, email FROM get_user_by_id(?)";
    static final String COUNT_PRODUCTS_SQL = "SELECT COUNT(*) FROM products";
    static final String SELECT_PRODUCTS_BY_MIN_PRICE_SQL =
        "SELECT id, name, price FROM products WHERE price >= ? ORDER BY price DESC";

    public static void main(String[] args) {
        try (
            HikariDataSource db1 = createDataSource(
                "db1-pool",
                env("DB1_URL", "jdbc:postgresql://localhost:5432/app_db1"),
                env("DB1_USER", "admin"),
                env("DB1_PASSWORD", "password")
            );
            HikariDataSource db2 = createDataSource(
                "db2-pool",
                env("DB2_URL", "jdbc:postgresql://localhost:5433/app_db2"),
                env("DB2_USER", "admin"),
                env("DB2_PASSWORD", "password")
            )
        ) {
            queryDb1(db1);
            queryDb2(db2);
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private static HikariDataSource createDataSource(
        String poolName,
        String jdbcUrl,
        String username,
        String password
    ) {
        return new HikariDataSource(createConfig(poolName, jdbcUrl, username, password));
    }

    static HikariConfig createConfig(
        String poolName,
        String jdbcUrl,
        String username,
        String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");
        return config;
    }

    private static void queryDb1(HikariDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(COUNT_USERS_SQL);
                ResultSet resultSet = statement.executeQuery()
            ) {
                if (resultSet.next()) {
                    System.out.println("DB1 users count: " + resultSet.getLong(1));
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(GET_USER_BY_ID_SQL)
            ) {
                statement.setLong(1, 1L);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.printf(
                            "DB1 user: id=%d, username=%s, email=%s%n",
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("email")
                        );
                    }
                }
            }
        }
    }

    private static void queryDb2(HikariDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (
                PreparedStatement statement = connection.prepareStatement(COUNT_PRODUCTS_SQL);
                ResultSet resultSet = statement.executeQuery()
            ) {
                if (resultSet.next()) {
                    System.out.println("DB2 products count: " + resultSet.getLong(1));
                }
            }

            try (
                PreparedStatement statement = connection.prepareStatement(SELECT_PRODUCTS_BY_MIN_PRICE_SQL)
            ) {
                statement.setBigDecimal(1, new BigDecimal("100.00"));
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.printf(
                            "DB2 product: id=%d, name=%s, price=%s%n",
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getBigDecimal("price")
                        );
                    }
                }
            }
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
