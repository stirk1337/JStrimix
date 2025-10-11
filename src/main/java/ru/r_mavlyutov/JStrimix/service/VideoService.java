package ru.r_mavlyutov.JStrimix.service;

import ru.r_mavlyutov.JStrimix.entity.Video;
import java.util.List;

public interface VideoService {
    void uploadVideo(Long id, String title, String description, int duration);
    Video getVideoById(Long id);
    void deleteVideo(Long id);
    void updateVideoInfo(Long id, String title, String description);
    void incrementViews(Long id);
    List<Video> getAllVideos();
}