package ru.r_mavlyutov.JStrimix.service;

public interface VideoViewService {
    void addView(Long userId, Long videoId);
    Long getViewCount(Long videoId);
}

