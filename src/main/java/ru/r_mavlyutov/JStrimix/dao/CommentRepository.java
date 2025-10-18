package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.repository.CrudRepository;
import ru.r_mavlyutov.JStrimix.entity.Comment;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAll();
    List<Comment> findByVideo_Id(Long videoId);
    List<Comment> findByUser_Username(String username);
}
