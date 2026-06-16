package com.ecommerce.user.repository;

import com.ecommerce.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByEmail(String email);
//
//    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
//    List<User> findAllByRoleName(@Param("roleName") String roleName);
//    @EntityGraph(attributePaths = {"addresses"})
//    @Query("SELECT u FROM User u")
//    List<User> findAllWithAddresses();
//    @EntityGraph(attributePaths = {"addresses"})
//    Optional<User> findById(Long id);
//
//    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.id = :id")
//    Optional<User> findByIdWithAddresses(@Param("id") Long id);
//
//}
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findAllByRoleName(@Param("roleName") String roleName);

    @EntityGraph(attributePaths = {"addresses"})
    @Query("SELECT DISTINCT u FROM User u")
    List<User> findAllWithAddresses();

    @Override
    Optional<User> findById(Long id);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.id = :id")
    Optional<User> findByIdWithAddresses(@Param("id") Long id);

    boolean existsByPhone(String phone);
}