plugins {
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.3"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.Main"
    }
}

repositories {
    mavenCentral()
    maven("https://artifactory.tcsbank.ru/artifactory/maven-all")
    maven("https://artifactory.tcsbank.ru/artifactory/maven-releases-hosted/ins-integration")
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.7")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.clickhouse:clickhouse-jdbc:0.7.1")
    implementation("org.lz4:lz4-java:1.8.0")

    implementation("org.springframework.data:spring-data-redis")
    implementation("redis.clients:jedis:5.1.2")

    implementation("software.amazon.awssdk:s3:2.29.0")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("com.hazelcast:hazelcast")
}

tasks.test {
    useJUnitPlatform()
}

fun registerJavaExampleTask(taskName: String, mainClassName: String) {
    tasks.register<JavaExec>(taskName) {
        group = "examples"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
    }
}

registerJavaExampleTask("runJdbc", "org.example.jdbc.JdbcTemplateMain")
registerJavaExampleTask("runJpa", "org.example.jpa.JpaMain")
registerJavaExampleTask("runJpaLocks", "org.example.jpa.LockMain")
registerJavaExampleTask("runClickHouse", "org.example.clickhouse.ClickHouseMain")
registerJavaExampleTask("runRedis", "org.example.redis.RedisMain")
registerJavaExampleTask("runMongo", "org.example.mongo.MongoMain")
registerJavaExampleTask("runS3", "org.example.s3.S3Main")
registerJavaExampleTask("runHazelcast", "org.example.hazelcast.HazelcastMain")
