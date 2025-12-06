package ru.r_mavlyutov.JStrimix.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.service.UserService;

@Configuration
public class DataInitializer {

    @Autowired(required = false)
    private UserService userService;

    @PostConstruct
    public void createAdminUser() {
        if (userService != null && !userService.existsByUsernameOrEmail("admin", "admin@example.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("admin"); // будет захеширован через UserService
            admin.setRoles("ADMIN,USER");
            userService.saveUser(admin);
            System.out.println("✓ Создан администратор: username=admin, password=admin");
        }
    }
}

