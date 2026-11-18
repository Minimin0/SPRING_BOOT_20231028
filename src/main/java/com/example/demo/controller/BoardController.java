package com.example.demo.controller;

import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.service.CommentService;
import com.example.demo.model.service.PostService;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final PostService postService;
    private final CommentService commentService;

    public BoardController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String type,
            Model model) {
        String normalizedKeyword = keyword == null ? null : keyword.strip();
        Page<Post> posts = postService.findPage(normalizedKeyword, type, page);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("type", type);
        return "board/list";
    }

    @GetMapping("/my")
    public String myPosts(
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {
        Page<Post> posts = postService.findByAuthor(principal.getName(), page);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("pageUrl", "/board/my");
        model.addAttribute("listTitle", "내가 쓴 글");
        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm(PostForm postForm) {
        return "board/form";
    }

    @PostMapping("/write")
    public String write(PostForm postForm, Principal principal, Model model) {
        try {
            Post post = postService.write(postForm, principal.getName());
            return "redirect:/board/" + post.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "board/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.read(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.findByPost(post));
        return "board/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findForEdit(id);
        PostForm postForm = new PostForm();
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());
        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id);
        return "board/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, PostForm postForm, Model model) {
        try {
            postService.edit(id, postForm);
            return "redirect:/board/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postId", id);
            return "board/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            postService.delete(id);
            return "redirect:/board?deleted";
        } catch (IllegalStateException e) {
            return "redirect:/board/" + id + "?deleteError";
        }
    }
}
