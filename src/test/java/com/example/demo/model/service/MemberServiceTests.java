package com.example.demo.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTests {

    @Mock
    private MemberRepository memberRepository;

    private PasswordEncoder passwordEncoder;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        memberService = new MemberService(memberRepository, passwordEncoder);
    }

    @Test
    void rejectsMismatchedPasswordsWithoutSaving() {
        MemberForm form = memberForm("student1", "123123", "999999");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> memberService.signup(form));

        assertEquals("비밀번호가 일치하지 않습니다.", error.getMessage());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void storesPasswordAsBcryptHash() {
        MemberForm form = memberForm("student1", "123123", "123123");
        when(memberRepository.existsByUsername("student1")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member member = memberService.signup(form);

        assertTrue(passwordEncoder.matches("123123", member.getPassword()));
        assertEquals("USER", member.getRole());
    }

    private MemberForm memberForm(String username, String password, String passwordConfirm) {
        MemberForm form = new MemberForm();
        form.setUsername(username);
        form.setName("최민");
        form.setPassword(password);
        form.setPasswordConfirm(passwordConfirm);
        return form;
    }
}
