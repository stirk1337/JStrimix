package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.r_mavlyutov.JStrimix.entity.VideoView;

import java.util.Optional;

public interface VideoViewRepository extends CrudRepository<VideoView, Long> {
    Optional<VideoView> findByUser_IdAndVideo_Id(Long userId, Long videoId);
    java.util.List<VideoView> findByVideo_Id(Long videoId);
    
    @Query("SELECT COUNT(vv) FROM VideoView vv WHERE vv.video.id = :videoId")
    Long countViewsByVideoId(Long videoId);
}

