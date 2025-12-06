package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.r_mavlyutov.JStrimix.dao.CategoryRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Category;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.time.Instant;
import java.util.List;

@DataJpaTest
class VideoCriteriaRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    VideoRepository videoRepository; // расширяет VideoRepositoryCustom

    @Test
    void criteria_findByAuthorUsernameAndCreatedAtBetween_shouldWork() {
        // arrange
        User author = new User();
        author.setUsername("alice");
        author.setEmail("alice@example.org");
        author.setPassword("$2a$10$hash");
        userRepository.save(author);

        Instant from = Instant.now().minusSeconds(10);
        Video v = new Video();
        v.setAuthor(author);
        v.setTitle("Criteria Between");
        v.setDescription("...");
        v.setVideoPath("/videos/c.mp4");
        videoRepository.save(v);
        Instant to = Instant.now().plusSeconds(10);

        // act
        var result = videoRepository.findByAuthorUsernameAndCreatedAtBetweenCriteria(
                "alice", from, to
        );

        // assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Criteria Between", result.get(0).getTitle());
    }

    @Test
    void criteria_findByCategoryName_shouldWork() {
        // arrange
        User author = new User();
        author.setUsername("ann");
        author.setEmail("ann@example.org");
        author.setPassword("$2a$10$hash");
        userRepository.save(author);

        Category cat = new Category();
        cat.setName("Music");
        categoryRepository.save(cat);

        Video v = new Video();
        v.setAuthor(author);
        v.setCategory(cat);
        v.setTitle("Guitar 101");
        v.setDescription("...");
        v.setVideoPath("/videos/guitar.mp4");
        videoRepository.save(v);

        // act
        List<Video> result = videoRepository.findByCategoryNameCriteria("Music");

        // assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Guitar 101", result.get(0).getTitle());
    }
}
