package ru.r_mavlyutov.JStrimix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "video_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_video_likes_user_video",
                columnNames = {"user_id", "video_id"}
        )
)
public class VideoLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_video_likes_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_video_likes_video"))
    private Video video;

    @Column(nullable = false)
    private Boolean isLike; // true = лайк, false = дизлайк

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }
    public Boolean getIsLike() { return isLike; }
    public void setIsLike(Boolean isLike) { this.isLike = isLike; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

