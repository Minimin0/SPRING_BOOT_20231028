package com.example.demo.model.dto;

import com.example.demo.model.domain.Post;

public record SeatMessage(Long postId, int appliedCount, int capacity, int remaining) {

    public static SeatMessage from(Post post) {
        return new SeatMessage(
                post.getId(), post.getAppliedCount(), post.getCapacity(), post.getRemaining());
    }
}
