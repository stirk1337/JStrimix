package ru.r_mavlyutov.JStrimix.service;

public interface SubscriptionService {
    void subscribe(Long subscriberId, Long channelId);
    void unsubscribe(Long subscriberId, Long channelId);
    boolean isSubscribed(Long subscriberId, Long channelId);
    Long getSubscriberCount(Long channelId);
}

