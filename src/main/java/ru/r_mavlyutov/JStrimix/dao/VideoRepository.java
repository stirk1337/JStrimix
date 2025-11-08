package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.r_mavlyutov.JStrimix.dao.custom.VideoRepositoryCustom;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.time.Instant;
import java.util.List;

@RepositoryRestResource(path = "videos", collectionResourceRel = "videos")
public interface VideoRepository
        extends CrudRepository<Video, Long>, VideoRepositoryCustom {

    List<Video> findAll();

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT v FROM Video v")
    List<Video> findAllWithAuthorAndCategory();

    // Требование задания: Query Lookup с ключевыми словами And + Between
    // Поиск видео по автору и интервалу дат создания
    List<Video> findByAuthor_UsernameAndCreatedAtBetween(
            String authorUsername,
            Instant createdAfter,
            Instant createdBefore
    );

    // Пара удобных методов
    List<Video> findByCategory_Name(String categoryName);
    List<Video> findByTitleContainingIgnoreCase(String q);
}
