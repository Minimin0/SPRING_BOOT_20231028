package com.example.demo.model.service;

import com.example.demo.model.domain.Comment;
import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.Notification;
import com.example.demo.model.repository.CommentRepository;
import com.example.demo.model.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostService postService;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(
            CommentRepository commentRepository,
            MemberRepository memberRepository,
            PostService postService,
            ApplicationEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postService = postService;
        this.eventPublisher = eventPublisher;
    }

    public List<Comment> findByPost(Post post) {
        return commentRepository.findByPostOrderByIdAsc(post);
    }

    @Transactional
    public Comment write(Long postId, String content, String username) {
        if (content == null || content.isBlank() || content.length() > 500) {
            throw new IllegalArgumentException("댓글은 1~500자로 입력하세요.");
        }
        Post post = postService.findById(postId);
        Member author = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Comment comment = new Comment();
        comment.setContent(content.strip());
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        String writer = post.getAuthor().getUsername();
        if (!writer.equals(username)) {
            eventPublisher.publishEvent(new Notification(
                    writer,
                    author.getName() + "님이 '" + post.getTitle() + "'에 댓글을 남겼습니다.",
                    post.getId()));
        }
        return saved;
    }

    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."));
        if (!isAdmin()) {
            checkWriter(comment);
        }
        commentRepository.delete(comment);
    }

    private void checkWriter(Comment comment) {
        if (!comment.getAuthor().getUsername().equals(authentication().getName())) {
            throw new AccessDeniedException("본인이 쓴 댓글만 삭제할 수 있습니다.");
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
}
