package com.example.afterproject.repository;

import com.example.afterproject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findByRole(String role);
    List<UserEntity> findByNameContaining(String name);
    List<UserEntity> findByRoleAndNameContaining(String role, String name);

    Optional<UserEntity> findByEmail(String email);
}