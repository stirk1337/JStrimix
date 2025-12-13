package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.r_mavlyutov.JStrimix.entity.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
    Optional<Subscription> findBySubscriber_IdAndChannel_Id(Long subscriberId, Long channelId);
    List<Subscription> findBySubscriber_Id(Long subscriberId);
    List<Subscription> findByChannel_Id(Long channelId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.channel.id = :channelId")
    Long countSubscribersByChannelId(Long channelId);
}
