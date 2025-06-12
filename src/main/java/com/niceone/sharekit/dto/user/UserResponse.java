package com.niceone.sharekit.dto;

import com.niceone.sharekit.domain.user.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String uid;
    private String email;
    private String name;
    private String studentId;

    public UserResponse(User user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.name = user.getName();
        this.studentId = user.getStudentId();
    }
}