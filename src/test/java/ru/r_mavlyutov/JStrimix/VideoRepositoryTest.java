package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
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
class VideoRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    VideoRepository videoRepository;

    @Test
    void findByAuthor_UsernameAndCreatedAtBetween_shouldReturnVideo() {
        // arrange
        User author = new User();
        author.setUsername("alice");
        author.setEmail("alice@example.org");
        author.setPassword("$2a$10$hash"); // BCrypt-заглушка
        userRepository.save(author);

        Category cat = new Category();
        cat.setName("Education");
        categoryRepository.save(cat);

        Instant from = Instant.now().minusSeconds(10);
        Video v = new Video();
        v.setAuthor(author);
        v.setCategory(cat);
        v.setTitle("Intro to Spring Data");
        v.setDescription("...");
        v.setVideoPath("/videos/intro.mp4");
        v.setPreviewPath("/previews/intro.jpg");
        videoRepository.save(v);
        Instant to = Instant.now().plusSeconds(10);

        // act
        List<Video> found = videoRepository
                .findByAuthor_UsernameAndCreatedAtBetween("alice", from, to);

        // assert
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("Intro to Spring Data", found.get(0).getTitle());
    }
}
