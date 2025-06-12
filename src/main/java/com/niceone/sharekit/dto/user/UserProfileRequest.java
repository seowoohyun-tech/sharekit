package com.niceone.sharekit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {
    private String name;
    private String studentId;
    private String password;
}