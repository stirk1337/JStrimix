package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class VideoRepository implements CrudRepository<Video, Long> {

    private final List<Video> videoContainer;

    @Autowired
    public VideoRepository(List<Video> videoContainer) {
        this.videoContainer = videoContainer;
    }

    @Override
    public void create(Video video) {
        videoContainer.add(video);
    }

    @Override
    public Video read(Long id) {
        Optional<Video> video = videoContainer.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
        return video.orElse(null);
    }

    @Override
    public void update(Video video) {
        for (int i = 0; i < videoContainer.size(); i++) {
            if (videoContainer.get(i).getId().equals(video.getId())) {
                videoContainer.set(i, video);
                return;
            }
        }
    }

    @Override
    public void delete(Long id) {
        videoContainer.removeIf(video -> video.getId().equals(id));
    }

    public List<Video> findAll() {
        return new ArrayList<>(videoContainer);
    }
}