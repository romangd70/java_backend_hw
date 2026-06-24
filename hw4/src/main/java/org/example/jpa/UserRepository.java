package org.example.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Пример метода запроса (query method)
    List<User> findByName(String name);

    // Пример метода запроса с несколькими условиями
    List<User> findByNameStartingWithAndIdLessThan(String namePrefix, Long idLimit);

    // Пример JPQL запроса
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
    List<User> searchByNameUsingJPQL(@Param("keyword") String keyword);

    // Пример нативного SQL запроса
    @Query(value = "SELECT * FROM users WHERE name LIKE %:keyword%", nativeQuery = true)
    List<User> searchByNameUsingNativeQuery(@Param("keyword") String keyword);

    // Пример сложного запроса с условиями
    @Query("SELECT u FROM User u WHERE (:name IS NULL OR u.name = :name) AND (:about IS NULL OR u.about = :about)")
    List<User> searchByOptionalCriteria(@Param("name") String name, @Param("about") String about);

    interface UserNameOnly {
        String getName();
    }

    @Query("SELECT u.name FROM User u WHERE u.id = :id")
    UserNameOnly findUserNameById(@Param("id") Long id);

    Page<User> findByName(String name, Pageable pageable);

    List<User> findByNameNamed(@Param("name") String name);

    class UserSpecifications {

        public static Specification<User> nameContains(String keyword) {
            return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + keyword + "%");
        }

        public static Specification<User> aboutContains(String keyword) {
            return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("about"), "%" + keyword + "%");
        }

        public static Specification<User> nameOrAboutContains(String keyword) {
            return nameContains(keyword).or(aboutContains(keyword));
        }
    }
}
