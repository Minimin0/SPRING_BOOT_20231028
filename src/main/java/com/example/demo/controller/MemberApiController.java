package com.example.demo.controller;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberResponse;
import com.example.demo.model.service.MemberService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;

    public MemberApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "message", "안녕하세요, REST API!",
                "time", LocalDateTime.now().withNano(0));
    }

    @GetMapping("/me")
    public MemberResponse me(Principal principal) {
        Member member = memberService.findByUsername(principal.getName());
        return MemberResponse.from(member);
    }
}
