package ru.r_mavlyutov.JStrimix.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public void uploadVideo(Long id, String title, String description, int duration) {
        Video video = new Video();
        video.setId(id);
        video.setTitle(title);
        video.setDescription(description);
        video.setDuration(duration);
        video.setViews(0);

        videoRepository.create(video);
    }

    @Override
    public Video getVideoById(Long id) {
        return videoRepository.read(id);
    }

    @Override
    public void deleteVideo(Long id) {
        videoRepository.delete(id);
    }

    @Override
    public void updateVideoInfo(Long id, String title, String description) {
        Video video = videoRepository.read(id);
        if (video != null) {
            video.setTitle(title);
            video.setDescription(description);
            videoRepository.update(video);
        }
    }

    @Override
    public void incrementViews(Long id) {
        Video video = videoRepository.read(id);
        if (video != null) {
            video.setViews(video.getViews() + 1);
            videoRepository.update(video);
        }
    }

    @Override
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }
}