package com.example.demo.controller;

import com.example.demo.model.service.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    private final TestService testService;

    public DemoController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("data", "반갑습니다.");
        return "hello";
    }

    @GetMapping("/hello2")
    public String hello2(Model model) {
        model.addAttribute("dept", "미디어소프트웨어학과");
        model.addAttribute("studentId", "20231028");
        model.addAttribute("name", "최민");
        model.addAttribute("subject", "자바웹프로그래밍(2)");
        model.addAttribute("week", "2주차");
        return "hello2";
    }

    @GetMapping("/testdb")
    public String testdb(Model model) {
        model.addAttribute("users", testService.findAll());
        return "testdb";
    }
}
