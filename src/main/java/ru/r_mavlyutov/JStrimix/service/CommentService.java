package ru.r_mavlyutov.JStrimix.service;

import ru.r_mavlyutov.JStrimix.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment createComment(Long userId, Long videoId, String message);
    void deleteComment(Long commentId, Long userId);
    Comment updateComment(Long commentId, Long userId, String message);
    List<Comment> getCommentsByVideoId(Long videoId);
    Optional<Comment> findById(Long id);
}

