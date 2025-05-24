package com.niceone.sharekit.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 구현 시

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "uid", nullable = false, unique = true)
    private String uid; // Firebase UID

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "student_id", unique = true)
    private String studentId;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role; // 관리자 권한 세부 구현하기

    @Builder
    public User(String uid, String email, String name, String studentId, String password, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.password = password;
        this.role = (role == null) ? "ROLE_PRE_AUTH_USER" : role;
    }
}