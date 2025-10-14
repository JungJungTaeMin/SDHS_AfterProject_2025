package com.example.afterproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AFTER_USERS")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "after_users_seq")
    @SequenceGenerator(name = "after_users_seq", sequenceName = "AFTER_USERS_SEQ", allocationSize = 1)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "role", nullable = false)
    private String role; // 'STUDENT', 'TEACHER', 'ADMIN'

    @Column(name = "student_id_no", unique = true)
    private String studentIdNo;
}
