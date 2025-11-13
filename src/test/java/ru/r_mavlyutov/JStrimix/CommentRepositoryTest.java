package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.r_mavlyutov.JStrimix.dao.CommentRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.dao.VideoRepository;
import ru.r_mavlyutov.JStrimix.entity.Comment;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.util.List;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VideoRepository videoRepository;

    @Test
    void testFindByVideoId() {
        // arrange
        User u = new User();
        u.setUsername("alex");
        u.setEmail("alex@example.com");
        u.setPassword("hash");
        u.setRoles("USER");
        userRepository.save(u);

        Video v = new Video();
        v.setAuthor(u);
        v.setTitle("Test video");
        v.setDescription("...");
        v.setVideoPath("/test.mp4");
        videoRepository.save(v);

        Comment c = new Comment();
        c.setUser(u);
        c.setVideo(v);
        c.setMessage("hello");
        commentRepository.save(c);

        // act
        List<Comment> comments = commentRepository.findByVideo_Id(v.getId());

        // assert
        Assertions.assertEquals(1, comments.size());
        Assertions.assertEquals("hello", comments.get(0).getMessage());
    }

    @Test
    void testFindByUserUsername() {
        // arrange
        User u = new User();
        u.setUsername("mike");
        u.setEmail("mike@example.com");
        u.setPassword("hash");
        u.setRoles("USER");
        userRepository.save(u);

        Video v = new Video();
        v.setAuthor(u);
        v.setTitle("Video");
        v.setDescription("desc");
        v.setVideoPath("/v.mp4");
        videoRepository.save(v);

        Comment c = new Comment();
        c.setUser(u);
        c.setVideo(v);
        c.setMessage("comment");
        commentRepository.save(c);

        // act
        var found = commentRepository.findByUser_Username("mike");

        // assert
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("comment", found.get(0).getMessage());
    }
}
