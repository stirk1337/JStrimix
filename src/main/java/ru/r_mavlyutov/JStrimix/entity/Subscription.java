package ru.r_mavlyutov.JStrimix.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subscriptions_subscriber_channel",
                columnNames = {"subscriber_id", "channel_id"}
        )
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Подписчик
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_subscriber"))
    private User subscriber;

    // Канал (пользователь, на которого подписываются)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_channel"))
    private User channel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getSubscriber() { return subscriber; }
    public void setSubscriber(User subscriber) { this.subscriber = subscriber; }
    public User getChannel() { return channel; }
    public void setChannel(User channel) { this.channel = channel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}