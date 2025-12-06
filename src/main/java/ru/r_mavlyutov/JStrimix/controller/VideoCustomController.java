package ru.r_mavlyutov.JStrimix.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.dao.custom.VideoRepositoryImpl;
import ru.r_mavlyutov.JStrimix.entity.Video;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/videos/custom")
@RequiredArgsConstructor
public class VideoCustomController {
    private final VideoRepository videoRepository; // основной репозиторий
    private final VideoRepositoryImpl videoRepositoryCustom;

    /**
     * Получить видео по автору и интервалу дат
     * GET /videos/custom/byAuthor?username=ramil&from=2025-01-01T00:00:00Z&to=2025-12-31T23:59:59Z
     */
    @GetMapping("/byAuthor")
    public List<Video> getByAuthorAndPeriod(
            @RequestParam String username,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return videoRepositoryCustom.findByAuthorUsernameAndCreatedAtBetweenCriteria(username, from, to);
    }

    /**
     * Получить видео по имени категории
     * GET /videos/custom/byCategory?categoryName=Music
     */
    @GetMapping("/byCategory")
    public List<Video> getByCategory(@RequestParam String categoryName) {
        return videoRepositoryCustom.findByCategoryNameCriteria(categoryName);
    }

    @GetMapping("/byId")
    public Video getById(@RequestParam Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video with id " + id + " not found"));
    }
}
