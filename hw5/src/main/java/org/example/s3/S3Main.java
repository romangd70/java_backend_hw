package org.example.s3;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ComponentScan(basePackages = "org.example.s3")
@Configuration
public class S3Main {
    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example.s3");
        S3Client s3Client = context.getBean(S3Client.class);

        String bucketName = "test2";
        s3Client.createBucket(b -> b.bucket(bucketName));
        File file = new File("build.gradle.kts");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("company", "Baeldung");
        metadata.put("environment", "development");

        s3Client.putObject(request ->
                request
                    .bucket(bucketName)
                    .key(file.getName())
                    .metadata(metadata)
                    .ifNoneMatch("*"),
            file.toPath());
        String key = file.getName();

        var is = s3Client.getObject(request ->
                request
                    .bucket(bucketName)
                    .key(key),
            ResponseTransformer.toInputStream());
        System.out.println(new String(is.readAllBytes()));
    }

    @Bean
    public S3Client s3Client() {
        AwsCredentials credentials = AwsBasicCredentials.create("minioAccessKey", "minioSecretKey");
        return S3Client
            .builder()
            .endpointOverride(URI.create("http://localhost:9090"))
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(b -> b.pathStyleAccessEnabled(true))
            .build();
    }
}
