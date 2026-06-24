package org.example.jdbc;

import org.example.jpa.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

@Configuration
public class JdbcTemplateMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.jdbc");

        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
        TransactionTemplate transactionTemplate = context.getBean(TransactionTemplate.class);

        createUsersTable(jdbcTemplate);
        clearUsers(jdbcTemplate);

        List<User> users = List.of(
            new User("John Doe" + new Random().nextInt(), "Test"),
            new User("Jane Doe" + new Random().nextInt(), "Test"),
            new User("Jane Doe" + new Random().nextInt(), "Test")
        );
//        insertUser(jdbcTemplate, users.get(0));
//        insertMultipleUsers(jdbcTemplate, users);
//        insertWithTransaction(jdbcTemplate, transactionTemplate, users);
//        insertWithTransactionRollback(jdbcTemplate, transactionTemplate, users);
//        testReadNotCommited(jdbcTemplate, transactionTemplate, users);
//        notDirtyRead(jdbcTemplate, transactionTemplate);
//        dirtyRead(jdbcTemplate, transactionTemplate);
//        repeatableRead(jdbcTemplate, transactionTemplate);
//        notRepeatableRead(jdbcTemplate, transactionTemplate);
//        phantomRead(jdbcTemplate, transactionTemplate);
//        notPhantomRead(jdbcTemplate, transactionTemplate);
//        anomalyExample(jdbcTemplate, transactionTemplate);
//        notAnomalyExample(jdbcTemplate, transactionTemplate);

//        propagationRequired(jdbcTemplate, transactionTemplate);
//        propagationSupport(jdbcTemplate, transactionTemplate);
        propagationRequiredNew(jdbcTemplate, transactionTemplate);

