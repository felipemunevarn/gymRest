package com.epam.gym.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
   @NotBlank(message = "username is required")
   String username,

   @NotBlank(message = "old password is required")
   String oldPassword,

   @NotBlank(message = "new password is required")
   String newPassword
) {}
