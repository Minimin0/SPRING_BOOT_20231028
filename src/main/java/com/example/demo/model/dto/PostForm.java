package com.example.demo.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostForm {

    private String title;
    private String content;
    private Integer capacity;
    private MultipartFile poster;
}
