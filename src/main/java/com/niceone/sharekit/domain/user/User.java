package com.niceone.sharekit.domain.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", // 중간 테이블
            joinColumns = @JoinColumn(name = "user_uid"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Builder
    public User(String uid, String email, String name, String studentId, String password) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.password = password;
    }
}