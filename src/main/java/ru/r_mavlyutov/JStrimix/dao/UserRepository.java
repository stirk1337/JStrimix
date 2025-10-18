package ru.r_mavlyutov.JStrimix.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.r_mavlyutov.JStrimix.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // Пример OR (удобно для логина по username или email)
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsernameOrEmail(String username, String email);

    List<User> findAll(); // удобно иметь List-версию
}