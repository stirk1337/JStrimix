package ru.r_mavlyutov.JStrimix.service;

import ru.r_mavlyutov.JStrimix.entity.Video;

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
     * Удаляет видео и все связанные комментарии/лайки/дизлайки — в одной транзакции.
     * Если на любом шаге будет ошибка, весь набор изменений откатывается.
     */
    void deleteVideoWithAllRelations(Long videoId);
}
