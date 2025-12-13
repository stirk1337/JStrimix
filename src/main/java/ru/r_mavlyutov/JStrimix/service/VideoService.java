package ru.r_mavlyutov.JStrimix.service;

import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.List;
import java.util.Optional;

public interface VideoService {

    /**
     * Создаёт видео и сразу добавляет первый комментарий автора — в одной транзакции.
     * Если что-то падает при добавлении комментария, видео не создаётся.
     */
    Video createVideoWithFirstComment(
            Long authorId,
            String title,
            String description,
            String videoPath,
            String previewPath,
            String firstCommentMessage
    );

    /**
     * Создаёт видео
     */
    Video createVideo(Long authorId, String title, String description, String videoPath, String previewPath, Long categoryId);

    /**
     * Обновляет видео
     */
    Video updateVideo(Long videoId, Long userId, String title, String description, Long categoryId);

    /**
     * Удаляет видео и все связанные комментарии/лайки/дизлайки — в одной транзакции.
     * Если на любом шаге будет ошибка, весь набор изменений откатывается.
     */
    void deleteVideoWithAllRelations(Long videoId, Long userId);

    /**
     * Получить видео по ID
     */
    Optional<Video> findById(Long id);

    /**
     * Получить все видео
     */
    List<Video> findAll();

    /**
     * Получить видео по автору
     */
    List<Video> findByAuthorId(Long authorId);

    /**
     * Поиск видео по запросу
     */
    List<Video> searchVideos(String query);

    /**
     * Получить видео по категории
     */
    List<Video> findByCategory(Long categoryId);
}
