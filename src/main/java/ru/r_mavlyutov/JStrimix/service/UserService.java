package ru.r_mavlyutov.JStrimix.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.r_mavlyutov.JStrimix.entity.User;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    /**
     * Сохранить нового пользователя
     */
    User saveUser(User user);

    /**
     * Найти пользователя по username
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверить, существует ли пользователь с таким username или email
     */
    boolean existsByUsernameOrEmail(String username, String email);
}


