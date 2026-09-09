package com.example.demo; // 현재 폴더 위치(패키지)

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 컨트롤러 어노테이션 명시
public class DemoController {

    @GetMapping("/hello") // 전송 방식 GET
    public String hello(Model model) {
        model.addAttribute("data", "반갑습니다."); // model 설정
        return "hello"; // hello.html 연결
    }

    @GetMapping("/hello2") // 2주차 연습문제 : 맵핑 추가
    public String hello2(Model model) {
        model.addAttribute("dept", "미디어소프트웨어학과");   // 1
        model.addAttribute("studentId", "20231028"); // 2
        model.addAttribute("name", "최민");           // 3
        model.addAttribute("subject", "자바웹프로그래밍(2)"); // 4
        model.addAttribute("week", "2주차");          // 5
        return "hello2"; // hello2.html 연결
    }
}
