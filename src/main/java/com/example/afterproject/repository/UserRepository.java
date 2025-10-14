package com.example.afterproject.repository;

import com.example.afterproject.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // [추가] 역할로 사용자 목록 조회 (관리자용)
    List<UserEntity> findByRole(String role);

    // [추가] 이름 포함으로 사용자 목록 조회 (관리자용)
    List<UserEntity> findByNameContaining(String name);

    // [추가] 역할 및 이름 포함으로 사용자 목록 조회 (관리자용)
    List<UserEntity> findByRoleAndNameContaining(String role, String name);
}
