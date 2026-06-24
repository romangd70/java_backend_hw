package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByEmailContainingIgnoreCase(String emailPart);

    @Query("select u from User u where u.createdAt >= :createdAt order by u.createdAt desc")
    List<User> findCreatedSince(@Param("createdAt") LocalDateTime createdAt);
}
