package com.example.demo.model.service;

import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.repository.PostRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    public static final int PAGE_SIZE = 10;

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostService(PostRepository postRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    public Page<Post> findPage(String keyword, String type, int page) {
        PageRequest pageable = pageRequest(page);
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findAll(pageable);
        }
        String query = keyword.strip();
        return "writer".equals(type)
                ? postRepository.searchByWriter(query, pageable)
                : postRepository.search(query, pageable);
    }

    public Page<Post> findByAuthor(String username, int page) {
        return postRepository.findByAuthor_Username(username, pageRequest(page));
    }

    public Post write(PostForm form, String username) {
        validate(form);
        Member author = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Post post = new Post();
        post.setTitle(form.getTitle().strip());
        post.setContent(form.getContent());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "글이 존재하지 않습니다."));
    }

    public Post read(Long id) {
        Post post = findById(id);
        post.setViewCount(post.getViewCount() + 1);
        return postRepository.save(post);
    }

    public Post findForEdit(Long id) {
        Post post = findById(id);
        checkWriter(post);
        return post;
    }

    public Post edit(Long id, PostForm form) {
        Post post = findForEdit(id);
        validate(form);
        post.setTitle(form.getTitle().strip());
        post.setContent(form.getContent());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public void delete(Long id) {
        Post post = findById(id);
        if (!isAdmin()) {
            checkWriter(post);
        }
        postRepository.delete(post);
    }

    private void validate(PostForm form) {
        if (form.getTitle() == null || form.getTitle().isBlank()
                || form.getContent() == null || form.getContent().isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 모두 입력하세요.");
        }
        if (form.getTitle().strip().length() > 100) {
            throw new IllegalArgumentException("제목은 100자까지 입력할 수 있습니다.");
        }
    }

    private void checkWriter(Post post) {
        Authentication authentication = authentication();
        if (!post.getAuthor().getUsername().equals(authentication.getName())) {
            throw new AccessDeniedException("본인이 쓴 글만 수정·삭제할 수 있습니다.");
        }
    }

    private boolean isAdmin() {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return authentication;
    }

    private PageRequest pageRequest(int page) {
        return PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id"));
    }
}
