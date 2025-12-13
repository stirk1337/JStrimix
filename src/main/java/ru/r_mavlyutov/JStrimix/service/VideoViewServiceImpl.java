package ru.r_mavlyutov.JStrimix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoViewRepository;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.entity.VideoView;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;

@Service
public class VideoViewServiceImpl implements VideoViewService {

    private final VideoViewRepository videoViewRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public VideoViewServiceImpl(VideoViewRepository videoViewRepository,
                                UserRepository userRepository,
                                VideoRepository videoRepository) {
        this.videoViewRepository = videoViewRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    @Transactional
    public void addView(Long userId, Long videoId) {
        // Проверяем, не просматривал ли уже пользователь это видео
        if (videoViewRepository.findByUser_IdAndVideo_Id(userId, videoId).isPresent()) {
            return; // Уже просмотрено
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + videoId));

        VideoView view = new VideoView();
        view.setUser(user);
        view.setVideo(video);
        videoViewRepository.save(view);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getViewCount(Long videoId) {
        return videoViewRepository.countViewsByVideoId(videoId);
    }
}

