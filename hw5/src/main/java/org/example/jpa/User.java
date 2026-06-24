package org.example.jpa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NamedQuery(name = "User.findByNameNamed", query = "SELECT u FROM User u WHERE u.name = :name")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "about", updatable = false)
    private String about;

    public User(String name) {
        this.name = name;
    }

    public User(String name, String about) {
        this.name = name;
        this.about = about;
    }

    @PrePersist
    public void onPrePersist() {
        System.out.println("INSERT");
    }

    @PreUpdate
    public void onPreUpdate() {
        System.out.println("UPDATE");
    }

    @PreRemove
    public void onPreRemove() {
        System.out.println("DELETE");
    }

}
