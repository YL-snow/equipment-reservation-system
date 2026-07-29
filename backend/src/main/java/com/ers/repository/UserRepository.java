package com.ers.repository;

import com.ers.entity.User;
import com.ers.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndNameContaining(UserRole role, String name, Pageable pageable);

    Page<User> findByRoleAndUserIdContaining(UserRole role, String userId, Pageable pageable);

    Page<User> findByNameContaining(String name, Pageable pageable);

    Page<User> findByUserIdContaining(String userId, Pageable pageable);

    Page<User> findByNameContainingAndUserIdContaining(String name, String userId, Pageable pageable);
}
