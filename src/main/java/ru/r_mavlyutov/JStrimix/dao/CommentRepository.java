package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.r_mavlyutov.JStrimix.entity.Comment;

import java.util.List;

@RepositoryRestResource(path = "comments", collectionResourceRel = "comments")
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAll();
    List<Comment> findByVideo_Id(Long videoId);
    List<Comment> findByUser_Username(String username);
}
