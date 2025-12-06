package ru.r_mavlyutov.JStrimix.dao.custom;

import ru.r_mavlyutov.JStrimix.entity.Video;

import java.time.Instant;
import java.util.List;

public interface VideoRepositoryCustom {

    /**
     * Аналог метода Query Lookup (And + Between):
     * найти видео по username автора и диапазону createdAt [from, to].
     */
    List<Video> findByAuthorUsernameAndCreatedAtBetweenCriteria(
            String authorUsername, Instant from, Instant to
    );

    /**
     * Поиск через связанную сущность (JOIN category):
     * найти видео по названию категории.
     */
    List<Video> findByCategoryNameCriteria(String categoryName);
}
