package com.example.demo.model.dto;

import lombok.Data;

@Data
public class MemberForm {

    private String username;
    private String password;
    private String passwordConfirm;
    private String name;
}
