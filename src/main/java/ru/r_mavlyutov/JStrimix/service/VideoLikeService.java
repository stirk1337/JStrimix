package ru.r_mavlyutov.JStrimix.service;

public interface VideoLikeService {
    void toggleLike(Long userId, Long videoId, boolean isLike);
    Long getLikeCount(Long videoId);
    Long getDislikeCount(Long videoId);
    Boolean getUserLikeStatus(Long userId, Long videoId);
}

