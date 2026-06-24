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

tasks.register<JavaExec>("runHibernateDemo") {
    group = "application"
    description = "Runs Hibernate/JPA select query examples against the users table."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.Main")
}

tasks.register<JavaExec>("runSpringDataDemo") {
    group = "application"
    description = "Runs Spring Data JPA select query examples against the users table."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.SpringMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.hibernate.orm:hibernate-core:6.6.2.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.6.2.Final")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
