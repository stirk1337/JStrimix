package ru.r_mavlyutov.JStrimix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoLikeRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.entity.VideoLike;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;

import java.util.Optional;

@Service
public class VideoLikeServiceImpl implements VideoLikeService {

    private final VideoLikeRepository videoLikeRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public VideoLikeServiceImpl(VideoLikeRepository videoLikeRepository,
                                 UserRepository userRepository,
                                 VideoRepository videoRepository) {
        this.videoLikeRepository = videoLikeRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @Override
    @Transactional
    public void toggleLike(Long userId, Long videoId, boolean isLike) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + videoId));

        Optional<VideoLike> existingLike = videoLikeRepository.findByUser_IdAndVideo_Id(userId, videoId);

        if (existingLike.isPresent()) {
            VideoLike like = existingLike.get();
            // Если тот же тип (лайк/дизлайк), удаляем, иначе меняем
            if (like.getIsLike().equals(isLike)) {
                videoLikeRepository.delete(like);
            } else {
                like.setIsLike(isLike);
                videoLikeRepository.save(like);
            }
        } else {
            VideoLike newLike = new VideoLike();
            newLike.setUser(user);
            newLike.setVideo(video);
            newLike.setIsLike(isLike);
            videoLikeRepository.save(newLike);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLikeCount(Long videoId) {
        return videoLikeRepository.countLikesByVideoId(videoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDislikeCount(Long videoId) {
        return videoLikeRepository.countDislikesByVideoId(videoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getUserLikeStatus(Long userId, Long videoId) {
        return videoLikeRepository.findByUser_IdAndVideo_Id(userId, videoId)
                .map(VideoLike::getIsLike)
                .orElse(null);
    }
}

