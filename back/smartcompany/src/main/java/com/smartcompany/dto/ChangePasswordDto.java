package com.smartcompany.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank(message = "Password must not be blank")
        @Size(min = 5, message = "Password must be at least 5 characters long")
        String newPassword
    ) 
    {}