package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.r_mavlyutov.JStrimix.dao.SubscriptionRepository;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.entity.Subscription;
import ru.r_mavlyutov.JStrimix.entity.User;

import java.util.List;

@DataJpaTest
class SubscriptionRepositoryTest {

    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    void findByChannelUsernameJPQL_shouldReturnSubscriptions() {
        // arrange: subscriber -> channel
        User channel = new User();
        channel.setUsername("channelA");
        channel.setEmail("chA@example.org");
        channel.setPassword("$2a$10$hash");
        userRepository.save(channel);

        User subscriber = new User();
        subscriber.setUsername("bob");
        subscriber.setEmail("bob@example.org");
        subscriber.setPassword("$2a$10$hash");
        userRepository.save(subscriber);

        Subscription s = new Subscription();
        s.setSubscriber(subscriber);
        s.setChannel(channel);
        subscriptionRepository.save(s);

        // act
        List<Subscription> found = subscriptionRepository.findByChannelUsernameJPQL("channelA");

        // assert
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("bob", found.get(0).getSubscriber().getUsername());
    }
}
