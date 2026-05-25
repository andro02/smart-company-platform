package com.smartcompany.dto;

import com.smartcompany.model.UserType;

public record SignUpDto(
    String username,
    char[] password,
    UserType type,
    boolean isActive,
    boolean mustChangePassword,
    String email,
    String image,
    String activationToken
) {}