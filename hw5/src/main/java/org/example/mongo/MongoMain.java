package org.example.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.UUID;

@ComponentScan(basePackages = "org.example.mongo")
@Configuration
@EnableMongoRepositories
public class MongoMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.mongo");
        AuthorRepository authorRepository = context.getBean(AuthorRepository.class);

        String id = UUID.randomUUID().toString();
        authorRepository.save(new Author(id, "John Doe"));
        System.out.println(authorRepository.findById(id));
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:password@localhost:27017/mydatabase?authSource=admin");
    }

    @Bean
    public MongoDatabaseFactory mongoDbFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "mydatabase");
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
