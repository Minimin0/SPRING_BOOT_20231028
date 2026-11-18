package com.example.demo.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.domain.Enrollment;
import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.repository.EnrollmentRepository;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTests {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(
                enrollmentRepository, postRepository, memberRepository);
    }

    @Test
    void confirmsEveryRecruitPostInCart() {
        Member member = member("student1");
        Post first = recruit(1L, "자바 스터디", 3, 0);
        Post second = recruit(2L, "스프링 특강", 2, 1);
        when(memberRepository.findByUsername("student1")).thenReturn(Optional.of(member));
        when(postRepository.findById(1L)).thenReturn(Optional.of(first));
        when(postRepository.findById(2L)).thenReturn(Optional.of(second));

        int count = enrollmentService.confirm("student1", List.of(1L, 2L));

        assertEquals(2, count);
        assertEquals(1, first.getAppliedCount());
        assertEquals(2, second.getAppliedCount());
        verify(enrollmentRepository, org.mockito.Mockito.times(2)).save(any(Enrollment.class));
    }

    @Test
    void rejectsFullRecruitPostWithoutSaving() {
        Member member = member("student1");
        Post full = recruit(1L, "마감 특강", 1, 1);
        when(memberRepository.findByUsername("student1")).thenReturn(Optional.of(member));
        when(postRepository.findById(1L)).thenReturn(Optional.of(full));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> enrollmentService.confirm("student1", List.of(1L)));

        assertEquals("정원이 마감되었습니다 : 마감 특강", error.getMessage());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void cancelsOwnEnrollmentAndRestoresSeat() {
        Member member = member("student1");
        Post post = recruit(1L, "자바 스터디", 3, 1);
        Enrollment enrollment = new Enrollment();
        enrollment.setMember(member);
        enrollment.setPost(post);
        when(enrollmentRepository.findById(7L)).thenReturn(Optional.of(enrollment));

        enrollmentService.cancel(7L, "student1");

        assertEquals(0, post.getAppliedCount());
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void rejectsCancellingAnotherUsersEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setMember(member("student1"));
        enrollment.setPost(recruit(1L, "자바 스터디", 3, 1));
        when(enrollmentRepository.findById(7L)).thenReturn(Optional.of(enrollment));

        assertThrows(
                AccessDeniedException.class,
                () -> enrollmentService.cancel(7L, "student2"));

        verify(enrollmentRepository, never()).delete(any());
    }

    private Post recruit(Long id, String title, int capacity, int appliedCount) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setCapacity(capacity);
        post.setAppliedCount(appliedCount);
        return post;
    }

    private Member member(String username) {
        Member member = new Member();
        member.setUsername(username);
        return member;
    }
}
