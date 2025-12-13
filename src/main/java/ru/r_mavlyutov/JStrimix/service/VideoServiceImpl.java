package ru.r_mavlyutov.JStrimix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.*;
import ru.r_mavlyutov.JStrimix.entity.Category;
import ru.r_mavlyutov.JStrimix.entity.Comment;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class VideoServiceImpl implements VideoService {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final VideoViewRepository videoViewRepository;

    public VideoServiceImpl(UserRepository userRepository,
                            VideoRepository videoRepository,
                            CommentRepository commentRepository,
                            CategoryRepository categoryRepository,
                            VideoLikeRepository videoLikeRepository,
                            VideoViewRepository videoViewRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.videoLikeRepository = videoLikeRepository;
        this.videoViewRepository = videoViewRepository;
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
    public Video createVideo(Long authorId, String title, String description, String videoPath, String previewPath, Long categoryId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + authorId));

        Video video = new Video();
        video.setAuthor(author);
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoPath(videoPath);
        video.setPreviewPath(previewPath);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            video.setCategory(category);
        }

        return videoRepository.save(video);
    }

    @Override
    @Transactional
    public Video updateVideo(Long videoId, Long userId, String title, String description, Long categoryId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + videoId));

        // Проверяем, что пользователь является автором видео
        if (!video.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own videos");
        }

        video.setTitle(title);
        video.setDescription(description);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            video.setCategory(category);
        } else {
            video.setCategory(null);
        }

        return videoRepository.save(video);
    }

    @Override
    @Transactional
    public void deleteVideoWithAllRelations(Long videoId, Long userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + videoId));

        // Проверяем, что пользователь является автором видео
        if (!video.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own videos");
        }

        // порядок важен при ограничениях FK:

        // 1) удалить лайки/дизлайки
        videoLikeRepository.deleteAll(videoLikeRepository.findByVideo_Id(videoId));

        // 2) удалить просмотры
        videoViewRepository.deleteAll(videoViewRepository.findByVideo_Id(videoId));

        // 3) удалить комментарии
        commentRepository.deleteAll(commentRepository.findByVideo_Id(videoId));

        // 4) удалить само видео
        videoRepository.delete(video);
        // Любая ошибка посреди процесса — транзакция откатится, БД останется целостной.
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Video> findById(Long id) {
        return videoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findAll() {
        return videoRepository.findAllWithAuthorAndCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findByAuthorId(Long authorId) {
        return videoRepository.findByAuthor_Id(authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> searchVideos(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        return videoRepository.findByTitleContainingIgnoreCase(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> findByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        return videoRepository.findByCategory_Name(category.getName());
    }
}
