package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.r_mavlyutov.JStrimix.dao.UserRepository;
import ru.r_mavlyutov.JStrimix.entity.User;

import java.util.Optional;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByUsernameOrEmail() {
        // arrange
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("$2a$10$hash");
        user.setRoles("USER");
        userRepository.save(user);

        // act
        Optional<User> found = userRepository.findByUsernameOrEmail("john", "nope@example.com");

        // assert
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void testUniqueUsernameConstraint() {
        User u1 = new User();
        u1.setUsername("dup");
        u1.setEmail("dup1@ex.com");
        u1.setPassword("hash");
        u1.setRoles("USER");
        userRepository.save(u1);

        User u2 = new User();
        u2.setUsername("dup");
        u2.setEmail("dup2@ex.com");
        u2.setPassword("hash");
        u2.setRoles("USER");
        Assertions.assertThrows(Exception.class, () -> userRepository.saveAndFlush(u2));
    }
}
