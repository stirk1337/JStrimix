package ru.r_mavlyutov.JStrimix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Автор комментария
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comments_user"))
    private User user;

    // Видео, к которому относится комментарий
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comments_video"))
    private Video video;

    @Lob
    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message; // Текст комментария

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
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}