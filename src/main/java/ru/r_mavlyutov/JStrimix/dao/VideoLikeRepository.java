package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.r_mavlyutov.JStrimix.entity.VideoLike;

import java.util.List;
import java.util.Optional;

public interface VideoLikeRepository extends CrudRepository<VideoLike, Long> {
    Optional<VideoLike> findByUser_IdAndVideo_Id(Long userId, Long videoId);
    List<VideoLike> findByVideo_Id(Long videoId);
    
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.video.id = :videoId AND vl.isLike = true")
    Long countLikesByVideoId(Long videoId);
    
    @Query("SELECT COUNT(vl) FROM VideoLike vl WHERE vl.video.id = :videoId AND vl.isLike = false")
    Long countDislikesByVideoId(Long videoId);
}

