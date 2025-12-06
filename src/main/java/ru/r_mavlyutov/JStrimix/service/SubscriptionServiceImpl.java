package ru.r_mavlyutov.JStrimix.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.r_mavlyutov.JStrimix.dao.SubscriptionRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.entity.Subscription;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.exception.ResourceNotFoundException;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                  UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void subscribe(Long subscriberId, Long channelId) {
        if (subscriberId.equals(channelId)) {
            throw new IllegalArgumentException("Cannot subscribe to yourself");
        }

        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found: " + subscriberId));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + channelId));

        // Проверяем, не подписан ли уже
        if (subscriptionRepository.findBySubscriber_IdAndChannel_Id(subscriberId, channelId).isPresent()) {
            return; // Уже подписан
        }

        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriber);
        subscription.setChannel(channel);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(Long subscriberId, Long channelId) {
        subscriptionRepository.findBySubscriber_IdAndChannel_Id(subscriberId, channelId)
                .ifPresent(subscriptionRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubscribed(Long subscriberId, Long channelId) {
        return subscriptionRepository.findBySubscriber_IdAndChannel_Id(subscriberId, channelId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getSubscriberCount(Long channelId) {
        return subscriptionRepository.countSubscribersByChannelId(channelId);
    }
}

