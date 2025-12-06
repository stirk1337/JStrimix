package ru.r_mavlyutov.JStrimix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.CommentRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Comment;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

@Service
public class VideoServiceImpl implements VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    public VideoServiceImpl(UserRepository userRepository,
                            VideoRepository videoRepository,
                            CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Video createVideoWithFirstComment(Long authorId,
                                             String title,
                                             String description,
                                             String videoPath,
                                             String previewPath,
                                             String firstCommentMessage) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + authorId));

        // 1) создаём видео
        Video video = new Video();
        video.setAuthor(author);
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoPath(videoPath);
        video.setPreviewPath(previewPath);
        Video saved = videoRepository.save(video);

        // 2) добавляем первый комментарий (если он нужен)
        if (firstCommentMessage != null && !firstCommentMessage.isBlank()) {
            Comment comment = new Comment();
            comment.setUser(author);
            comment.setVideo(saved);
            comment.setMessage(firstCommentMessage);
            commentRepository.save(comment);
        }

        // Если произойдёт RuntimeException на любом этапе — вся транзакция откатится.
        return saved;
    }

    @Override
    @Transactional
    public void deleteVideoWithAllRelations(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + videoId));

        // порядок важен при ограничениях FK:

        // 1) удалить комментарии
        commentRepository.deleteAll(commentRepository.findByVideo_Id(videoId));

        // 2) удалить само видео
        videoRepository.delete(video);
        // Любая ошибка посреди процесса — транзакция откатится, БД останется целостной.
    }
}
