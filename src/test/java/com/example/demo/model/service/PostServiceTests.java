package com.example.demo.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.repository.EnrollmentRepository;
import com.example.demo.model.repository.PostRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
class PostServiceTests {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private FileStore fileStore;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostService(
                postRepository, memberRepository, enrollmentRepository, fileStore);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesPostWithAuthenticatedMemberAsAuthor() {
        Member author = member("student1", Member.ROLE_USER);
        PostForm form = form("  첫 글  ", "게시글 내용");
        when(memberRepository.findByUsername("student1")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post post = postService.write(form, "student1");

        assertEquals("첫 글", post.getTitle());
        assertEquals(author, post.getAuthor());
        verify(postRepository).save(post);
    }

    @Test
    void rejectsBlankPostWithoutSaving() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> postService.write(form(" ", "내용"), "student1"));

        assertEquals("제목과 내용을 모두 입력하세요.", error.getMessage());
        verify(postRepository, never()).save(any());
    }

    @Test
    void increasesViewCountWhenReadingPost() {
        Post post = post(member("student1", Member.ROLE_USER));
        post.setViewCount(2);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        Post result = postService.read(1L);

        assertEquals(3, result.getViewCount());
        verify(postRepository).save(post);
    }

    @Test
    void rejectsEditingAnotherUsersPostEvenForAdmin() {
        Post post = post(member("student1", Member.ROLE_USER));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        authenticate("admin", "ROLE_ADMIN");

        assertThrows(
                AccessDeniedException.class,
                () -> postService.edit(1L, form("수정", "내용")));

        verify(postRepository, never()).save(any());
    }

    @Test
    void allowsWriterToEditOwnPost() {
        Post post = post(member("student1", Member.ROLE_USER));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        authenticate("student1", "ROLE_USER");

        Post result = postService.edit(1L, form("수정 제목", "수정 내용"));

        assertEquals("수정 제목", result.getTitle());
        assertEquals("수정 내용", result.getContent());
        verify(postRepository).save(post);
    }

    @Test
    void allowsAdministratorToDeleteAnotherUsersPost() {
        Post post = post(member("student1", Member.ROLE_USER));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        authenticate("admin", "ROLE_ADMIN");

        postService.delete(1L);

        verify(postRepository).delete(post);
    }

    @Test
    void rejectsDeletingAnotherUsersPostForRegularUser() {
        Post post = post(member("student1", Member.ROLE_USER));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        authenticate("student2", "ROLE_USER");

        assertThrows(AccessDeniedException.class, () -> postService.delete(1L));

        verify(postRepository, never()).delete(any());
    }

    @Test
    void searchesWriterWithTenPostsPerPage() {
        Page<Post> expected = new PageImpl<>(java.util.List.of());
        when(postRepository.searchByWriter(
                org.mockito.ArgumentMatchers.eq("홍길동"), any(PageRequest.class)))
                .thenReturn(expected);

        Page<Post> result = postService.findPage("  홍길동  ", "writer", -1);

        assertEquals(expected, result);
        verify(postRepository).searchByWriter(
                org.mockito.ArgumentMatchers.eq("홍길동"),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }

    @Test
    void writesRecruitPostWithPoster() {
        Member author = member("student1", Member.ROLE_USER);
        PostForm form = form("스터디 모집", "함께 공부합니다.");
        form.setCapacity(5);
        when(memberRepository.findByUsername("student1")).thenReturn(Optional.of(author));
        when(fileStore.save(form.getPoster())).thenReturn("poster.png");
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post post = postService.write(form, "student1");

        assertEquals(5, post.getCapacity());
        assertEquals("poster.png", post.getPosterFile());
        assertEquals(5, post.getRemaining());
    }

    @Test
    void rejectsDeletingRecruitPostWithEnrollments() {
        Post post = post(member("student1", Member.ROLE_USER));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(enrollmentRepository.existsByPost(post)).thenReturn(true);
        authenticate("student1", "ROLE_USER");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> postService.delete(1L));

        assertEquals("신청자가 있는 모집글은 삭제할 수 없습니다.", error.getMessage());
        verify(postRepository, never()).delete(any());
    }

    private PostForm form(String title, String content) {
        PostForm form = new PostForm();
        form.setTitle(title);
        form.setContent(content);
        return form;
    }

    private Post post(Member author) {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("제목");
        post.setContent("내용");
        post.setAuthor(author);
        return post;
    }

    private Member member(String username, String role) {
        Member member = new Member();
        member.setUsername(username);
        member.setRole(role);
        return member;
    }

    private void authenticate(String username, String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        AuthorityUtils.createAuthorityList(authorities)));
    }
}