//        countUsers(jdbcTemplate);
        printUsers(jdbcTemplate);
        clearUsers(jdbcTemplate);
        clearUsers(jdbcTemplate);
    }

    static void clearUsers(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DELETE FROM users");
    }

    static void createUsersTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                about VARCHAR(255)
            )
            """);
    }

    static void printUsers(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.query(
            "SELECT id, name, about FROM users",
            (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("name"), rs.getString("about"))
        ).forEach(System.out::println);
    }

    static void countUsers(JdbcTemplate jdbcTemplate) {
        System.out.println(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
    }

    static void concatUsersName(JdbcTemplate jdbcTemplate) {
        System.out.println(jdbcTemplate.queryForObject("SELECT string_agg(name, ', ') FROM users", String.class));
    }

    static void insertUser(JdbcTemplate jdbcTemplate, User user) {
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", user.getName(), user.getAbout());
    }

    static void insertMultipleUsers(JdbcTemplate jdbcTemplate, List<User> users) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO users (name, about) VALUES (?, ?)",
            users,
            10,
            (ps, user) -> {
                ps.setString(1, user.getName());
                ps.setString(2, user.getAbout());
            }
        );
    }

    static void insertWithTransaction(JdbcTemplate jdbcTemplate,
                                      TransactionTemplate transactionTemplate,
                                      List<User> users) {
        transactionTemplate.execute(status -> {
            users.forEach(user -> {
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", user.getName(), user.getAbout());
                printUsers(jdbcTemplate);
                System.out.println("-----------");
            });
            return null;
        });
    }

    static void insertWithTransactionRollback(JdbcTemplate jdbcTemplate,
                                              TransactionTemplate transactionTemplate,
                                              List<User> users) {
        transactionTemplate.execute(status -> {
            users.forEach(user -> {
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", user.getName(), user.getAbout());
                printUsers(jdbcTemplate);
                System.out.println("-----------");
                status.setRollbackOnly();
            });
            return null;
        });
    }

    static void testReadNotCommited(JdbcTemplate jdbcTemplate,
                                    TransactionTemplate transactionTemplate,
                                    List<User> users) {
        Thread threadInsert = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                users.forEach(user -> {
                    jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", user.getName(), user.getAbout());
                    sleep(1_000);
                });
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(jdbcTemplate);
                sleep(100);
            }
        });

        try {
            threadInsert.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void notDirtyRead(JdbcTemplate jdbcTemplate,
                             TransactionTemplate transactionTemplate) {
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                jdbcTemplate.update("UPDATE users SET about=about || ' Test' WHERE name='Luci'");
                sleep(3_000);
                status.setRollbackOnly();
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                sleep(1_000);
                printUsers(jdbcTemplate);
                return null;
            });
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void dirtyRead(JdbcTemplate jdbcTemplate,
                          TransactionTemplate transactionTemplate) {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                jdbcTemplate.update("UPDATE users SET about=about || ' Test' WHERE name='Luci'");
                sleep(3_000);
                status.setRollbackOnly();
                return null;
            });
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            printUsers(jdbcTemplate);
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Не сработает в postgres
    }

    static void repeatableRead(JdbcTemplate jdbcTemplate,
                               TransactionTemplate transactionTemplate) {
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 1");
                jdbcTemplate.update("UPDATE users SET about=about || ' Test' WHERE name='Luci'");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 2");
                printUsers(jdbcTemplate);
                sleep(3_000);
                printUsers(jdbcTemplate);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void notRepeatableRead(JdbcTemplate jdbcTemplate,
                                  TransactionTemplate transactionTemplate) {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 1");
                jdbcTemplate.update("UPDATE users SET about=about || ' Test' WHERE name='Luci'");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 2");
                printUsers(jdbcTemplate);
                sleep(3_000);
                printUsers(jdbcTemplate);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void phantomRead(JdbcTemplate jdbcTemplate,
                            TransactionTemplate transactionTemplate) {
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 1");
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Mark", "Test");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 2");
                concatUsersName(jdbcTemplate);
                sleep(3_000);
                concatUsersName(jdbcTemplate);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void notPhantomRead(JdbcTemplate jdbcTemplate,
                               TransactionTemplate transactionTemplate) {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
        Thread thread1 = Thread.startVirtualThread(() -> {
            sleep(1_000);
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 1");
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Mark", "Test");
                return null;
            });
            System.out.println("Finish thread 1");
        });
        Thread thread2 = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                System.out.println("Start thread 2");
                concatUsersName(jdbcTemplate);
                sleep(3_000);
                concatUsersName(jdbcTemplate);
                return null;
            });
            System.out.println("Finish thread 2");
        });

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    static void anomalyExample(JdbcTemplate jdbcTemplate,
                               TransactionTemplate transactionTemplate) {
        jdbcTemplate.update("DROP TABLE IF EXISTS calculator");
        jdbcTemplate.update("CREATE TABLE calculator (class INT, value INT)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 10)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 20)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 100)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 200)");
        try {
            transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
            Thread thread1 = Thread.startVirtualThread(() -> {
                transactionTemplate.execute(status -> {
                    System.out.println("Start thread 1");
                    jdbcTemplate.execute("SELECT SUM(value) FROM calculator WHERE class = 1");
                    sleep(2_000);
                    jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 30)");
                    return null;
                });
                System.out.println("Finish thread 1");
            });
            Thread thread2 = Thread.startVirtualThread(() -> {
                transactionTemplate.execute(status -> {
                    System.out.println("Start thread 2");
                    jdbcTemplate.execute("SELECT SUM(value) FROM calculator WHERE class = 2");
                    sleep(2_000);
                    jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 300)");
                    return null;
                });
                System.out.println("Finish thread 2");
            });

            try {
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        jdbcTemplate.query(
            "SELECT class, value FROM calculator",
            (rs, rowNum) -> rs.getInt("class") + " " + rs.getInt("value")
        ).forEach(System.out::println);
        jdbcTemplate.update("DROP TABLE calculator");
    }

    static void notAnomalyExample(JdbcTemplate jdbcTemplate,
                                  TransactionTemplate transactionTemplate) {
        jdbcTemplate.update("DROP TABLE IF EXISTS calculator");
        jdbcTemplate.update("CREATE TABLE calculator (class INT, value INT)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 10)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 20)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 100)");
        jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 200)");
        try {
            transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            Thread thread1 = Thread.startVirtualThread(() -> {
                sleep(1_000);
                transactionTemplate.execute(status -> {
                    System.out.println("Start thread 1");
                    jdbcTemplate.execute("SELECT SUM(value) FROM calculator WHERE class = 1");
                    sleep(2_000);
                    jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (2, 30)");
                    return null;
                });
                System.out.println("Finish thread 1");
            });
            Thread thread2 = Thread.startVirtualThread(() -> {
                sleep(1_000);
                transactionTemplate.execute(status -> {
                    System.out.println("Start thread 2");
                    jdbcTemplate.execute("SELECT SUM(value) FROM calculator WHERE class = 2");
                    sleep(2_000);
                    jdbcTemplate.update("INSERT INTO calculator (class, value) VALUES (1, 300)");
                    return null;
                });
                System.out.println("Finish thread 2");
            });

            try {
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        jdbcTemplate.query(
            "SELECT class, value FROM calculator",
            (rs, rowNum) -> rs.getInt("class") + " " + rs.getInt("value")
        ).forEach(System.out::println);
        jdbcTemplate.update("DROP TABLE calculator");
    }

    static void propagationRequired(JdbcTemplate jdbcTemplate,
                                    TransactionTemplate transactionTemplate) {
        Thread threadInsert = Thread.startVirtualThread(() -> {
            transactionTemplate.execute(status -> {
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
                sleep(2_000);
                transactionTemplate.execute(status2 -> {
                    jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Mark", "Test");
                    return null;
                });
                sleep(2_000);
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(jdbcTemplate);
                sleep(100);
            }
        });

        try {
            threadInsert.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void propagationSupport(JdbcTemplate jdbcTemplate,
                                   TransactionTemplate transactionTemplate) {
        Thread threadInsert = Thread.startVirtualThread(() -> {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
            transactionTemplate.execute(status -> {
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
                sleep(2_000);
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Mark", "Test");
                sleep(2_000);
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(jdbcTemplate);
                sleep(100);
            }
        });

        try {
            threadInsert.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void propagationRequiredNew(JdbcTemplate jdbcTemplate,
                                       TransactionTemplate transactionTemplate) {
        Thread threadInsert = Thread.startVirtualThread(() -> {
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionTemplate.execute(status -> {
                jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Luci", "Test");
                sleep(2_000);
                transactionTemplate.execute(status2 -> {
                    jdbcTemplate.update("INSERT INTO users (name, about) VALUES (?, ?)", "Mark", "Test");
                    return null;
                });
                sleep(2_000);
                return null;
            });
        });
        Thread.startVirtualThread(() -> {
            while (threadInsert.isAlive()) {
                countUsers(jdbcTemplate);
                sleep(100);
            }
        });

        try {
            threadInsert.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
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
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
