package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.r_mavlyutov.JStrimix.entity.Subscription;

import java.util.List;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {

    List<Subscription> findAll();

    // Все подписки конкретного подписчика
    List<Subscription> findBySubscriber_Username(String subscriberUsername);

    // Требование задания: JPQL через связанную сущность (@Query)
    // Находим подписки по username канала (user, на которого подписываются)
    @Query("select s from Subscription s join s.channel ch where ch.username = :channelUsername")
    List<Subscription> findByChannelUsernameJPQL(String channelUsername);
}
