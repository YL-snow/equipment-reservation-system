package com.ers.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "学号/工号不能为空")
    private String userId;

    @NotBlank(message = "密码不能为空")
    private String password;
}
