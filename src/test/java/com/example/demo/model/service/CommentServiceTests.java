package com.example.demo.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.domain.Comment;
import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.repository.CommentRepository;
import com.example.demo.model.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CommentServiceTests {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostService postService;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, memberRepository, postService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesTrimmedCommentAsAuthenticatedMember() {
        Post post = new Post();
        Member author = member("student1");
        when(postService.findById(1L)).thenReturn(post);
        when(memberRepository.findByUsername("student1")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Comment comment = commentService.write(1L, "  좋은 글입니다.  ", "student1");

        assertEquals("좋은 글입니다.", comment.getContent());
        assertEquals(post, comment.getPost());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    void rejectsBlankCommentWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> commentService.write(1L, "   ", "student1"));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void rejectsDeletingAnotherUsersComment() {
        Comment comment = comment(member("student1"));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        authenticate("student2", "ROLE_USER");

        assertThrows(AccessDeniedException.class, () -> commentService.delete(1L));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void allowsAdministratorToDeleteComment() {
        Comment comment = comment(member("student1"));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        authenticate("admin", "ROLE_ADMIN");

        commentService.delete(1L);

        verify(commentRepository).delete(comment);
    }

    private Comment comment(Member author) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        return comment;
    }

    private Member member(String username) {
        Member member = new Member();
        member.setUsername(username);
        return member;
    }

    private void authenticate(String username, String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        AuthorityUtils.createAuthorityList(authority)));
    }
}
