package com.example.demo.controller;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.dto.MemberResponse;
import com.example.demo.model.dto.RoleRequest;
import com.example.demo.model.service.MemberService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
public class AdminApiController {

    private final MemberService memberService;

    public AdminApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> list(@RequestParam(required = false) String keyword) {
        return memberService.findAll(keyword).stream().map(MemberResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> get(@PathVariable Long id) {
        return memberService.findById(id)
                .map(member -> ResponseEntity.ok(MemberResponse.from(member)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@RequestBody MemberForm form) {
        Member saved = memberService.signup(form);
        return ResponseEntity.created(URI.create("/api/admin/members/" + saved.getId()))
                .body(MemberResponse.from(saved));
    }

    @PatchMapping("/{id}/role")
    public MemberResponse changeRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        return MemberResponse.from(memberService.changeRole(id, request.role()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
